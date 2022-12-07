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

# This file is automatically generated. Any changes will be lost!

require_relative 'log/base_log_entry'
require_relative 'log/generic_log_entry'
require_relative 'log/console_log_entry'
require_relative 'log/javascript_log_entry'

module Selenium
  module WebDriver
    class BiDi
      class LogInspector
        EVENTS = {
          entry_added: 'entryAdded'
        }.freeze

        LOG_LEVEL = {
          DEBUG: "debug",
          ERROR: "error",
          INFO: "info",
          WARNING: "warning"
        }.freeze

        def initialize(driver, browsing_context_ids = nil)
          unless driver.capabilities.web_socket_url
            raise Error::WebDriverError, "WebDriver instance must support BiDi protocol"
          end

          @bidi = driver.bidi
          @bidi.session.subscribe("log.entryAdded", browsing_context_ids)
        end

        def on_console_log(&block)
          enabled = log_listeners[:console].any?
          log_listeners[:console] << block
          return if enabled

          on(:entry_added) do |params|
            type = params["type"]
            __send__(:console_log_events, params) if type.eql?("console")
          end
        end

        def on_javascript_log(&block)
          enabled = log_listeners[:javascript].any?
          log_listeners[:javascript] << block
          return if enabled

          on(:entry_added) do |params|
            type = params["type"]
            __send__(:javascript_log_events, params) if type.eql?("javascript")
          end
        end

        def on_javascript_exception(&block)
          enabled = log_listeners[:js_exception].any?
          log_listeners[:js_exception] << block
          log_listeners[:javascript] << block
          return if enabled

          on(:entry_added) do |params|
            type = params["type"]
            level = params["level"]

            __send__(:javascript_log_events, params) if type.eql?("javascript") && level.eql?(LOG_LEVEL[:ERROR])
          end
        end

        def on_log(&block)
          on(:entry_added, &block)
        end

        private

        def on(event, &block)
          event = EVENTS[event] if event.is_a?(Symbol)
          @bidi.callbacks["log.#{event}"] << block
        end

        def log_listeners
          @log_listeners ||= Hash.new { |listeners, kind| listeners[kind] = [] }
        end

        def console_log_events(params)
          event = ConsoleLogEntry.new(
            level: params['level'],
            text: params['text'],
            timestamp: params['timestamp'],
            type: params['type'],
            method: params['method'],
            realm: params['realm'],
            args: params['args'],
            stack_trace: params['stackTrace']
          )
          log_listeners[:console].each do |listener|
            listener.call(event)
          end
        end

        def javascript_log_events(params)
          event = JavascriptLogEntry.new(
            level: params['level'],
            text: params['text'],
            timestamp: params['timestamp'],
            type: params['type'],
            stack_trace: params['stackTrace']
          )
          log_listeners[:javascript].each do |listener|
            listener.call(event)
          end

          return unless params['level'].eql?(LOG_LEVEL[:ERROR])

          log_listeners[:js_exception].each do |listener|
            listener.call(event)
          end
        end

      end # LogInspector
    end # Bidi
  end # WebDriver
end # Selenium
