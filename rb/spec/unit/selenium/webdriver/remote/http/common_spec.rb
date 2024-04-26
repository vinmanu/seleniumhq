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

require File.expand_path('../../spec_helper', __dir__)

module Selenium
  module WebDriver
    module Remote
      module Http
        describe Common do
          subject(:common) do
            common = described_class.new
            common.server_url = URI.parse('http://server')
            allow(common).to receive(:request)

            common
          end

          after do
            described_class.extra_headers = nil
            described_class.user_agent = nil
          end

          it 'sends non-empty body header for POST requests without command data' do
            common.call(:post, 'clear', nil)

            expect(common).to have_received(:request)
              .with(:post, URI.parse('http://server/clear'),
                    hash_including('Content-Length' => '2'), '{}')
          end

          it 'sends a standard User-Agent by default' do
            user_agent_regexp = %r{\Aselenium/#{WebDriver::VERSION} \(ruby #{Platform.os}\)\z}

            common.call(:post, 'session', nil)

            expect(common).to have_received(:request)
              .with(:post, URI.parse('http://server/session'),
                    hash_including('User-Agent' => a_string_matching(user_agent_regexp)), '{}')
          end

          it 'allows registering extra headers' do
            described_class.extra_headers = {'Foo' => 'bar'}

            common.call(:post, 'session', nil)

            expect(common).to have_received(:request)
              .with(:post, URI.parse('http://server/session'),
                    hash_including('Foo' => 'bar'), '{}')
          end

          it 'allows overriding default User-Agent' do
            described_class.user_agent = 'rspec/1.0 (ruby 3.2)'

            common.call(:post, 'session', nil)

            expect(common).to have_received(:request)
              .with(:post, URI.parse('http://server/session'),
                    hash_including('User-Agent' => 'rspec/1.0 (ruby 3.2)'), '{}')
          end
        end
      end # Http
    end # Remote
  end # WebDriver
end # Selenium
