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

package org.openqa.selenium.support.events;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StubDriver;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.internal.WrapsElement;

import com.google.common.collect.ImmutableMap;

/**
 * @author Michael Tamm
 */
@RunWith(JUnit4.class)
public class EventFiringWebDriverTest {

  @Test
  public void alertEvents() {
    final WebDriver mockedDriver = mock(WebDriver.class);
    final Alert mockedAlert = mock(Alert.class);
    final WebDriver.TargetLocator mockedTargetLocator = mock(WebDriver.TargetLocator.class);

    when(mockedDriver.switchTo()).thenReturn(mockedTargetLocator);
    when(mockedTargetLocator.alert()).thenReturn(mockedAlert);

    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    testedDriver.switchTo().alert().accept();
    testedDriver.switchTo().alert().dismiss();

    InOrder order = Mockito.inOrder(mockedDriver, mockedAlert, listener);
    order.verify(mockedDriver).switchTo();
    order.verify(listener).beforeAlertAccept(any(WebDriver.class));
    order.verify(mockedAlert).accept();
    order.verify(listener).afterAlertAccept(any(WebDriver.class));
    order.verify(mockedDriver).switchTo();
    order.verify(listener).beforeAlertDismiss(any(WebDriver.class));
    order.verify(mockedAlert).dismiss();
    order.verify(listener).afterAlertDismiss(any(WebDriver.class));
    verifyNoMoreInteractions(mockedDriver, mockedAlert, listener);
  }

  @Test
  public void navigationEvents() {
    final WebDriver mockedDriver = mock(WebDriver.class);
    final Navigation mockedNavigation = mock(Navigation.class);

    when(mockedDriver.navigate()).thenReturn(mockedNavigation);

    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    testedDriver.get("http://www.get.com");
    testedDriver.navigate().to("http://www.navigate-to.com");
    testedDriver.navigate().back();
    testedDriver.navigate().forward();
    testedDriver.navigate().refresh();

    InOrder order = Mockito.inOrder(mockedDriver, mockedNavigation, listener);
    order.verify(listener).beforeNavigateTo(eq("http://www.get.com"), any(WebDriver.class));
    order.verify(mockedDriver).get("http://www.get.com");
    order.verify(listener).afterNavigateTo(eq("http://www.get.com"), any(WebDriver.class));
    order.verify(mockedDriver).navigate();
    order.verify(listener).beforeNavigateTo(eq("http://www.navigate-to.com"), any(WebDriver.class));
    order.verify(mockedNavigation).to("http://www.navigate-to.com");
    order.verify(listener).afterNavigateTo(eq("http://www.navigate-to.com"), any(WebDriver.class));
    order.verify(mockedDriver).navigate();
    order.verify(listener).beforeNavigateBack(any(WebDriver.class));
    order.verify(mockedNavigation).back();
    order.verify(listener).afterNavigateBack(any(WebDriver.class));
    order.verify(mockedDriver).navigate();
    order.verify(listener).beforeNavigateForward(any(WebDriver.class));
    order.verify(mockedNavigation).forward();
    order.verify(listener).afterNavigateForward(any(WebDriver.class));
    order.verify(mockedDriver).navigate();
    order.verify(listener).beforeNavigateRefresh(any(WebDriver.class));
    order.verify(mockedNavigation).refresh();
    order.verify(listener).afterNavigateRefresh(any(WebDriver.class));
    verifyNoMoreInteractions(mockedDriver, mockedNavigation, listener);
  }

  @Test
  public void clickEvent() {
    final WebDriver mockedDriver = mock(WebDriver.class);
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo"))).thenReturn(mockedElement);

    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    testedDriver.findElement(By.name("foo")).click();

    InOrder order = Mockito.inOrder(mockedDriver, mockedElement, listener);
    order.verify(listener).beforeFindBy(eq(By.name("foo")), eq(null), any(WebDriver.class));
    order.verify(mockedDriver).findElement(By.name("foo"));
    order.verify(listener).afterFindBy(eq(By.name("foo")), eq(null), any(WebDriver.class));
    order.verify(listener).beforeClickOn(any(WebElement.class), any(WebDriver.class));
    order.verify(mockedElement).click();
    order.verify(listener).afterClickOn(any(WebElement.class), any(WebDriver.class));
    verifyNoMoreInteractions(mockedDriver, mockedElement, listener);
  }

