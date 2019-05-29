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

import static org.openqa.selenium.json.JsonInputConverter.extractInt;

import org.openqa.selenium.json.JsonInput;

/**
 * Coverage data for a source range.
 */
public class CoverageRange {

  /**
   * JavaScript script source offset for the range start.
   */
  private int startOffset;
  /**
   * JavaScript script source offset for the range end.
   */
  private int endOffset;
  /**
   * Collected execution count of the source range.
   */
  private int count;

  public CoverageRange(int startOffset, int endOffset, int count) {
    this.setStartOffset(startOffset);
    this.setEndOffset(endOffset);
    this.setCount(count);
  }

  public static CoverageRange fronJson(JsonInput input) {
    int startOffset = 0;
    int endOffset = 0;
    int count = 0;
    input.beginObject();
    while (input.hasNext()) {
      switch (input.nextName()) {
        case "startOffset":
          startOffset = extractInt(input);
          break;
        case "endOffset":
          endOffset = extractInt(input);
          break;
        case "count":
          count = extractInt(input);
          break;
        default:
          input.skipValue();
          break;
      }
    }
    input.endObject();
    return new CoverageRange(startOffset, endOffset, count);
  }

  public int getStartOffset() {
    return startOffset;
  }

  public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
  }

  public int getEndOffset() {
    return endOffset;
  }

  public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
