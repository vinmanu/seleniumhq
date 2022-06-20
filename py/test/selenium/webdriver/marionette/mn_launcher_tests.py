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

from selenium.webdriver import Firefox
from selenium.webdriver.firefox.service import Service

import pytest


def test_launch_and_close_browser(driver):
    assert 'browserName' in driver.capabilities


def test_we_can_launch_multiple_firefox_instances(capabilities):
    Firefox(capabilities=capabilities)
    Firefox(capabilities=capabilities)
    Firefox(capabilities=capabilities)


@pytest.mark.parametrize('log_path', [None, "different_file.log", ("my_custom_pipe", "w"), ["windows.log", "a+", "-1", "cp1253"]])
def test_launch_firefox_with_service_log_path(log_path):
    service = Service(log_path=log_path)
    driver = Firefox(service=service)
    if log_path is None:
        assert driver.service.log_file is None
    elif isinstance(log_path, str):
        assert driver.service.log_file.name == log_path
        assert driver.service.log_file.mode == "a+"
    else:
        assert driver.service.log_file.name == log_path[0]
        assert driver.service.log_file.mode == log_path[1]


def test_launch_firefox_with_service_default_log_path():
    service = Service()
    driver = Firefox(service=service)
    assert driver.service.log_file.mode == "a+"
    assert driver.service.log_file.name == "geckodriver.log"
