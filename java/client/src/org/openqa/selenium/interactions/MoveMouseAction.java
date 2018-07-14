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

package org.openqa.selenium.interactions;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.MouseAction;
import org.openqa.selenium.interactions.internal.Locatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Moves the mouse to an element.
 *
 * @deprecated Use {@link Actions#moveToElement(WebElement)}
 */
@Deprecated
public class MoveMouseAction extends MouseAction implements Action {
  public MoveMouseAction(Mouse mouse, Locatable locationProvider) {
    super(mouse, locationProvider);
    if (locationProvider == null) {
      throw new IllegalArgumentException("Must provide a location for a move action.");
    }
  }

  public void perform() {
    mouse.mouseMove(getActionLocation());
  }

  @Override
  public List<Interaction> asInteractions(PointerInput mouse, KeyInput keyboard) {
    List<Interaction> interactions = new ArrayList<>(moveToLocation(mouse));

    return Collections.unmodifiableList(interactions);
  }
}
