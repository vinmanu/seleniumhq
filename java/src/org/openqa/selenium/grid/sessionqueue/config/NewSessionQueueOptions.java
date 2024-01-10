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

package org.openqa.selenium.grid.sessionqueue.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.config.ConfigException;
import org.openqa.selenium.grid.jmx.JMXHelper;
import org.openqa.selenium.grid.jmx.ManagedAttribute;
import org.openqa.selenium.grid.jmx.ManagedService;
import org.openqa.selenium.grid.server.BaseServerOptions;
import org.openqa.selenium.grid.sessionqueue.NewSessionQueue;

@ManagedService(
    objectName = "org.seleniumhq.grid:type=Config,name=NewSessionQueueConfig",
    description = "New session queue config")
public class NewSessionQueueOptions {

  static final String SESSION_QUEUE_SECTION = "sessionqueue";
  static final int DEFAULT_REQUEST_TIMEOUT = 300;
  static final int DEFAULT_REQUEST_TIMEOUT_PERIOD = 10;
  static final int DEFAULT_RETRY_INTERVAL = 15;
  static final int DEFAULT_BATCH_SIZE = Runtime.getRuntime().availableProcessors() * 3;

  private final Config config;

  public NewSessionQueueOptions(Config config) {
    this.config = config;
    new JMXHelper().register(this);
  }

  public URI getSessionQueueUri() {

    Optional<URI> host =
        config
            .get(SESSION_QUEUE_SECTION, "host")
            .map(
                str -> {
                  try {
                    URI sessionQueueUri = new URI(str);
                    if (sessionQueueUri.getHost() == null || sessionQueueUri.getPort() == -1) {
                      throw new ConfigException(
                          "Undefined host or port in SessionQueue server URI: " + str);
                    }
                    return sessionQueueUri;
                  } catch (URISyntaxException e) {
                    throw new ConfigException(
                        "Session queue server URI is not a valid URI: " + str);
                  }
                });

    if (host.isPresent()) {
      return host.get();
    }

    BaseServerOptions serverOptions = new BaseServerOptions(config);
    String schema = (serverOptions.isSecure() || serverOptions.isSelfSigned()) ? "https" : "http";
    Optional<Integer> port = config.getInt(SESSION_QUEUE_SECTION, "port");
    Optional<String> hostname = config.get(SESSION_QUEUE_SECTION, "hostname");

    if (!(port.isPresent() && hostname.isPresent())) {
      throw new ConfigException("Unable to determine host and port for the session queue server");
    }

    try {
      return new URI(schema, null, hostname.get(), port.get(), "", null, null);
    } catch (URISyntaxException e) {
      throw new ConfigException(
          "Session queue server uri configured through host (%s) and port (%d) is not a valid URI",
          hostname.get(), port.get());
    }
  }

  public Duration getSessionRequestTimeout() {
    // If the user sets 0 or less, we default to 1s.
    int timeout =
        Math.max(
            config
                .getInt(SESSION_QUEUE_SECTION, "session-request-timeout")
                .orElse(DEFAULT_REQUEST_TIMEOUT),
            1);

    return Duration.ofSeconds(timeout);
  }

  public Duration getSessionRequestTimeoutPeriod() {
    // If the user sets 0 or less, we default to 1s.
    int timeout =
        Math.max(
            config
                .getInt(SESSION_QUEUE_SECTION, "session-request-timeout-period")
                .orElse(DEFAULT_REQUEST_TIMEOUT_PERIOD),
            1);

    return Duration.ofSeconds(timeout);
  }

  public Duration getSessionRequestRetryInterval() {
    // If the user sets 0 or less, we default to DEFAULT_RETRY_INTERVAL (in milliseconds).
    int interval =
        Math.max(
            config
                .getInt(SESSION_QUEUE_SECTION, "session-retry-interval")
                .orElse(DEFAULT_RETRY_INTERVAL),
            DEFAULT_RETRY_INTERVAL);
    return Duration.ofMillis(interval);
  }

  public int getBatchSize() {
    // If the user sets 0 or less, we default to 10.
    int batchSize =
        Math.max(
            config
                .getInt(SESSION_QUEUE_SECTION, "sessionqueue-batch-size")
                .orElse(DEFAULT_BATCH_SIZE),
            1);

    return batchSize;
  }

  @ManagedAttribute(name = "RequestTimeoutSeconds")
  public long getRequestTimeoutSeconds() {
    return getSessionRequestTimeout().getSeconds();
  }

  @ManagedAttribute(name = "RetryIntervalMilliseconds")
  public long getRetryIntervalMilliseconds() {
    return getSessionRequestRetryInterval().toMillis();
  }

  public NewSessionQueue getSessionQueue(String implementation) {
    return config.getClass(
        SESSION_QUEUE_SECTION, "implementation", NewSessionQueue.class, implementation);
  }
}
