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

package org.openqa.selenium.bidi;

import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.HttpClient;

import java.net.URI;
import java.util.Optional;

public interface HasBiDi {
  Optional<BiDi> maybeGetBiDi();

  void setBiDi(BiDi bidi);

  default BiDi getBiDi() {
    return maybeGetBiDi()
        .orElseThrow(() -> new BiDiException("Unable to create a BiDi connection"));
  }

  default Optional<BiDi> createBiDi(Optional<URI> biDiUri) {
    if (!biDiUri.isPresent()) {
      return Optional.empty();
    }

    URI wsUri =
        biDiUri.orElseThrow(
            () ->
                new BiDiException("This version of Firefox or geckodriver does not support BiDi"));

    HttpClient.Factory clientFactory = HttpClient.Factory.createDefault();
    ClientConfig wsConfig = ClientConfig.defaultConfig().baseUri(wsUri);
    HttpClient wsClient = clientFactory.createClient(wsConfig);

    org.openqa.selenium.bidi.Connection biDiConnection =
        new org.openqa.selenium.bidi.Connection(wsClient, wsUri.toString());

    return Optional.of(new BiDi(biDiConnection));
  }
}
