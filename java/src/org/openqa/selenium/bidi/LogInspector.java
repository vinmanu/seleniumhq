package org.openqa.selenium.bidi;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.log.*;
import org.openqa.selenium.internal.Require;

import java.util.*;
import java.util.function.Consumer;

public class LogInspector {

  private final List<Consumer<ConsoleLogEntry>> consoleLogListeners = new LinkedList<>();
  private final List<Consumer<JavascriptLogEntry>> jsExceptionLogListeners = new LinkedList<>();
  private final List<Consumer<JavascriptLogEntry>> jsLogListeners = new LinkedList<>();
  private final List<Consumer<GenericLogEntry>> genericLogListeners = new LinkedList<>();
  private final Set<String> browsingContextIds;

  private final BiDi bidi;

  public LogInspector(WebDriver driver) {
    this(new HashSet<>(), driver);
  }

  public LogInspector(String browsingContextId, WebDriver driver) {
    Require.nonNull("WebDriver", driver);
    Require.nonNull("Browsing context id", browsingContextId);

    if (!(driver instanceof HasBiDi)) {
      throw new IllegalArgumentException("WebDriver instance must support BiDi protocol");
    }

    this.bidi = ((HasBiDi) driver).getBiDi();
    this.browsingContextIds = new HashSet<>();
    browsingContextIds.add(browsingContextId);
    initializeLogListener();
  }

  public LogInspector(Set<String> browsingContextIds, WebDriver driver) {
    Require.nonNull("WebDriver", driver);
    Require.nonNull("Browsing context id list", browsingContextIds);

    if (!(driver instanceof HasBiDi)) {
      throw new IllegalArgumentException("WebDriver instance must support BiDi protocol");
    }

    this.bidi = ((HasBiDi) driver).getBiDi();
    this.browsingContextIds = browsingContextIds;
    initializeLogListener();
  }

  private void initializeLogListener() {
    Consumer<LogEntry> logEntryConsumer = logEntry -> {
      logEntry.getConsoleLogEntry().ifPresent(
        consoleLogEntry -> consoleLogListeners.forEach(
          consumer -> consumer.accept(consoleLogEntry)));

      logEntry.getJavascriptLogEntry().ifPresent(
        jsLogEntry -> {
          if (jsLogEntry.getLevel() == BaseLogEntry.LogLevel.ERROR) {
            jsExceptionLogListeners.forEach(
              consumer -> consumer.accept(jsLogEntry));
          } else {
            jsLogListeners.forEach(
              consumer -> consumer.accept(jsLogEntry));
          }
        }
      );

      logEntry.getGenericLogEntry().ifPresent(
        genericLogEntry -> genericLogListeners.forEach(
          consumer -> consumer.accept(genericLogEntry)));

    };

    if (browsingContextIds.isEmpty()) {
      this.bidi.addListener(Log.entryAdded(), logEntryConsumer);
    } else {
      this.bidi.addListener(browsingContextIds, Log.entryAdded(), logEntryConsumer);
    }
  }

  public void onConsoleLog(Consumer<ConsoleLogEntry> consumer) {
    consoleLogListeners.add(consumer);
  }

  public void onJavaScriptLog(Consumer<JavascriptLogEntry> consumer) {
    jsLogListeners.add(consumer);
  }

  public void onJavaScriptException(Consumer<JavascriptLogEntry> consumer) {
    jsExceptionLogListeners.add(consumer);
  }

  public void onGenericLog(Consumer<GenericLogEntry> consumer) {
    genericLogListeners.add(consumer);
  }

  public void onLog(Consumer<LogEntry> consumer) {
    this.bidi.addListener(Log.entryAdded(), consumer);
  }
}
