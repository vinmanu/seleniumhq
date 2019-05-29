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

package org.openqa.selenium.devtools.profiler.model;

import java.util.Objects;
import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.json.JsonInputConverter;

/**
 * Specifies a number of samples attributed to a certain source position.
 */
public class PositionTickInfo {

  /**
   * Source line number (1-based).
   */
  private int line;
  /**
   * Number of samples attributed to the source line.
   */
  private int ticks;

  public PositionTickInfo(int line, int ticks) {
    this.setLine(line);
    this.setTicks(ticks);
  }

  public static PositionTickInfo fromJson(JsonInput input) {
    int line = JsonInputConverter.extractInt(input);
    int ticks = 0;
    while (input.hasNext()) {
      switch (input.nextName()) {
        case "ticks":
          ticks = JsonInputConverter.extractInt(input);
          break;
        default:
          input.skipValue();
          break;
      }
    }
    return new PositionTickInfo(line, ticks);
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getTicks() {
    return ticks;
  }

  public void setTicks(int ticks) {
    this.ticks = ticks;
  }

  @Override
  public boolean equals(Object obj) {
    Objects.requireNonNull(obj, "obj is mandatory for equals method");
    return this.getLine() == ((PositionTickInfo) obj).getLine()
      && this.getTicks() == ((PositionTickInfo) obj).getTicks();
  }
}
