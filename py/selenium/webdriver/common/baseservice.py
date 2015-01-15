from __future__ import absolute_import

import abc
import os
import six
import time

from selenium.common.exceptions import WebDriverException
from selenium.webdriver.common import utils


@six.add_metaclass(abc.ABCMeta)
class BaseService(object):
    """
    An abstract base class to implement the common code among the various services
    Particularly for ie, opera, phantomjs, safari, and chrom
    """

    port_retries = 30

    def __init__(self, executable_path, port=0):
        """
        Creates a new instance of the Service

        :Args:
         - executable_path : Path to the ChromeDriver
         - port : Port the service is running on
        """
        if port == 0:
            port = utils.free_port()
        self.port = port
        self.path = executable_path

    @abc.abstractmethod
    def start(self):
        pass

    @abc.abstractmethod
    def stop(self):
        pass

    @property
    def service_url(self):
        return "http://localhost:%d" % self.port

    def wait_for_open_port(self, wait_open=True):
        """
        Waits for the port specified on this instance to be open.
        Unless wait_open is False, in which case it will actually wait for the port to be closed.

        :param wait_open: Specifies whether it should wait for the port to be open or closed
        :type wait_open: bool
        :raises WebDriverException: Raises this exception if it fails to achieve the
            desired status
        """
        count = 0
        while utils.is_connectable(self.port) is not wait_open:
            count += 1
            time.sleep(1)
            if count >= 30:
                raise WebDriverException("Can not connect to "
                                         "the '{0}'".format(os.path.basename(self.path)))