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

package org.openqa.selenium.support.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Models a SELECT tag, providing helper methods to select and deselect options.
 */
public class Select {

  private final WebElement element;
  private final boolean isMulti;

  /**
   * Constructor. A check is made that the given element is, indeed, a SELECT tag. If it is not,
   * then an UnexpectedTagNameException is thrown.
   *
   * @param element SELECT element to wrap
   * @throws UnexpectedTagNameException when element is not a SELECT
   */
  public Select(WebElement element) {
    String tagName = element.getTagName();

    if (null == tagName || !"select".equals(tagName.toLowerCase())) {
      throw new UnexpectedTagNameException("select", tagName);
    }

    this.element = element;

    String value = element.getAttribute("multiple");

    // The atoms normalize the returned value, but check for "false"
    isMulti = (value != null && !"false".equals(value));
  }

  /**
   * @return Whether this select element support selecting multiple options at the same time? This
   *         is done by checking the value of the "multiple" attribute.
   */
  public boolean isMultiple() {
    return isMulti;
  }

  /**
   * @return All options belonging to this select tag
   */
  public List<WebElement> getOptions() {
    return element.findElements(By.tagName("option"));
  }

  /**
   * @return All selected options belonging to this select tag
   */
  public List<WebElement> getAllSelectedOptions() {
    List<WebElement> toReturn = new ArrayList<>();

    for (WebElement option : getOptions()) {
      if (option.isSelected()) {
        toReturn.add(option);
      }
    }

    return toReturn;
  }

  /**
   * @return The first selected option in this select tag (or the currently selected option in a
   *         normal select)
   * @throws NoSuchElementException If no option is selected
   */
  public WebElement getFirstSelectedOption() {
    for (WebElement option : getOptions()) {
      if (option.isSelected()) {
        return option;
      }
    }

    throw new NoSuchElementException("No options are selected");
  }

  /**
   * Select all options that display text matching the argument. That is, when given "Bar" this
   * would select an option like:
   *
   * &lt;option value="foo"&gt;Bar&lt;/option&gt;
   *
   * @param text The visible text to match against
   * @throws NoSuchElementException If no matching option elements are found
   */
  public void selectByVisibleText(String text) {
    // try to find the option via XPATH ...
    List<WebElement> options =
        element.findElements(By.xpath(".//option[normalize-space(.) = " + Quotes.escape(text) + "]"));

    boolean matched = false;
    for (WebElement option : options) {
      setSelected(option, true);
      if (!isMultiple()) {
        return;
      }
      matched = true;
    }

    if (options.isEmpty() && text.contains(" ")) {
      String subStringWithoutSpace = getLongestSubstringWithoutSpace(text);
      List<WebElement> candidates;
      if ("".equals(subStringWithoutSpace)) {
        // hmm, text is either empty or contains only spaces - get all options ...
        candidates = element.findElements(By.tagName("option"));
      } else {
        // get candidates via XPATH ...
        candidates =
            element.findElements(By.xpath(".//option[contains(., " +
                Quotes.escape(subStringWithoutSpace) + ")]"));
      }
      for (WebElement option : candidates) {
        if (text.equals(option.getText())) {
          setSelected(option, true);
          if (!isMultiple()) {
            return;
          }
          matched = true;
        }
      }
    }

    if (!matched) {
      throw new NoSuchElementException("Cannot locate element with text: " + text);
    }
  }

  private String getLongestSubstringWithoutSpace(String s) {
    String result = "";
    StringTokenizer st = new StringTokenizer(s, " ");
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      if (t.length() > result.length()) {
        result = t;
      }
    }
    return result;
  }

  /**
   * Select the option at the given index. This is done by examining the "index" attribute of an
   * element, and not merely by counting.
   *
   * @param index The option at this index will be selected
   * @throws NoSuchElementException If no matching option elements are found
   */
  public void selectByIndex(int index) {
    String match = String.valueOf(index);

    for (WebElement option : getOptions()) {
      if (match.equals(option.getAttribute("index"))) {
        setSelected(option, true);
        return;
      }
    }
    throw new NoSuchElementException("Cannot locate option with index: " + index);
  }

  /**
   * Select all options that have a value matching the argument. That is, when given "foo" this
   * would select an option like:
   *
   * &lt;option value="foo"&gt;Bar&lt;/option&gt;
   *
   * @param value The value to match against
   * @throws NoSuchElementException If no matching option elements are found
   */
  public void selectByValue(String value) {
    List<WebElement> options = element.findElements(By.xpath(
        ".//option[@value = " + Quotes.escape(value) + "]"));

    boolean matched = false;
    for (WebElement option : options) {
      setSelected(option, true);
      if (!isMultiple()) {
        return;
      }
      matched = true;
    }

    if (!matched) {
      throw new NoSuchElementException("Cannot locate option with value: " + value);
    }
  }

  /**
   * Clear all selected entries. This is only valid when the SELECT supports multiple selections.
   *
   * @throws UnsupportedOperationException If the SELECT does not support multiple selections
   */
  public void deselectAll() {
    if (!isMultiple()) {
      throw new UnsupportedOperationException(
          "You may only deselect all options of a multi-select");
    }

    for (WebElement option : getOptions()) {
      setSelected(option, false);
    }
  }

  /**
   * Deselect all options that have a value matching the argument. That is, when given "foo" this
   * would deselect an option like:
   *
   * &lt;option value="foo"&gt;Bar&lt;/option&gt;
   *
   * @param value The value to match against
   * @throws NoSuchElementException If no matching option elements are found
   */
  public void deselectByValue(String value) {
    List<WebElement> options = element.findElements(By.xpath(
        ".//option[@value = " + Quotes.escape(value) + "]"));
    boolean matched = false;
    for (WebElement option : options) {
      setSelected(option, false);
      if (!isMultiple()) {
        return;
      }
      matched = true;
    }
    if (!matched) {
      throw new NoSuchElementException("Cannot locate option with value: " + value);
    }
  }

  /**
   * Deselect the option at the given index. This is done by examining the "index" attribute of an
   * element, and not merely by counting.
   *
   * @param index The option at this index will be deselected
   * @throws NoSuchElementException If no matching option elements are found
   */
  public void deselectByIndex(int index) {
    String match = String.valueOf(index);
    
    for (WebElement option : getOptions()) {
      if (match.equals(option.getAttribute("index"))) {
        setSelected(option, false);
        return;
      }
    }
    throw new NoSuchElementException("Cannot locate option with index: " + index);
  }

  /**
   * Deselect all options that display text matching the argument. That is, when given "Bar" this
   * would deselect an option like:
   *
   * &lt;option value="foo"&gt;Bar&lt;/option&gt;
   *
   * @param text The visible text to match against
   * @throws NoSuchElementException If no matching option elements are found
   */
  public void deselectByVisibleText(String text) {
    List<WebElement> options = element.findElements(By.xpath(
        ".//option[normalize-space(.) = " + Quotes.escape(text) + "]"));

    boolean matched = false;
    for (WebElement option : options) {
      setSelected(option, false);
      if (!isMultiple()) {
        return;
      }
      matched = true;
    }

    if (!matched) {
      throw new NoSuchElementException("Cannot locate element with text: " + text);
    }
  }

  /**
   * Select or deselect specified option
   *
   * @param option
   *          The option which state needs to be changed
   * @param select
   *          Indicates whether the option needs to be selected (true) or
   *          deselected (false)
   */
  private void setSelected(WebElement option, boolean select) {
    boolean isSelected=option.isSelected();
    if ((!isSelected && select) || (isSelected && !select)) {
      option.click();
    }
  }
}