  @Test
  public void windowEvent() {
    String windowName = "Window name";
    WebDriver mockedDriver = mock(WebDriver.class);
    TargetLocator mockedTargetLocator = mock(TargetLocator.class);
    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    when(mockedDriver.switchTo()).thenReturn(mockedTargetLocator);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    testedDriver.switchTo().window(windowName);

    InOrder order = Mockito.inOrder(mockedTargetLocator, listener);
    order.verify(listener).beforeSwitchToWindow(eq(windowName), any(WebDriver.class));
    order.verify(mockedTargetLocator).window(windowName);
    order.verify(listener).afterSwitchToWindow(eq(windowName), any(WebDriver.class));
    verifyNoMoreInteractions(mockedTargetLocator, listener);
  }
  
  @Test
  public void getScreenshotAs() {
	final String DATA = "data";
	WebDriver mockedDriver = mock(WebDriver.class, withSettings().extraInterfaces(TakesScreenshot.class));
    WebDriverEventListener listener = mock(WebDriverEventListener.class);
    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);
    
    Mockito.doReturn(DATA).when((TakesScreenshot)mockedDriver).getScreenshotAs(OutputType.BASE64);
    
    String screenshot = ((TakesScreenshot)testedDriver).getScreenshotAs(OutputType.BASE64);
    assertTrue(screenshot.equals(DATA));
    
