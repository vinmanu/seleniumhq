// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

import java.net.URI;

public class RedisClientHelper {
  private final RedisClient client;
  private final StatefulRedisConnection<String, String> connection;

  public RedisClientHelper(URI serverUri) {
    client = RedisClient.create(RedisURI.create(serverUri));
    connection = client.connect();
  }

  public StatefulRedisConnection<String, String> getConnection() {
    return connection;
  }

  public void close() {
    client.shutdown();
  }
}
