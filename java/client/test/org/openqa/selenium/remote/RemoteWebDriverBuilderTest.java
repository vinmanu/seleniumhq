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

package org.openqa.selenium.remote;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.testing.UnitTests;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.openqa.selenium.json.Json.JSON_UTF_8;
import static org.openqa.selenium.json.Json.MAP_TYPE;
import static org.openqa.selenium.remote.BrowserType.CHROME;
import static org.openqa.selenium.remote.BrowserType.FIREFOX;

import com.google.common.collect.ImmutableMap;

@Category(UnitTests.class)
public class RemoteWebDriverBuilderTest {

  private static final SessionId SESSION_ID = new SessionId(UUID.randomUUID());
  private static final HttpResponse CANNED_SESSION_RESPONSE = new HttpResponse()
    .setContent(Contents.asJson(ImmutableMap.of(
      "value", ImmutableMap.of(
        "sessionId", SESSION_ID,
        // Primula is a canned cheese. Boom boom!
        "capabilities", new ImmutableCapabilities("se:cheese", "primula")))));

  @Test
  public void justCallingBuildWithoutSettingAnyOptionsIsAnError() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder();
    assertThatExceptionOfType(SessionNotCreatedException.class).isThrownBy(builder::build);
  }

  @Test
  public void mustSpecifyAtLeastOneSetOfOptions() {
    List<List<Capabilities>> caps = new ArrayList<>();

    RemoteWebDriver.builder().oneOf(new FirefoxOptions())
      .address("http://localhost:34576")
      .connectingWith(config -> req -> {
        caps.add(listCapabilities(req));
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(caps).hasSize(1);
    List<Capabilities> caps0 = caps.get(0);
    assertThat(caps0.get(0).getBrowserName()).isEqualTo(FIREFOX);
  }

  @Test
  public void settingAGlobalCapabilityCountsAsAnOption() {
    AtomicBoolean match = new AtomicBoolean(false);

    RemoteWebDriver.builder().setCapability("se:cheese", "anari")
      .address("http://localhost:34576")
      .connectingWith(config -> req -> {
        listCapabilities(req).stream()
          .map(caps -> "anari".equals(caps.getCapability("se:cheese")))
          .reduce(Boolean::logicalOr)
          .ifPresent(match::set);
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(match.get()).isTrue();
  }

  @Test
  public void shouldForbidGlobalCapabilitiesFromClobberingFirstMatchCapabilities() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder()
      .oneOf(new ImmutableCapabilities("se:cheese", "stinking bishop"))
      .setCapability("se:cheese", "cheddar")
      .address("http://localhost:38746");

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(builder::build);
  }

  @Test
  public void requireAllOptionsAreW3CCompatible() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder()
      .setCapability("cheese", "casu marzu")
      .address("http://localhost:45734");

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(builder::build);
  }

  @Test
  public void shouldRejectOldJsonWireProtocolNames() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder()
      .oneOf(new ImmutableCapabilities("platform", Platform.getCurrent()))
      .address("http://localhost:35856");

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(builder::build);
  }

  @Test
  public void shouldAllowMetaDataToBeSet() {
    AtomicBoolean seen = new AtomicBoolean(false);

    RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .addMetadata("cloud:options", "merhaba")
      .address("http://localhost:34576")
      .connectingWith(config -> req -> {
        Map<String, Object> payload = new Json().toType(Contents.string(req), MAP_TYPE);
        seen.set("merhaba".equals(payload.getOrDefault("cloud:options", "")));
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(seen.get()).isTrue();
  }

  @Test
  public void doesNotAllowFirstMatchToBeUsedAsAMetadataNameAsItIsConfusing() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder();
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> builder.addMetadata("firstMatch", "cheese"));
  }

  @Test
  public void doesNotAllowAlwaysMatchToBeUsedAsAMetadataNameAsItIsConfusing() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder();
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> builder.addMetadata("alwaysMatch", "cheese"));

  }

  @Test
  public void doesNotAllowCapabilitiesToBeUsedAsAMetadataName() {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder();
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> builder.addMetadata("capabilities", "cheese"));
  }

  @Test
  public void shouldAllowCapabilitiesToBeSetGlobally() {
    AtomicBoolean seen = new AtomicBoolean(false);

    RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .setCapability("se:option", "cheese")
      .address("http://localhost:34576")
      .connectingWith(config -> req -> {
        listCapabilities(req).stream()
          .map(caps -> "cheese".equals(caps.getCapability("se:option")))
          .reduce(Boolean::logicalAnd)
          .ifPresent(seen::set);
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(seen.get()).isTrue();
  }

  @Test
  public void ifARemoteUrlIsGivenThatIsUsedForTheSession() {
    URI uri = URI.create("http://localhost:7575");
    AtomicReference<URI> seen = new AtomicReference<>();

    RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .address(uri)
      .connectingWith(config -> req -> {
        seen.set(config.baseUri());
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(seen.get()).isEqualTo(uri);
  }

  @Test
  public void shouldUseGivenDriverServiceForUrlIfProvided() throws IOException {
    URI uri = URI.create("http://localhost:9898");
    URL url = uri.toURL();

    DriverService service = new FakeDriverService() {
      @Override
      public URL getUrl() {
        return url;
      }
    };

    AtomicReference<URI> seen = new AtomicReference<>();
    RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .withDriverService(service)
      .connectingWith(config -> {
        seen.set(config.baseUri());
        return req -> CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(seen.get()).isEqualTo(uri);
  }

  @Test
  public void settingBothDriverServiceAndUrlIsAnError() throws IOException {
    RemoteWebDriverBuilder builder = RemoteWebDriver.builder()
        .withDriverService(new FakeDriverService());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> builder.address("http://localhost:89789"));
  }

  @Test
  public void oneOfWillClearOutTheCurrentlySetCapabilities() {
    AtomicBoolean allOk = new AtomicBoolean();

    RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .addAlternative(new InternetExplorerOptions())
      .oneOf(new ChromeOptions())
      .address("http://localhost:34576")
      .connectingWith(config -> req -> {
        List<Capabilities> caps = listCapabilities(req);
        allOk.set(caps.size() == 1 && caps.get(0).getBrowserName().equals(CHROME));
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(allOk.get()).isTrue();
  }

  @Test
  public void shouldBeAbleToSetClientConfigDirectly() {
    URI uri = URI.create("http://localhost:5763");

    AtomicReference<URI> seen = new AtomicReference<>();

    RemoteWebDriver.builder()
      .address(uri.toString())
      .oneOf(new FirefoxOptions())
      .connectingWith(config -> {
        seen.set(config.baseUri());
        return req -> CANNED_SESSION_RESPONSE;
      })
    .build();

    assertThat(seen.get()).isEqualTo(uri);
  }

  @Test
  public void shouldSetRemoteHostUriOnClientConfigIfSet() {
    URI uri = URI.create("http://localhost:6546");
    ClientConfig config = ClientConfig.defaultConfig().baseUri(uri);

    AtomicReference<URI> seen = new AtomicReference<>();

    RemoteWebDriver.builder()
      .config(config)
      .oneOf(new FirefoxOptions())
      .connectingWith(c -> {
        seen.set(c.baseUri());
        return req -> CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(seen.get()).isEqualTo(uri);
  }

  @Test
  public void shouldSetSessionIdFromW3CResponse() {
    RemoteWebDriver driver = (RemoteWebDriver) RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .address("http://localhost:34576")
      .connectingWith(config -> req -> CANNED_SESSION_RESPONSE)
      .build();

    assertThat(driver.getSessionId()).isEqualTo(SESSION_ID);
  }

  @Test
  public void commandsShouldBeSentWithW3CHeaders() {
    AtomicBoolean allOk = new AtomicBoolean(false);

    RemoteWebDriver.builder()
      .oneOf(new FirefoxOptions())
      .address("http://localhost:34576")
      .connectingWith(config -> req -> {
        allOk.set("no-cache".equals(req.getHeader("Cache-Control")) && JSON_UTF_8.equals(req.getHeader("Content-Type")));
        return CANNED_SESSION_RESPONSE;
      })
      .build();

    assertThat(allOk.get()).isTrue();
  }

  @Test
  public void shouldUseWebDriverInfoToFindAMatchingDriverImplementationForRequestedCapabilitiesIfRemoteUrlNotSet() {
  }

  @Test
  public void shouldAugmentDriverIfPossible() {
  }

  @SuppressWarnings("unchecked")
  private List<Capabilities> listCapabilities(HttpRequest request) {
    Map<String, Object> converted = new Json().toType(Contents.string(request), MAP_TYPE);
    Map<String, Object> w3cCaps = (Map<String, Object>) converted.get("capabilities");
    Map<String, Object> always = (Map<String, Object>) w3cCaps.getOrDefault("alwaysMatch", emptyMap());
    Capabilities alwaysMatch = new ImmutableCapabilities(always);
    List<Map<String, Object>> first = (List<Map<String, Object>>) w3cCaps
      .getOrDefault("firstMatch", singletonList(emptyMap()));

    return first.stream()
      .map(ImmutableCapabilities::new)
      .map(alwaysMatch::merge)
      .collect(Collectors.toList());
  }

  static class FakeDriverService extends DriverService {
    private boolean started;

    FakeDriverService() throws IOException {
      super(new File("."), 0, DEFAULT_TIMEOUT, null, null);
    }

    @Override
    public void start() {
      started = true;
    }

    @Override
    public boolean isRunning() {
      return started;
    }

    @Override
    protected void waitUntilAvailable() {
      // return immediately
    }
  }
}
