/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.ateno.sonarqube.test.rules;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.ateno.sonarqube.test.languages.BarLanguage;

/**
 * The goal of this Sensor is to load the results of an analysis performed by a fictive external tool named: BarLint
 * Results are provided as an xml file and are corresponding to the rules defined in 'rules.xml'.
 * To be very abstract, these rules are applied on source files made with the fictive language Bar.
 */
public class BarLintIssuesLoaderSensor implements Sensor {

  private static final Logger LOGGER = Loggers.get(BarLintIssuesLoaderSensor.class);

  protected static final String REPORT_PATH_KEY = "sonar.barlint.reportPath";

  protected final Settings settings;
  protected final FileSystem fileSystem;
  protected SensorContext context;

  /**
   * Use of IoC to get Settings, FileSystem, RuleFinder and ResourcePerspectives
   */
  public BarLintIssuesLoaderSensor(final Settings settings, final FileSystem fileSystem) {
    this.settings = settings;
    this.fileSystem = fileSystem;
  }

  @Override
  public void describe(final SensorDescriptor descriptor) {
    descriptor.name("BarLint Issues Loader Sensor");
    descriptor.onlyOnLanguage(BarLanguage.KEY);
  }

  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  protected String getReportPath() {
    String reportPath = settings.getString(reportPathKey());
    if (!StringUtils.isEmpty(reportPath)) {
      return reportPath;
    } else {
      return null;
    }
  }

  @Override
  public void execute(final SensorContext context) {
    if (!StringUtils.isEmpty(getReportPath())) {
      this.context = context;
      String reportPath = getReportPath();
      File analysisResultsFile = new File(reportPath);
      try {
        parseAndSaveResults(analysisResultsFile);
      } catch (XMLStreamException e) {
        throw new IllegalStateException("Unable to parse the provided BarLint file", e);
      }
    }
  }

  protected void parseAndSaveResults(final File file) throws XMLStreamException {
    LOGGER.info("(mock) Parsing 'BarLint' Analysis Results");
    BarLintAnalysisResultsParser parser = new BarLintAnalysisResultsParser();
    List<BarLintError> errors = parser.parse(file);
    for (BarLintError error : errors) {
      getResourceAndSaveIssue(error);
    }
  }

  private void getResourceAndSaveIssue(final BarLintError error) {
    LOGGER.debug(error.toString());

    InputFile inputFile = fileSystem.inputFile(
      fileSystem.predicates().and(
        fileSystem.predicates().hasRelativePath(error.getFilePath()),
        fileSystem.predicates().hasType(InputFile.Type.MAIN)));

    LOGGER.debug("inputFile null ? " + (inputFile == null));

    if (inputFile != null) {
      saveIssue(inputFile, error.getLine(), error.getType(), error.getDescription());
    } else {
      LOGGER.error("Not able to find a InputFile with " + error.getFilePath());
    }
  }

  private void saveIssue(final InputFile inputFile, int line, final String externalRuleKey, final String message) {
    RuleKey ruleKey = RuleKey.of(getRepositoryKeyForLanguage(inputFile.language()), externalRuleKey);

    NewIssue newIssue = context.newIssue()
      .forRule(ruleKey);

    NewIssueLocation primaryLocation = newIssue.newLocation()
      .on(inputFile)
      .message(message);
    if (line > 0) {
      primaryLocation.at(inputFile.selectLine(line));
    }
    newIssue.at(primaryLocation);

    newIssue.save();
  }

  private static String getRepositoryKeyForLanguage(String languageKey) {
    return languageKey.toLowerCase() + "-" + BarLintRulesDefinition.KEY;
  }

  @Override
  public String toString() {
    return "BarLintIssuesLoaderSensor";
  }

  private class BarLintError {

    private final String type;
    private final String description;
    private final String filePath;
    private final int line;

    public BarLintError(final String type, final String description, final String filePath, final int line) {
      this.type = type;
      this.description = description;
      this.filePath = filePath;
      this.line = line;
    }

    public String getType() {
      return type;
    }

    public String getDescription() {
      return description;
    }

    public String getFilePath() {
      return filePath;
    }

    public int getLine() {
      return line;
    }

    @Override
    public String toString() {
      StringBuilder s = new StringBuilder();
      s.append(type);
      s.append("|");
      s.append(description);
      s.append("|");
      s.append(filePath);
      s.append("(");
      s.append(line);
      s.append(")");
      return s.toString();
    }
  }

  private class BarLintAnalysisResultsParser {

    public List<BarLintError> parse(final File file) throws XMLStreamException {
      LOGGER.info("Parsing file {}", file.getAbsolutePath());

      // as the goal of this example is not to demonstrate how to parse an xml file we return an hard coded list of FooError

      BarLintError barError1 = new BarLintError("ExampleRule1", "More precise description of the error", "src/MyClass.foo", 5);
      BarLintError barError2 = new BarLintError("ExampleRule2", "More precise description of the error", "src/MyClass.foo", 9);

      return Arrays.asList(barError1, barError2);
    }
  }

}
