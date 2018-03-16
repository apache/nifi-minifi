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

import org.apache.nifi.minifi.c2.api.provider.agent.AgentPersistenceProvider;
import org.apache.nifi.minifi.c2.model.Agent;
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
public class VolatileAgentPersistenceProvider implements AgentPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileAgentPersistenceProvider.class);

    private Map<String, Agent> agents = new ConcurrentHashMap<>();

    @Override
    public long getCount() {
        return agents.size();
    }

    @Override
    public Agent save(Agent agent) {
        if (agent == null || agent.getIdentifier() == null) {
            throw new IllegalArgumentException("Agent must be not null and must have an id");
        }
        agents.put(agent.getIdentifier(), agent);
        return agent;
    }

    @Override
    public Iterable<Agent> getAll() {
        return new ArrayList<>(agents.values());
    }

    @Override
    public Iterable<Agent> getByClassName(String agentClassName) {
        return agents.values().stream().filter(agent -> agentClassName.equals(agent.getAgentClass())).collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        return agents.containsKey(agentId);
    }

    @Override
    public Optional<Agent> getById(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        return Optional.ofNullable(agents.get(agentId));
    }

    @Override
    public void deleteById(String agentId) {
        agents.remove(agentId);
    }

    @Override
    public void delete(Agent agent) {
        if (agent == null || agent.getIdentifier() == null) {
            throw new IllegalArgumentException("Agent must be not null and must have an id");
        }
        deleteById(agent.getIdentifier());
    }

    @Override
    public void deleteAll() {
        agents.clear();
    }
}
