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

package org.openqa.selenium.remote.http;

import org.openqa.selenium.internal.Require;

import java.io.Closeable;
import java.net.URL;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.openqa.selenium.remote.http.ClientConfig.defaultConfig;

/**
 * Defines a simple client for making HTTP requests.
 */
public interface HttpClient extends Closeable, HttpHandler {

  WebSocket openSocket(HttpRequest request, WebSocket.Listener listener);

  default void close() {}

  interface Factory {

    /**
     * Creates a new instance of {@link HttpClient.Factory} with the given name. It uses
     * {@link ServiceLoader} to find all available implementations and selects the class
     * that has an {@link @HttpClientName} annotation with the given name as the value.
     *
     * @throws IllegalArgumentException if no implementation with the given name can be found
     * @throws IllegalStateException if more than one implementation with the given name can be found
     */
    static Factory create(String name) {
      try {
        Class<Factory> factory;
        ClassLoader classLoader = (Factory.class.getClassLoader() == null) ?
          ClassLoader.getSystemClassLoader() : Factory.class.getClassLoader();

        if ("netty".equals(name)) {
          factory =
            (Class<Factory>) classLoader.loadClass(
              "org.openqa.selenium.remote.http.netty.NettyClient$Factory");
        } else {
          factory =
            (Class<Factory>) classLoader.loadClass(
              "org.openqa.selenium.remote.http.jdk.JdkHttpClient$Factory");
        }
        return factory.newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Use the {@code webdriver.http.factory} system property to determine which implementation of
     * {@link HttpClient.Factory} should be used.
     *
     * {@see create}
     */
    static Factory createDefault() {
      String httpFactory = "netty";
      String javaVersion = System.getProperty("java.version");
      if (!javaVersion.trim().isEmpty()) {
        int dot = javaVersion.indexOf(".");
        if (dot != -1) {
          javaVersion = javaVersion.substring(0, dot);
        }
        if (Integer.parseInt(javaVersion) >= 11) {
          httpFactory = "jdk-http-client";
        }
      }
      return create(System.getProperty("webdriver.http.factory", httpFactory));
    }

    /**
     * Creates a HTTP client that will send requests to the given URL.
     *
     * @param url URL The base URL for requests.
     */
    default HttpClient createClient(URL url) {
      Require.nonNull("URL to use as base URL", url);
      return createClient(defaultConfig().baseUrl(url));
    }

    HttpClient createClient(ClientConfig config);

    /**
     * Closes idle clients.
     */
    default void cleanupIdleClients() {
      // do nothing by default.
    }
  }
}
