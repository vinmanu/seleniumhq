// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.testing;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.testing.drivers.Browser;

/** Class that decides whether a test class or method should be ignored. */
class TestIgnorance {

  private final IgnoreComparator ignoreComparator = new IgnoreComparator();
  private final Set<String> methods = new HashSet<>();
  private final Set<String> only = new HashSet<>();
  private final Set<String> ignoreMethods = new HashSet<>();
  private final Set<String> ignoreClasses = new HashSet<>();

  public TestIgnorance(Browser driver) {
    ignoreComparator.addDriver(
        Require.argument("Driver", driver)
            .nonNull(
                "Browser to use must be set. Do this by setting the 'selenium.browser' system"
                    + " property"));

    String onlyRun = System.getProperty("only_run");
    if (onlyRun != null) {
      only.addAll(Arrays.asList(onlyRun.split(",")));
    }

    String method = System.getProperty("method");
    if (method != null) {
      methods.addAll(Arrays.asList(method.split(",")));
    }

    String ignoreClass = System.getProperty("ignore_class");
    if (ignoreClass != null) {
      ignoreClasses.addAll(Arrays.asList(ignoreClass.split(",")));
    }

    String skip = System.getProperty("ignore_method");
    if (skip != null) {
      ignoreMethods.addAll(Arrays.asList(skip.split(",")));
    }
  }

  public boolean isIgnored(ExtensionContext extensionContext) {
    Optional<Class<?>> testClass = extensionContext.getTestClass();
    Optional<Method> testMethod = extensionContext.getTestMethod();

    // Ignored because of Selenium's custom extensions
    Optional<IgnoreList> ignoreListClass = findAnnotation(testClass, IgnoreList.class);
    List<Ignore> ignoreClass = findRepeatableAnnotations(testClass, Ignore.class);
    Optional<IgnoreList> ignoreListMethod = findAnnotation(testMethod, IgnoreList.class);
    List<Ignore> ignoreMethod = findRepeatableAnnotations(testMethod, Ignore.class);
    boolean ignored =
        (ignoreListClass.isPresent() && ignoreComparator.shouldIgnore(ignoreListClass.get()))
            || (!ignoreClass.isEmpty() && ignoreComparator.shouldIgnore(ignoreClass.stream()))
            || (ignoreListMethod.isPresent()
                && ignoreComparator.shouldIgnore(ignoreListMethod.get()))
            || (!ignoreMethod.isEmpty() && ignoreComparator.shouldIgnore(ignoreMethod.stream()));

    // Ignored because of Jupiter's @Disabled
    ignored |= findAnnotation(testClass, Disabled.class).isPresent();
    ignored |= findAnnotation(testMethod, Disabled.class).isPresent();

    // Ignored because of environment variables
    if (Boolean.getBoolean("ignored_only")) {
      ignored = !ignored;
    }
    if (testClass.isPresent() && testMethod.isPresent()) {
      ignored |= isIgnoredDueToEnvironmentVariables(testClass.get(), testMethod.get());
    }

    return ignored;
  }

  private boolean isIgnoredDueToEnvironmentVariables(Class<?> testClass, Method testMethod) {
    return (!only.isEmpty() && !only.contains(testClass.getSimpleName()))
        || (!methods.isEmpty() && !methods.contains(testMethod.getName()))
        || ignoreClasses.contains(testClass.getSimpleName())
        || ignoreMethods.contains(testMethod.getName());
  }
}
