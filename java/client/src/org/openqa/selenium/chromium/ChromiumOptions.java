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

package org.openqa.selenium.chromium;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Class to manage options specific to {@link ChromiumDriver}.
 *
 * @since Since chromedriver v17.0.963.0
 */
public class ChromiumOptions extends AbstractDriverOptions<ChromiumOptions> {

  /**
   * Key used to store a set of ChromiumOptions in a {@link Capabilities}
   * object.
   */
  public static final String CAPABILITY = "goog:chromeOptions";

  private String binary;
  private List<String> args = new ArrayList<>();
  private List<File> extensionFiles = new ArrayList<>();
  private List<String> extensions = new ArrayList<>();
  private Map<String, Object> experimentalOptions = new HashMap<>();

  @Override
  public ChromiumOptions merge(Capabilities extraCapabilities) {
    super.merge(extraCapabilities);
    return this;
  }

  /**
   * Sets the path to the browser executable. This path should exist on the
   * machine which will launch browser. The path should either be absolute or
   * relative to the location of running ChromiumDriver server.
   *
   * @param path Path to browser executable.
   */
  public ChromiumOptions setBinary(File path) {
    binary = checkNotNull(path).getPath();
    return this;
  }

  /**
   * Sets the path to the browser executable. This path should exist on the
   * machine which will launch browser. The path should either be absolute or
   * relative to the location of running ChromiumDriver server.
   *
   * @param path Path to browser executable.
   */
  public ChromiumOptions setBinary(String path) {
    binary = checkNotNull(path);
    return this;
  }

  /**
   * @param arguments The arguments to use when starting browser.
   * @see #addArguments(java.util.List)
   */
  public ChromiumOptions addArguments(String... arguments) {
    addArguments(ImmutableList.copyOf(arguments));
    return this;
  }

  /**
   * Adds additional command line arguments to be used when starting browser.
   * For example:
   * <pre><code>
   *   options.setArguments(
   *       "load-extension=/path/to/unpacked_extension",
   *       "allow-outdated-plugins");
   * </code></pre>
   *
   * <p>Each argument may contain an option "--" prefix: "--foo" or "foo".
   * Arguments with an associated value should be delimited with an "=":
   * "foo=bar".
   *
   * @param arguments The arguments to use when starting browser.
   */
  public ChromiumOptions addArguments(List<String> arguments) {
    args.addAll(arguments);
    return this;
  }

  /**
   * @param paths Paths to the extensions to install.
   * @see #addExtensions(java.util.List)
   */
  public ChromiumOptions addExtensions(File... paths) {
    addExtensions(ImmutableList.copyOf(paths));
    return this;
  }

  /**
   * Adds a new Chrome extension to install on browser startup. Each path should
   * specify a packed Chrome extension (CRX file).
   *
   * @param paths Paths to the extensions to install.
   */
  public ChromiumOptions addExtensions(List<File> paths) {
    for (File path : paths) {
      checkNotNull(path);
      checkArgument(path.exists(), "%s does not exist", path.getAbsolutePath());
      checkArgument(!path.isDirectory(), "%s is a directory",
          path.getAbsolutePath());
    }
    extensionFiles.addAll(paths);
    return this;
  }

  /**
   * @param encoded Base64 encoded data of the extensions to install.
   * @see #addEncodedExtensions(java.util.List)
   */
  public ChromiumOptions addEncodedExtensions(String... encoded) {
    addEncodedExtensions(ImmutableList.copyOf(encoded));
    return this;
  }

  /**
   * Adds a new Chrome extension to install on browser startup. Each string data should
   * specify a Base64 encoded string of packed Chrome extension (CRX file).
   *
   * @param encoded Base64 encoded data of the extensions to install.
   */
  public ChromiumOptions addEncodedExtensions(List<String> encoded) {
    for (String extension : encoded) {
      checkNotNull(extension);
    }
    extensions.addAll(encoded);
    return this;
  }

  /**
   * Sets an experimental option. Useful for new ChromiumDriver options not yet
   * exposed through the {@link ChromiumOptions} API.
   *
   * @param name Name of the experimental option.
   * @param value Value of the experimental option, which must be convertible
   *     to JSON.
   */
  public ChromiumOptions setExperimentalOption(String name, Object value) {
    experimentalOptions.put(checkNotNull(name), value);
    return this;
  }

  public ChromiumOptions setHeadless(boolean headless) {
    args.remove("--headless");
    if (headless) {
      args.add("--headless");
    }
    return this;
  }

  @Override
  protected int amendHashCode() {
    return Objects.hash(
        args,
        binary,
        experimentalOptions,
        extensionFiles,
        extensions);
  }

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new TreeMap<>(super.asMap());

    Map<String, Object> options = new TreeMap<>();
    experimentalOptions.forEach(options::put);

    if (binary != null) {
      options.put("binary", binary);
    }

    options.put("args", ImmutableList.copyOf(args));

    options.put(
        "extensions",
        Stream.concat(
            extensionFiles.stream()
                .map(file -> {
                  try {
                    return Base64.getEncoder().encodeToString(Files.toByteArray(file));
                  } catch (IOException e) {
                    throw new SessionNotCreatedException(e.getMessage(), e);
                  }
                }),
            extensions.stream()
        ).collect(ImmutableList.toImmutableList()));

    toReturn.put(CAPABILITY, options);

    return Collections.unmodifiableMap(toReturn);
  }
}
