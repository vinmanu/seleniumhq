// <copyright file="GlobalSuppressions.cs" company="WebDriver Committers">
// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The SFC licenses this file
// to you under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// </copyright>

// This file is used by Code Analysis to maintain SuppressMessage 
// attributes that are applied to this project. 
// Project-level suppressions either have no target or are given 
// a specific target and scoped to a namespace, type, member, etc. 
//
// To add a suppression to this file, right-click the message in the 
// Error List, point to "Suppress Message(s)", and click 
// "In Project Suppression File". 
// You do not need to add suppressions to this file manually. 
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1006:DoNotNestGenericTypesInMemberSignatures", Scope = "member", Target = "OpenQA.Selenium.By.#FindElementsMethod", Justification = "Type is properly specified. It should be a Func<T, TResult> that returns a ReadOnlyCollection<IWebElement>")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1006:DoNotNestGenericTypesInMemberSignatures", Scope = "member", Target = "OpenQA.Selenium.By.#.ctor(System.Func`2<OpenQA.Selenium.ISearchContext,OpenQA.Selenium.IWebElement>,System.Func`2<OpenQA.Selenium.ISearchContext,System.Collections.ObjectModel.ReadOnlyCollection`1<OpenQA.Selenium.IWebElement>>)", Justification = "Type is properly specified. It should be a Func<T, TResult> that returns a ReadOnlyCollection<IWebElement>")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1020:AvoidNamespacesWithFewTypes", Scope = "namespace", Target = "OpenQA.Selenium.PhantomJS", Justification = "Namespaces are properly scoped.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1020:AvoidNamespacesWithFewTypes", Scope = "namespace", Target = "OpenQA.Selenium.Interactions", Justification = "Namespaces are properly scoped.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1020:AvoidNamespacesWithFewTypes", Scope = "namespace", Target = "OpenQA.Selenium.Chrome", Justification = "Namespaces are properly scoped.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1020:AvoidNamespacesWithFewTypes", Scope = "namespace", Target = "OpenQA.Selenium.Opera", Justification = "Namespaces are properly scoped.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1024:UsePropertiesWhereAppropriate", Scope = "member", Target = "OpenQA.Selenium.ITakesScreenshot.#GetScreenshot()", Justification = "API specification demands method.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1027:MarkEnumsWithFlags", Scope = "type", Target = "OpenQA.Selenium.ProxyKind", Justification = "The ProxyKind enum is not a set of flags, but has values determined by an external API.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1027:MarkEnumsWithFlags", Scope = "type", Target = "OpenQA.Selenium.WebDriverResult", Justification = "The WebDriverResult enum is not a set of flags, but has values determined by an external API.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1027:MarkEnumsWithFlags", Scope = "type", Target = "OpenQA.Selenium.Safari.Internal.FrameType", Justification = "Enum is not a set of flags.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.SocketWrapper.#OnAuthenticate(System.IAsyncResult)", Justification = "Providing event for all error types, so catching top-level exception is correct design.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.SocketWrapper.#OnDataSend(System.IAsyncResult)", Justification = "Providing event for all error types, so catching top-level exception is correct design.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.SocketWrapper.#OnDataReceive(System.IAsyncResult)", Justification = "Providing event for all error types, so catching top-level exception is correct design.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1031:DoNotCatchGeneralExceptionTypes", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.SocketWrapper.#OnClientConnect(System.IAsyncResult)", Justification = "Providing event for all error types, so catching top-level exception is correct design.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings", Scope = "member", Target = "OpenQA.Selenium.IWebDriver.#Url", Justification = "Specification demands string value for property.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings", Scope = "member", Target = "OpenQA.Selenium.Proxy.#ProxyAutoConfigUrl", Justification = "Proxy configuration can be string instead of Uri.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings", Scope = "member", Target = "OpenQA.Selenium.IE.InternetExplorerOptions.#InitialBrowserUrl", Justification = "InitialBrowserUrl should be string instead of Uri.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings", Scope = "member", Target = "OpenQA.Selenium.Chrome.ChromeDriverService.#UrlPathPrefix", Justification = "UrlPathPrefix is a prefix for use with ChromeDriver, and should be a string.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings", Scope = "member", Target = "OpenQA.Selenium.Opera.OperaDriverService.#UrlPathPrefix", Justification = "UrlPathPrefix is a prefix for use with OperaDriver, and should be a string.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings", Scope = "member", Target = "OpenQA.Selenium.PhantomJS.PhantomJSDriverService.#GridHubUrl", Justification = "GridHubUrl is a command line for use with PhantomJS, and should properly be a string.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Globalization", "CA1308:NormalizeStringsToUppercase", Scope = "member", Target = "OpenQA.Selenium.Firefox.Preferences.#SetPreferenceValue(System.String,System.Object)", Justification = "Strings are normalized to lower case by JSON wire protocol.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Globalization", "CA1308:NormalizeStringsToUppercase", Scope = "member", Target = "OpenQA.Selenium.Remote.RemoteWebElement.#GetAttribute(System.String)", Justification = "Strings are normalized to lower case by JSON wire protocol.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "OnScreen", Scope = "member", Target = "OpenQA.Selenium.Interactions.Internal.ICoordinates.#LocationOnScreen", Justification = "On Screen is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "OnScreen", Scope = "member", Target = "OpenQA.Selenium.ILocatable.#LocationOnScreenOnceScrolledIntoView", Justification = "On Screen is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "BridgePort", Scope = "member", Target = "OpenQA.Selenium.Chrome.ChromeDriverService.#AndroidDebugBridgePort", Justification = "Bridge Port is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "BridgePort", Scope = "member", Target = "OpenQA.Selenium.Opera.OperaDriverService.#AndroidDebugBridgePort", Justification = "Bridge Port is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "TouchScreen", Scope = "type", Target = "OpenQA.Selenium.IHasTouchScreen", Justification = "Touch Screen is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "TouchScreen", Scope = "member", Target = "OpenQA.Selenium.IHasTouchScreen.#TouchScreen", Justification = "Touch Screen is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "TouchScreen", Scope = "type", Target = "OpenQA.Selenium.ITouchScreen", Justification = "Touch Screen is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1702:CompoundWordsShouldBeCasedCorrectly", MessageId = "TouchScreen", Scope = "type", Target = "OpenQA.Selenium.Remote.RemoteTouchScreen", Justification = "Touch Screen is properly used as two-word discrete term.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "xpath", Scope = "member", Target = "OpenQA.Selenium.By.#XPath(System.String)", Justification = "XPath is spelled correctly.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "xpath", Scope = "member", Target = "OpenQA.Selenium.Internal.IFindsByXPath.#FindElementByXPath(System.String)", Justification = "XPath is spelled correctly.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "xpath", Scope = "member", Target = "OpenQA.Selenium.Internal.IFindsByXPath.#FindElementsByXPath(System.String)", Justification = "XPath is spelled correctly.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "Minidump", Scope = "member", Target = "OpenQA.Selenium.Chrome.ChromeOptions.#MinidumpPath", Justification = "Minidump is spelled correctly.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "Minidump", Scope = "member", Target = "OpenQA.Selenium.Opera.OperaOptions.#MinidumpPath", Justification = "Minidump is spelled correctly.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "Api", Scope = "member", Target = "OpenQA.Selenium.IE.InternetExplorerOptions.#ForceCreateProcessApi", Justification = "API is spelled and cased correctly for use in method names.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1704:IdentifiersShouldBeSpelledCorrectly", MessageId = "Api", Scope = "member", Target = "OpenQA.Selenium.IE.InternetExplorerOptions.#ForceShellWindowsApi", Justification = "API is spelled and cased correctly for use in method names.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays", Scope = "member", Target = "OpenQA.Selenium.Remote.ErrorResponse.#StackTrace", Justification = "Specification compliance demands use of an array.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays", Scope = "member", Target = "OpenQA.Selenium.Screenshot.#AsByteArray", Justification = "Specification compliance demands use of an array.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.BinaryMessageHandledEventArgs.#Data", Justification = "Type is properly specified. It should be an array of bytes.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.ReceivedEventArgs.#Buffer", Justification = "Type is properly specified. It should be an array of bytes.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays", Scope = "member", Target = "OpenQA.Selenium.Safari.Internal.WebSocketHttpRequest.#Payload", Justification = "Type is properly specified. It should be an array of bytes.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2202:Do not dispose objects multiple times", Scope = "member", Target = "OpenQA.Selenium.Firefox.FirefoxProfile.#FromBase64String(System.String)", Justification = "Separate disposal of the stream object is approved, and ensures disposal if there are exceptions in nested object constructor or method.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2202:Do not dispose objects multiple times", Scope = "member", Target = "OpenQA.Selenium.Firefox.FirefoxProfile.#ReadDefaultPreferences()", Justification = "Separate disposal of the stream object is approved, and ensures disposal if there are exceptions in nested object constructor or method.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2202:Do not dispose objects multiple times", Scope = "member", Target = "OpenQA.Selenium.Firefox.FirefoxProfile.#ToBase64String()", Justification = "Separate disposal of the stream object is approved, and ensures disposal if there are exceptions in nested object constructor or method.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2202:Do not dispose objects multiple times", Scope = "member", Target = "OpenQA.Selenium.Remote.RemoteWebElement.#UploadFile(System.String)", Justification = "Separate disposal of the stream object is approved, and ensures disposal if there are exceptions in nested object constructor or method.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA2204:Literals should be spelled correctly", MessageId = "WebDriver", Scope = "member", Target = "OpenQA.Selenium.Remote.HttpCommandExecutor.#CreateResponse(System.Net.WebRequest)", Justification = "WebDriver is correctly used as a single word.")]
[assembly: System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2214:DoNotCallOverridableMethodsInConstructors", Scope = "member", Target = "OpenQA.Selenium.Remote.RemoteWebDriver.#.ctor(OpenQA.Selenium.Remote.ICommandExecutor,OpenQA.Selenium.ICapabilities)", Justification = "Class provides a hook for subclasses to modify functionality, so virtual method call in constructor is appropriate.")]

