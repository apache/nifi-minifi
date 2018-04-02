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

import org.apache.nifi.minifi.c2.api.provider.agent.AgentManifestPersistenceProvider;
import org.apache.nifi.minifi.c2.model.AgentManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
public class VolatileAgentManifestPersistenceProvider implements AgentManifestPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileAgentManifestPersistenceProvider.class);

    private Map<String, AgentManifest> agentManifests = new ConcurrentHashMap<>();

    @Override
    public long getCount() {
        return agentManifests.size();
    }

    @Override
    public AgentManifest save(AgentManifest agentManifest) {
        if (agentManifest == null || agentManifest.getIdentifier() == null) {
            throw new IllegalArgumentException("Agent Manifest must be not null and have an id in order to be saved.");
        }
        agentManifests.put(agentManifest.getIdentifier(), agentManifest);
        return agentManifest;
    }

    @Override
    public Iterable<AgentManifest> getAll() {
        return new ArrayList<>(agentManifests.values());
    }

    @Override
    public Iterable<AgentManifest> getAllById(Iterable<String> agentManifestIds) {
        if (agentManifestIds == null) {
            throw new IllegalArgumentException("Agent manifest ids must not be null");
        }

        HashSet<AgentManifest> matchedManifests = new HashSet<>();
        agentManifestIds.forEach(id -> {
                AgentManifest manifest = agentManifests.get(id);
                if (manifest != null) {
                    matchedManifests.add(manifest);
                }
        });
        return matchedManifests;

    }

    @Override
    public boolean existsById(String agentManifestId) {
        if (agentManifestId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        return agentManifests.containsKey(agentManifestId);
    }

    @Override
    public Optional<AgentManifest> getById(String agentManifestId) {
        if (agentManifestId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        return Optional.ofNullable(agentManifests.get(agentManifestId));
    }

    @Override
    public void deleteById(String agentManifestId) {
        if (agentManifestId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        agentManifests.remove(agentManifestId);
    }

    @Override
    public void delete(AgentManifest agentManifest) {
        if (agentManifest == null || agentManifest.getIdentifier() == null) {
            throw new IllegalArgumentException("Agent Manifest must be not null and must have an id");
        }
        deleteById(agentManifest.getIdentifier());
    }

    @Override
    public void deleteAll() {
        agentManifests.clear();
    }
}
