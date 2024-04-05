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

package org.openqa.selenium.bidi.browsingcontext;

public enum ReadinessState {
  // Mapping the page load strategy values used in BiDi To Classic
  // Refer: https://w3c.github.io/webdriver-bidi/#type-browsingContext-ReadinessState
  // Refer: https://www.w3.org/TR/webdriver2/#navigation
  NONE("none", "none"),
  INTERACTIVE("interactive", "eager"),
  COMPLETE("complete", "normal");

  private final String readinessState;

  private final String pageLoadStrategy;

  ReadinessState(String readinessState, String pageLoadStrategy) {
    this.readinessState = readinessState;
    this.pageLoadStrategy = pageLoadStrategy;
  }

  public String getPageLoadStrategy() {
    return pageLoadStrategy;
  }

  public static ReadinessState getReadinessState(String pageLoadStrategy) {
    if (pageLoadStrategy != null) {
      for (ReadinessState b : ReadinessState.values()) {
        if (pageLoadStrategy.equalsIgnoreCase(b.pageLoadStrategy)) {
          return b;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return String.valueOf(readinessState);
  }
}
