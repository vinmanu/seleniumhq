// <copyright file="IHasInputDevices.cs" company="WebDriver Committers">
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

using System;

namespace OpenQA.Selenium
{
    /// <summary>
    /// Provides access to input devices for advanced user interactions.
    /// </summary>
    [Obsolete("Use the Actions or ActionBuilder class to simulate mouse and keyboard input.")]
    public interface IHasInputDevices
    {
        /// <summary>
        /// Gets an <see cref="IKeyboard"/> object for sending keystrokes to the browser.
        /// </summary>
        IKeyboard Keyboard { get; }

        /// <summary>
        /// Gets an <see cref="IMouse"/> object for sending mouse commands to the browser.
        /// </summary>
        IMouse Mouse { get; }
    }
}
