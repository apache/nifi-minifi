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

import org.apache.nifi.minifi.c2.api.provider.agent.AgentClassPersistenceProvider;
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
public class VolatileAgentClassPersistenceProvider implements AgentClassPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileAgentClassPersistenceProvider.class);

    private Map<String, AgentClass> agentClasses = new ConcurrentHashMap<>();

    @Override
    public long getCount() {
        return agentClasses.size();
    }

    @Override
    public AgentClass save(AgentClass agentClass) {
        if (agentClass == null || agentClass.getName() == null) {
            throw new IllegalArgumentException("Agent Class must be not null and have a name in order to be saved.");
        }
        agentClasses.put(agentClass.getName(), agentClass);
        logger.debug("Saved AgentClass with name={}", agentClass.getName());
        return agentClass;

    }

    @Override
    public Iterable<AgentClass> getAll() {
        return new ArrayList<>(agentClasses.values());
    }

    @Override
    public boolean existsById(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        return agentClasses.containsKey(name);
    }

    @Override
    public Optional<AgentClass> getById(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        return Optional.ofNullable(agentClasses.get(name));
    }

    @Override
    public void deleteById(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        AgentClass removed = agentClasses.remove(name);
        if (removed != null) {
            logger.debug("Found and deleted AgentClass with name='{}'", name);
        } else {
            logger.debug("Could not delete AgentClass with name='{}' (no match).", name);
        }
    }

    @Override
    public void delete(AgentClass agentClass) {
        if (agentClass == null || agentClass.getName() == null) {
            throw new IllegalArgumentException("Agent Class must be not null and have a name.");
        }
        deleteById(agentClass.getName());
    }

    @Override
    public void deleteAll() {
        agentClasses.clear();
    }
}
