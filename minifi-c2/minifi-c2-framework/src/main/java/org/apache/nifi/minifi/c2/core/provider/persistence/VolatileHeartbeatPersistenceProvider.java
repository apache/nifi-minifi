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
package org.apache.nifi.minifi.c2.core.provider.persistence;

import org.apache.nifi.minifi.c2.api.provider.ProviderConfigurationContext;
import org.apache.nifi.minifi.c2.api.provider.ProviderCreationException;
import org.apache.nifi.minifi.c2.api.provider.heartbeat.HeartbeatPersistenceProvider;
import org.apache.nifi.minifi.c2.model.C2Heartbeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A test provider that only persists the most recent heartbeat for every agent/device.
 *
 * This implementation is not intended for use outside of development and testing.
 *
 * TODO, deep copy objects on save/get so that they cannot be modified outside this class.
 *
 */
class VolatileHeartbeatPersistenceProvider implements HeartbeatPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileHeartbeatPersistenceProvider.class);

    private Map<String, C2Heartbeat> heartbeats = new ConcurrentHashMap<>();
    private Map<String, C2Heartbeat> heartbeatsByAgentId = new ConcurrentHashMap<>();
    private Map<String, C2Heartbeat> heartbeatsByDeviceId = new ConcurrentHashMap<>();

    @Override
    public C2Heartbeat saveHeartbeat(C2Heartbeat heartbeat) {

        if (heartbeat == null || heartbeat.getIdentifier() == null) {
            throw new IllegalArgumentException("Heartbeat must be not null and must have an id in order to be saved.");
        }

        // TODO, atomic transaction
        heartbeats.put(heartbeat.getIdentifier(), heartbeat);
        if (heartbeat.getAgentInfo() != null && heartbeat.getAgentInfo().getIdentifier() != null) {
            heartbeatsByAgentId.put(heartbeat.getAgentInfo().getIdentifier(), heartbeat);
        }
        if (heartbeat.getDeviceInfo() != null && heartbeat.getDeviceInfo().getIdentifier() != null) {
            heartbeatsByDeviceId.put(heartbeat.getDeviceInfo().getIdentifier(), heartbeat);
        }

        logger.debug("Saved heartbeat with id={}", heartbeat.getIdentifier());
        return heartbeat;

    }

    @Override
    public List<C2Heartbeat> getHeartbeats() {
        return new ArrayList<>(heartbeats.values());
    }

    @Override
    public List<C2Heartbeat> getHeartbeatsByAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Agent id cannot be null");
        }
        List<C2Heartbeat> heartbeats = new ArrayList<>();
        C2Heartbeat hb = heartbeatsByAgentId.get(agentId);
        if (hb != null) {
            heartbeats.add(hb);
        }
        return heartbeats;
    }

    @Override
    public List<C2Heartbeat> getHeartbeatsByDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device id cannot be null");
        }
        List<C2Heartbeat> heartbeats = new ArrayList<>();
        C2Heartbeat hb = heartbeatsByDeviceId.get(deviceId);
        if (hb != null) {
            heartbeats.add(hb);
        }
        return heartbeats;
    }

    @Override
    public Optional<C2Heartbeat> getHeartbeat(String heartbeatId) {
        if (heartbeatId == null) {
            throw new IllegalArgumentException("Heartbeat id cannot be null");
        }
        return Optional.ofNullable(heartbeats.get(heartbeatId));
    }

    @Override
    public void deleteHeartbeat(String heartbeatId) {
        if (heartbeatId == null) {
            throw new IllegalArgumentException("Heartbeat id cannot be null");
        }
        heartbeats.remove(heartbeatId);
    }

    @Override
    public void deleteAllHeartbeats() {
        heartbeats.clear();
        heartbeatsByDeviceId.clear();
        heartbeatsByAgentId.clear();
    }

    @Override
    public void onConfigured(ProviderConfigurationContext configurationContext) throws ProviderCreationException {
        // do nothing
    }
}