    InOrder order = Mockito.inOrder(mockedDriver, listener);
    order.verify(listener).beforeGetScreenshotAs(OutputType.BASE64);
    order.verify((TakesScreenshot)mockedDriver).getScreenshotAs(OutputType.BASE64);
    order.verify(listener).afterGetScreenshotAs(OutputType.BASE64, screenshot);
    verifyNoMoreInteractions(mockedDriver, listener);
  
  }

  @Test
  public void changeValueEvent() {
    final WebDriver mockedDriver = mock(WebDriver.class);
    final WebElement mockedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.name("foo"))).thenReturn(mockedElement);

    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    String someText = "some text";

    testedDriver.findElement(By.name("foo")).clear();
    testedDriver.findElement(By.name("foo")).sendKeys(someText);

    InOrder order = Mockito.inOrder(mockedElement, listener);
    order.verify(listener).beforeChangeValueOf(any(WebElement.class), any(WebDriver.class), eq(null));
    order.verify(mockedElement).clear();
    order.verify(listener).afterChangeValueOf(any(WebElement.class), any(WebDriver.class), eq(null));
    order.verify(listener).beforeChangeValueOf(any(WebElement.class), any(WebDriver.class), eq(new CharSequence[]{someText}));
    order.verify(mockedElement).sendKeys(someText);
    order.verify(listener).afterChangeValueOf(any(WebElement.class), any(WebDriver.class), eq(new CharSequence[]{someText}));

    verify(mockedDriver, times(2)).findElement(By.name("foo"));
    verify(listener, times(2)).beforeFindBy(eq(By.name("foo")), eq(null), any(WebDriver.class));
    verify(listener, times(2)).afterFindBy(eq(By.name("foo")), eq(null), any(WebDriver.class));
    verifyNoMoreInteractions(mockedDriver, mockedElement, listener);
  }

  @Test
  public void findByEvent() {
    final WebDriver mockedDriver = mock(WebDriver.class);
    final WebElement mockedElement = mock(WebElement.class);
    final WebElement mockedChildElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.id("foo"))).thenReturn(mockedElement);
    when(mockedElement.findElement(any())).thenReturn(mockedChildElement);

    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    WebElement element = testedDriver.findElement(By.id("foo"));
    element.findElement(By.linkText("bar"));
    element.findElements(By.name("xyz"));
    testedDriver.findElements(By.xpath("//link[@type = 'text/css']"));

    InOrder order = Mockito.inOrder(mockedElement, mockedDriver, listener);
    verify(listener).beforeFindBy(eq(By.id("foo")), eq(null), any(WebDriver.class));
    order.verify(mockedDriver).findElement(By.id("foo"));
    verify(listener).afterFindBy(eq(By.id("foo")), eq(null), any(WebDriver.class));
    verify(listener).beforeFindBy(eq(By.linkText("bar")), any(WebElement.class), any(WebDriver.class));
    order.verify(mockedElement).findElement(By.linkText("bar"));
    verify(listener).afterFindBy(eq(By.linkText("bar")), any(WebElement.class), any(WebDriver.class));
    verify(listener).beforeFindBy(eq(By.name("xyz")), any(WebElement.class), any(WebDriver.class));
    order.verify(mockedElement).findElements(By.name("xyz"));
    verify(listener).afterFindBy(eq(By.name("xyz")), any(WebElement.class), any(WebDriver.class));
    verify(listener).beforeFindBy(eq(By.xpath("//link[@type = 'text/css']")), eq(null), any(WebDriver.class));
    order.verify(mockedDriver).findElements(By.xpath("//link[@type = 'text/css']"));
    verify(listener).afterFindBy(eq(By.xpath("//link[@type = 'text/css']")), eq(null), any(WebDriver.class));
    verifyNoMoreInteractions(mockedElement, mockedDriver, listener);
  }

  @Test
  public void shouldCallListenersWhenAnExceptionIsThrown() {
    final WebDriver mockedDriver = mock(WebDriver.class);

    final NoSuchElementException exception = new NoSuchElementException("argh");

    when(mockedDriver.findElement(By.id("foo"))).thenThrow(exception);

    WebDriverEventListener listener = mock(WebDriverEventListener.class);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(listener);

    try {
      testedDriver.findElement(By.id("foo"));
      fail("Expected exception to be propagated");
    } catch (NoSuchElementException e) {
      // Fine
    }

    InOrder order = Mockito.inOrder(mockedDriver, listener);
    order.verify(listener).beforeFindBy(eq(By.id("foo")), eq(null), any(WebDriver.class));
    order.verify(mockedDriver).findElement(By.id("foo"));
    order.verify(listener).onException(any(NoSuchElementException.class), any(WebDriver.class));
    verifyNoMoreInteractions(mockedDriver, listener);
  }

  @Test
  public void shouldUnpackElementArgsWhenCallingScripts() {
    final WebDriver mockedDriver = mock(WebDriver.class,
                                        withSettings().extraInterfaces(JavascriptExecutor.class));
    final WebElement stubbedElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.id("foo"))).thenReturn(stubbedElement);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);
    testedDriver.register(new AbstractWebDriverEventListener() {});

    WebElement element = testedDriver.findElement(By.id("foo"));
    testedDriver.executeScript("foo", element);
    verify((JavascriptExecutor) mockedDriver).executeScript("foo", element);
  }

  @Test
  public void shouldWrapElementFoundWhenCallingScripts() {
    final WebDriver mockedDriver = mock(WebDriver.class,
                                        withSettings().extraInterfaces(JavascriptExecutor.class));
    final WebElement stubbedElement = mock(WebElement.class);

    when(((JavascriptExecutor) mockedDriver).executeScript("foo"))
        .thenReturn(stubbedElement);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);

    Object res = testedDriver.executeScript("foo");
    verify((JavascriptExecutor) mockedDriver).executeScript("foo");
    assertThat(res, instanceOf(WebElement.class));
    assertThat(res, instanceOf(WrapsElement.class));
    assertSame(stubbedElement, ((WrapsElement) res).getWrappedElement());
  }

  @Test
  public void testShouldUnpackListOfElementArgsWhenCallingScripts() {
    final WebDriver mockedDriver = mock(WebDriver.class,
                                        withSettings().extraInterfaces(JavascriptExecutor.class));
    final WebElement mockElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.id("foo"))).thenReturn(mockElement);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);
    testedDriver.register(new AbstractWebDriverEventListener() {});

    final WebElement foundElement = testedDriver.findElement(By.id("foo"));
    assertTrue(foundElement instanceof WrapsElement);
    assertSame(mockElement, ((WrapsElement) foundElement).getWrappedElement());

    List<Object> args = Arrays.asList("before", foundElement, "after");

    testedDriver.executeScript("foo", args);

    verify((JavascriptExecutor) mockedDriver).executeScript("foo", args);
  }

  @Test
  public void shouldWrapMultipleElementsFoundWhenCallingScripts() {
    final WebDriver mockedDriver = mock(WebDriver.class,
                                        withSettings().extraInterfaces(JavascriptExecutor.class));
    final WebElement stubbedElement1 = mock(WebElement.class);
    final WebElement stubbedElement2 = mock(WebElement.class);

    when(((JavascriptExecutor) mockedDriver).executeScript("foo"))
        .thenReturn(Arrays.asList(stubbedElement1, stubbedElement2));

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);

    Object res = testedDriver.executeScript("foo");
    verify((JavascriptExecutor) mockedDriver).executeScript("foo");
    assertThat(res, instanceOf(List.class));
    List<Object> resList = (List<Object>) res;
    resList.stream().forEach(el -> assertTrue(el instanceof WrapsElement));
    assertSame(stubbedElement1, ((WrapsElement) resList.get(0)).getWrappedElement());
    assertSame(stubbedElement2, ((WrapsElement) resList.get(1)).getWrappedElement());
  }

  @Test
  public void shouldWrapMapsWithNullValues() {
    Map<String, Object> map = new HashMap<>();
    map.put("a", null);
    final WebDriver mockedDriver = mock(WebDriver.class,
                                        withSettings().extraInterfaces(JavascriptExecutor.class));
    when(((JavascriptExecutor) mockedDriver).executeScript("foo")).thenReturn(map);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);

    Object res = testedDriver.executeScript("foo");
    verify((JavascriptExecutor) mockedDriver).executeScript("foo");
    assertThat(res, instanceOf(Map.class));
    assertThat(((Map<String, Object>) res).get("a"), is(nullValue()));
  }

  @Test
  public void testShouldUnpackMapOfElementArgsWhenCallingScripts() {
    final WebDriver mockedDriver = mock(WebDriver.class,
                                        withSettings().extraInterfaces(JavascriptExecutor.class));
    final WebElement mockElement = mock(WebElement.class);

    when(mockedDriver.findElement(By.id("foo"))).thenReturn(mockElement);

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);
    testedDriver.register(mock(WebDriverEventListener.class));

    final WebElement foundElement = testedDriver.findElement(By.id("foo"));
    assertTrue(foundElement instanceof WrapsElement);
    assertSame(mockElement, ((WrapsElement) foundElement).getWrappedElement());

    ImmutableMap<String, Object> args = ImmutableMap.of(
        "foo", "bar",
        "element", foundElement,
        "nested", Arrays.asList("before", foundElement, "after")
    );

    testedDriver.executeScript("foo", args);

    verify((JavascriptExecutor) mockedDriver).executeScript("foo", args);
  }

  @Test
  public void shouldBeAbleToWrapSubclassesOfSomethingImplementingTheWebDriverInterface() {
    try {
      new EventFiringWebDriver(new ChildDriver());
      // We should get this far
    } catch (ClassCastException e) {
      e.printStackTrace();
      fail("Should have been able to wrap the child of a webdriver implementing interface");
    }
  }

  @Test
  public void shouldBeAbleToAccessWrappedInstanceFromEventCalls() {
    final WebDriver stub = mock(WebDriver.class);
    EventFiringWebDriver driver = new EventFiringWebDriver(stub);
    WebDriver wrapped = driver.getWrappedDriver();
    assertEquals(stub, wrapped);

    class MyListener extends AbstractWebDriverEventListener {
      @Override
      public void beforeNavigateTo(String url, WebDriver driver) {
        WebDriver unwrapped = ((WrapsDriver) driver).getWrappedDriver();

        assertEquals(stub, unwrapped);
      }
    }

    driver.register(new MyListener());

    driver.get("http://example.org");
  }

  @Test
  public void shouldBeAbleToAccessWrappedElementInstanceFromEventCalls() {
    final WebElement stubElement = mock(WebElement.class);

    final WebDriver stubDriver = mock(WebDriver.class);
    when(stubDriver.findElement(By.name("stub"))).thenReturn(stubElement);

    EventFiringWebDriver driver = new EventFiringWebDriver(stubDriver);

    class MyListener extends AbstractWebDriverEventListener {
      @Override
      public void beforeClickOn(WebElement element, WebDriver driver) {
        assertEquals(stubElement, ((WrapsElement) element).getWrappedElement());
      }
    }

    driver.register(new MyListener());

    driver.findElement(By.name("stub")).click();
  }

  @Test
  public void shouldReturnLocatorFromToStringMethod() {
    final WebElement stubElement = mock(WebElement.class);
    when(stubElement.toString()).thenReturn("cheese");

    final WebDriver driver = mock(WebDriver.class);
    when(driver.findElement(By.id("ignored"))).thenReturn(stubElement);

    EventFiringWebDriver firingDriver = new EventFiringWebDriver(driver);
    WebElement firingElement = firingDriver.findElement(By.id("ignored"));

    assertEquals(stubElement.toString(), firingElement.toString());
  }

  private static class ChildDriver extends StubDriver {}
}
