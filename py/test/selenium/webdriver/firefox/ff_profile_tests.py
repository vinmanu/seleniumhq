# Licensed to the Software Freedom Conservancy (SFC) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The SFC licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import base64
import os
import zipfile

try:
    from io import BytesIO
except ImportError:
    from cStringIO import StringIO as BytesIO

try:
    unicode
except NameError:
    unicode = str

import pytest

from selenium.webdriver import Firefox, FirefoxProfile
from selenium.webdriver.common.proxy import Proxy, ProxyType


def test_that_we_can_accept_a_profile(capabilities, webserver):
    profile1 = FirefoxProfile()
    profile1.set_preference("browser.startup.homepage_override.mstone", "")
    profile1.set_preference("startup.homepage_welcome_url", webserver.where_is('simpleTest.html'))
    profile1.update_preferences()

    profile2 = FirefoxProfile(profile1.path)
    driver = Firefox(
        capabilities=capabilities,
        firefox_profile=profile2)
    title = driver.title
    driver.quit()
    assert "Hello WebDriver" == title


def test_that_prefs_are_written_in_the_correct_format():
    profile = FirefoxProfile()
    profile.set_preference("sample.preference", "hi there")
    profile.update_preferences()

    assert 'hi there' == profile.default_preferences["sample.preference"]

    encoded = profile.encoded
    decoded = base64.b64decode(encoded)
    with BytesIO(decoded) as fp:
        zip = zipfile.ZipFile(fp, "r")
        for entry in zip.namelist():
            if entry.endswith("user.js"):
                user_js = zip.read(entry)
                for line in user_js.splitlines():
                    if line.startswith(b'user_pref("sample.preference",'):
                        assert line.endswith(b'hi there");')
            # there should be only one user.js
            break


def test_that_unicode_prefs_are_written_in_the_correct_format():
    profile = FirefoxProfile()
    profile.set_preference('sample.preference.2', unicode('hi there'))
    profile.update_preferences()

    assert 'hi there' == profile.default_preferences["sample.preference.2"]

    encoded = profile.encoded
    decoded = base64.b64decode(encoded)
    with BytesIO(decoded) as fp:
        zip = zipfile.ZipFile(fp, "r")
        for entry in zip.namelist():
            if entry.endswith('user.js'):
                user_js = zip.read(entry)
                for line in user_js.splitlines():
                    if line.startswith(b'user_pref("sample.preference.2",'):
                        assert line.endswith(b'hi there");')
            # there should be only one user.js
            break


def test_that_integer_prefs_are_written_in_the_correct_format():
    profile = FirefoxProfile()
    profile.set_preference("sample.int.preference", 12345)
    profile.update_preferences()
    assert 12345 == profile.default_preferences["sample.int.preference"]


def test_that_boolean_prefs_are_written_in_the_correct_format():
    profile = FirefoxProfile()
    profile.set_preference("sample.bool.preference", True)
    profile.update_preferences()
    assert profile.default_preferences["sample.bool.preference"] is True


def test_that_we_delete_the_profile(capabilities):
    driver = Firefox(capabilities=capabilities)
    path = driver.firefox_profile.path
    driver.quit()
    assert not os.path.exists(path)


def test_profiles_do_not_share_preferences():
    profile1 = FirefoxProfile()
    profile1.accept_untrusted_certs = False
    profile2 = FirefoxProfile()
    # Default is true. Should remain so.
    assert profile2.default_preferences["webdriver_accept_untrusted_certs"] is True


def test_none_proxy_is_set():
    profile = FirefoxProfile()
    proxy = None
    with pytest.raises(ValueError):
        profile.set_proxy(proxy)
    assert "network.proxy.type" not in profile.default_preferences


def test_unspecified_proxy_is_set():
    profile = FirefoxProfile()
    proxy = Proxy()
    profile.set_proxy(proxy)
    assert "network.proxy.type" not in profile.default_preferences


def test_manual_proxy_is_set_in_profile():
    profile = FirefoxProfile()
    proxy = Proxy()
    proxy.no_proxy = 'localhost, foo.localhost'
    proxy.http_proxy = 'some.url:1234'
    proxy.ftp_proxy = None
    proxy.sslProxy = 'some2.url'

    profile.set_proxy(proxy)
    assert profile.default_preferences["network.proxy.type"] == ProxyType.MANUAL['ff_value']
    assert profile.default_preferences["network.proxy.no_proxies_on"] == 'localhost, foo.localhost'
    assert profile.default_preferences["network.proxy.http"] == 'some.url'
    assert profile.default_preferences["network.proxy.http_port"] == 1234
    assert profile.default_preferences["network.proxy.ssl"] == 'some2.url'
    assert "network.proxy.ssl_port" not in profile.default_preferences
    assert "network.proxy.ftp" not in profile.default_preferences


def test_pac_proxy_is_set_in_profile():
    profile = FirefoxProfile()
    proxy = Proxy()
    proxy.proxy_autoconfig_url = 'http://some.url:12345/path'

    profile.set_proxy(proxy)
    assert profile.default_preferences["network.proxy.type"] == ProxyType.PAC['ff_value']
    assert profile.default_preferences["network.proxy.autoconfig_url"] == 'http://some.url:12345/path'


def test_autodetect_proxy_is_set_in_profile():
    profile = FirefoxProfile()
    proxy = Proxy()
    proxy.auto_detect = True

    profile.set_proxy(proxy)
    assert profile.default_preferences["network.proxy.type"] == ProxyType.AUTODETECT['ff_value']


def test_that_we_can_add_a_webextension(capabilities, webserver):
    extension_filename = 'webextension.xpi'
    # TODO: This file should probably live in a common directory.
    current_directory = os.path.dirname(os.path.realpath(__file__))
    root_directory = os.path.join(current_directory, '..' , '..', '..', '..', '..')
    data_directory = os.path.join(root_directory, 'javascript', 'node',
                                  'selenium-webdriver', 'lib', 'test', 'data', 'firefox')
    full_extension_filename = os.path.join(data_directory, extension_filename)

    profile = FirefoxProfile()
    profile.add_extension(full_extension_filename)

    driver = Firefox(fcapabilities=capabilities, firefox_profile=profile)
    driver.get(webserver.where_is('simpleTest.html'))
    driver.find_element_by_id('webextensions-selenium-example')
