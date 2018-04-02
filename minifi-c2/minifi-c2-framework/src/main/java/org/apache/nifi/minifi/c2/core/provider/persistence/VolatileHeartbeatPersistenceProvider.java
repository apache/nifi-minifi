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

import org.apache.nifi.minifi.c2.api.provider.heartbeat.HeartbeatPersistenceProvider;
import org.apache.nifi.minifi.c2.model.C2Heartbeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple, in-memory "persistence" provider in order to test the service layer.
 *
 * This is not designed for real use outside of development. For example:
 *   - it only keeps an in-memory record of saved entities, there is no real persistence
 *   - it does not support transactions
 *   - it does not clone objects on save/retrieval, so any modifications made after interacting with this service
 *     also modify the "persisted" copies.
 *
 * TODO, deep copy objects on save/get so that they cannot be modified outside this class without modifying the persisted copy.
 */
class VolatileHeartbeatPersistenceProvider implements HeartbeatPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileHeartbeatPersistenceProvider.class);

    private Map<String, C2Heartbeat> heartbeats = new ConcurrentHashMap<>();

    @Override
    public long getCount() {
        return heartbeats.size();
    }

    @Override
    public C2Heartbeat save(C2Heartbeat heartbeat) {

        if (heartbeat == null || heartbeat.getIdentifier() == null) {
            throw new IllegalArgumentException("Heartbeat must be not null and must have an id in order to be saved.");
        }

        // TODO, atomic transaction
        heartbeats.put(heartbeat.getIdentifier(), heartbeat);

        logger.debug("Saved heartbeat with id={}", heartbeat.getIdentifier());
        return heartbeat;

    }

    @Override
    public Iterable<C2Heartbeat> getAll() {
        return new ArrayList<>(heartbeats.values());
    }

    @Override
    public Iterable<C2Heartbeat> getByAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Agent id cannot be null");
        }

        return heartbeats.values().stream()
                .filter(hb -> hb.getAgentInfo() != null)
                .filter(hb -> agentId.equals(hb.getAgentInfo().getIdentifier()) )
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String heartbeatId) {
        if (heartbeatId == null) {
            throw new IllegalArgumentException("Heartbeat id cannot be null");
        }
        return heartbeats.containsKey(heartbeatId);
    }

    @Override
    public Optional<C2Heartbeat> getById(String heartbeatId) {
        if (heartbeatId == null) {
            throw new IllegalArgumentException("Heartbeat id cannot be null");
        }
        return Optional.ofNullable(heartbeats.get(heartbeatId));
    }

    @Override
    public void deleteById(String heartbeatId) {
        if (heartbeatId == null) {
            throw new IllegalArgumentException("Heartbeat id cannot be null");
        }
        heartbeats.remove(heartbeatId);
    }

    @Override
    public void delete(C2Heartbeat heartbeat) {
        if (heartbeat == null || heartbeat.getIdentifier() == null) {
            throw new IllegalArgumentException("heartbeat cannot be null and must have an id");
        }
        deleteById(heartbeat.getIdentifier());
    }

    @Override
    public void deleteAll() {
        heartbeats.clear();
    }

}
