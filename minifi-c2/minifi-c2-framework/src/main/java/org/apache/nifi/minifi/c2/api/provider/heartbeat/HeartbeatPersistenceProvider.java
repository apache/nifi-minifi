/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.minifi.c2.api.provider.heartbeat;

import org.apache.nifi.minifi.c2.api.provider.Provider;
import org.apache.nifi.minifi.c2.model.C2Heartbeat;

import java.util.List;
import java.util.Optional;


/**
 * Note: This is an unstable interface that is expected to change.
 *
 * TODO - normalize heartbeat objects into sensible RDB ERM.
 * Will need heartbeat id/key, heartbeat summary, etc.
 */
public interface HeartbeatPersistenceProvider extends Provider {

    C2Heartbeat saveHeartbeat(C2Heartbeat heartbeat);

    List<C2Heartbeat> getHeartbeats();

    List<C2Heartbeat> getHeartbeatsByAgent(String agentId);

    List<C2Heartbeat> getHeartbeatsByDevice(String deviceId);

    Optional<C2Heartbeat> getHeartbeat(String heartbeatId);

    void deleteHeartbeat(String heartbeatId);

    void deleteAllHeartbeats();

}
