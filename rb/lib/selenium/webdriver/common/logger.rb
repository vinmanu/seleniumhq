# frozen_string_literal: true

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

require 'selenium/logger'

module Selenium
  module WebDriver
    class Logger < Selenium::Logger
      def initialize(progname = 'Selenium', default_level: nil, ignored: nil, allowed: nil)
        super
        deprecate 'Selenium::WebDriver::Logger', 'Selenium::Logger', id: :webdriver_logger
      end
    end

    #
    # Returns logger instance that can be used across the whole Selenium.
    # Set level for backwards compatibility
    #
    # @return [Logger]
    #

    def self.logger(**opts)
      level = $DEBUG || ENV.key?('DEBUG') ? :debug : :info
      @logger ||= Selenium::Logger.new('Selenium', default_level: level, **opts)
    end
  end # WebDriver
end # Selenium
