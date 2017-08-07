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
package com.ateno.sonarqube.test;

import static java.util.Arrays.asList;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

import com.ateno.sonarqube.test.hooks.DisplayIssuesInScanner;
import com.ateno.sonarqube.test.hooks.DisplayQualityGateStatus;
import com.ateno.sonarqube.test.languages.BarLanguage;
import com.ateno.sonarqube.test.languages.BarQualityProfile;
import com.ateno.sonarqube.test.measures.ComputeSizeAverage;
import com.ateno.sonarqube.test.measures.ComputeSizeRating;
import com.ateno.sonarqube.test.measures.ExampleMetrics;
import com.ateno.sonarqube.test.measures.SetSizeOnFilesSensor;
import com.ateno.sonarqube.test.rules.CreateIssuesOnJavaFilesSensor;
import com.ateno.sonarqube.test.rules.BarLintIssuesLoaderSensor;
import com.ateno.sonarqube.test.rules.BarLintRulesDefinition;
import com.ateno.sonarqube.test.rules.JavaRulesDefinition;
import com.ateno.sonarqube.test.settings.BarLanguageProperties;
import com.ateno.sonarqube.test.settings.HelloWorldProperties;
import com.ateno.sonarqube.test.settings.SayHelloFromScanner;
import com.ateno.sonarqube.test.web.ExampleFooter;
import com.ateno.sonarqube.test.web.ExampleWidget;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */
public class ExamplePlugin implements Plugin {

  @Override
  public void define(Context context) {
    // tutorial on hooks
    // http://docs.sonarqube.org/display/DEV/Adding+Hooks
    context.addExtensions(DisplayIssuesInScanner.class, DisplayQualityGateStatus.class);

    // tutorial on languages
    context.addExtensions(BarLanguage.class, BarQualityProfile.class);
    context.addExtension(BarLanguageProperties.getProperties());

    // tutorial on measures
    context
      .addExtensions(ExampleMetrics.class, SetSizeOnFilesSensor.class, ComputeSizeAverage.class, ComputeSizeRating.class);

    // tutorial on rules
    context.addExtensions(JavaRulesDefinition.class, CreateIssuesOnJavaFilesSensor.class);
    context.addExtensions(BarLintRulesDefinition.class, BarLintIssuesLoaderSensor.class);

    // tutorial on settings
    context
      .addExtensions(HelloWorldProperties.getProperties())
      .addExtension(SayHelloFromScanner.class);

    // tutorial on web extensions
    context.addExtensions(ExampleFooter.class, ExampleWidget.class);

    context.addExtensions(asList(
      PropertyDefinition.builder("ateno.bar.file.suffixes")
        .name("Suffixes BarLint")
        .description("Suffixes supported by BarLint")
        .category("BarLint")
        .defaultValue("")
        .build()));
  }
}
