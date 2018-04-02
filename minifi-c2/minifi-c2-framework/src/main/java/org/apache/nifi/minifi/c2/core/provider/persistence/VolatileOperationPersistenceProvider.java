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

import org.apache.nifi.minifi.c2.api.provider.operations.OperationPersistenceProvider;
import org.apache.nifi.minifi.c2.model.OperationRequest;
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
public class VolatileOperationPersistenceProvider implements OperationPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileOperationPersistenceProvider.class);

    private Map<String, OperationRequest> operations = new ConcurrentHashMap<>();

    @Override
    public long getCount() {
        return operations.size();
    }


    @Override
    public OperationRequest save(OperationRequest operationRequest) {
        if (operationRequest == null || operationRequest.getOperation() == null || operationRequest.getOperation().getIdentifier() == null) {
            throw new IllegalArgumentException("operation must be not null and have id");
        }
        operations.put(operationRequest.getOperation().getIdentifier(), operationRequest);
        return operationRequest;
    }

    @Override
    public Iterable<OperationRequest> getAll() {
        return new ArrayList<>(operations.values());
    }

    @Override
    public Iterable<OperationRequest> getByAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Agent id cannot be null");
        }
        return operations.values().stream()
                .filter(operation -> agentId.equals(operation.getTargetAgentIdentifier()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        return operations.containsKey(operationId);
    }

    @Override
    public Optional<OperationRequest> getById(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        return Optional.ofNullable(operations.get(operationId));
    }

    @Override
    public void deleteById(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        operations.remove(operationId);
    }

    @Override
    public void delete(OperationRequest operationRequest) {
        if (operationRequest == null || operationRequest.getOperation() == null || operationRequest.getOperation().getIdentifier() == null) {
            throw new IllegalArgumentException("operation must be not null and have id");
        }
        operations.remove(operationRequest.getOperation().getIdentifier());
    }

    @Override
    public void deleteAll() {
        operations.clear();
    }

}
