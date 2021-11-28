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

module Selenium
  module WebDriver
    module Interactions
      class PointerMove < Interaction
        VIEWPORT = :viewport
        POINTER = :pointer
        ORIGINS = [VIEWPORT, POINTER].freeze

        def initialize(source, duration, x, y, element: nil, origin: nil)
          super(source)
          @duration = duration * 1000
          @x_offset = x
          @y_offset = y
          @origin = element || origin || :viewport
        end

        def type
          :pointerMove
        end

        def encode
          {type: type, duration: @duration.to_i, x: @x_offset, y: @y_offset, origin: @origin}
        end
      end # PointerMove
    end # Interactions
  end # WebDriver
end # Selenium
