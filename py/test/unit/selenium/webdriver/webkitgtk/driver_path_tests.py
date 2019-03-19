import pytest


@pytest.fixture(autouse=True)
def preserve_driver_path():
    from selenium.webdriver import WebKitGTK
    old_driver_path = WebKitGTK.driver_path
    yield
    WebKitGTK.driver_path = old_driver_path


def test_driver_path_default(mocker):
    from selenium.webdriver import WebKitGTK
    mocker.patch("selenium.webdriver.remote.webdriver.WebDriver", autospec=True)
    mocked_service = mocker.patch(
        "selenium.webdriver.webkitgtk.service.Service",
        autospec=True,
    )
    WebKitGTK()
    args = {
        "executable_path": "WebKitWebDriver",
        "port": mocker.ANY,
        "service_args": mocker.ANY,
        "log_path": mocker.ANY,
        "env": mocker.ANY,
    }
    mocked_service.__init__.assert_called_with(args)


def test_driver_path_override(mocker):
    from selenium.webdriver import WebKitGTK
    mocker.patch("selenium.webdriver.remote.webdriver.WebDriver", autospec=True)
    mocked_service = mocker.patch(
        "selenium.webdriver.webkitgtk.service.Service",
        autospec=True,
    )

    WebKitGTK.driver_path = "/alternate/path"
    WebKitGTK()
    args = {
        "executable_path": "/alternate/path",
        "port": mocker.ANY,
        "service_args": mocker.ANY,
        "log_path": mocker.ANY,
        "env": mocker.ANY,
    }
    mocked_service.__init__.assert_called_with(args)


def test_executable_path_override(mocker):
    from selenium.webdriver import WebKitGTK
    mocker.patch("selenium.webdriver.remote.webdriver.WebDriver", autospec=True)
    mocked_service = mocker.patch(
        "selenium.webdriver.webkitgtk.service.Service",
        autospec=True,
    )

    WebKitGTK(executable_path="/alternate/other/path")
    args = {
        "executable_path": "/alternate/other/path",
        "port": mocker.ANY,
        "service_args": mocker.ANY,
        "log_path": mocker.ANY,
        "env": mocker.ANY,
    }
    mocked_service.__init__.assert_called_with(args)


def test_executable_path_override_priority_over_driver_path_override(mocker):
    from selenium.webdriver import WebKitGTK
    mocker.patch("selenium.webdriver.remote.webdriver.WebDriver", autospec=True)
    mocked_service = mocker.patch(
        "selenium.webdriver.webkitgtk.service.Service",
        autospec=True,
    )

    WebKitGTK.driver_path = "/alternate/path"
    WebKitGTK(executable_path="/alternate/other/path")
    args = {
        "executable_path": "/alternate/other/path",
        "port": mocker.ANY,
        "service_args": mocker.ANY,
        "log_path": mocker.ANY,
        "env": mocker.ANY,
    }
    mocked_service.__init__.assert_called_with(args)
