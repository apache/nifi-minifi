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
package org.apache.nifi.minifi.c2.core.service;

import org.apache.nifi.minifi.c2.api.provider.agent.AgentPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.device.DevicePersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.operations.OperationPersistenceProvider;
import org.apache.nifi.minifi.c2.core.exception.ResourceNotFoundException;
import org.apache.nifi.minifi.c2.model.Agent;
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.apache.nifi.minifi.c2.model.AgentManifest;
import org.apache.nifi.minifi.c2.model.Device;
import org.apache.nifi.minifi.c2.model.OperationRequest;
import org.apache.nifi.minifi.c2.model.OperationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class StandardC2Service implements C2Service {

    private static final Logger logger = LoggerFactory.getLogger(StandardC2Service.class);

    private final AgentPersistenceProvider agentPersistenceProvider;
    private final DevicePersistenceProvider devicePersistenceProvider;
    private final OperationPersistenceProvider operationPersistenceProvider;
    private final Validator validator;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    @Autowired
    public StandardC2Service(
            final AgentPersistenceProvider agentPersistenceProvider,
            final DevicePersistenceProvider devicePersistenceProvider,
            final OperationPersistenceProvider operationPersistenceProvider,
            final Validator validator) {
        this.agentPersistenceProvider = agentPersistenceProvider;
        this.devicePersistenceProvider = devicePersistenceProvider;
        this.operationPersistenceProvider = operationPersistenceProvider;
        this.validator = validator;
    }

    private <T> void validate(T t, String invalidMessage) {
        if (t == null) {
            throw new IllegalArgumentException(invalidMessage + ". Object cannot be null");
        }

        final Set<ConstraintViolation<T>> violations = validator.validate(t);
        if (violations.size() > 0) {
            throw new ConstraintViolationException(invalidMessage, violations);
        }
    }

    //**********************************
    //***  Agent Class CRUD methods  ***
    //**********************************

    @Override
    public List<AgentClass> getAgentClasses() {
        readLock.lock();
        try {
            return agentPersistenceProvider.getAgentClasses();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public AgentClass createAgentClass(AgentClass agentClass) {
        validate(agentClass, "Cannot create agent class");

        writeLock.lock();
        try {
            return agentPersistenceProvider.saveAgentClass(agentClass);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<AgentClass> getAgentClass(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        readLock.lock();
        try {
            return agentPersistenceProvider.getAgentClass(name);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public AgentClass updateAgentClass(AgentClass agentClass) {
        validate(agentClass, "Cannot update agent class");
        writeLock.lock();
        try {
            if (!agentPersistenceProvider.agentClassExists(agentClass.getName())) {
                throw new ResourceNotFoundException(
                        String.format("Agent class with name '%s' not found.", agentClass.getName()));
            }
            return agentPersistenceProvider.saveAgentClass(agentClass);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public AgentClass deleteAgentClass(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        writeLock.lock();
        try {
            Optional<AgentClass> deletedAgentClass = agentPersistenceProvider.getAgentClass(name);
            if (!deletedAgentClass.isPresent()) {
                throw new ResourceNotFoundException(
                        String.format("Agent class with name '%s' not found.", name));
            }
            agentPersistenceProvider.deleteAgentClass(name);
            return deletedAgentClass.get();
        } finally {
            writeLock.unlock();
        }
    }


    //*************************************
    //***  Agent Manifest CRUD methods  ***
    //*************************************

    @Override
    public AgentManifest createAgentManifest(AgentManifest agentManifest) {
        validate(agentManifest, "Could not create agent manifest");

        writeLock.lock();
        try {
            agentManifest.setIdentifier(UUID.randomUUID().toString());
            return agentPersistenceProvider.saveAgentManifest(agentManifest);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<AgentManifest> getAgentManifests() {
        readLock.lock();
        try {
            return agentPersistenceProvider.getAgentManifests();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<AgentManifest> getAgentManifests(String agentClassName) {
        if (agentClassName == null) {
            throw new IllegalArgumentException("Agent class name cannot be null");
        }

        readLock.lock();
        try {
            return agentPersistenceProvider.getAgentManifestsByClass(agentClassName);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<AgentManifest> getAgentManifest(String manifestId) {
        if (manifestId == null) {
            throw new IllegalArgumentException("Agent manifest id must not be null");
        }

        writeLock.lock();
        try {
            return agentPersistenceProvider.getAgentManifest(manifestId);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public AgentManifest deleteAgentManifest(String manifestId) {
        if (manifestId == null) {
            throw new IllegalArgumentException("Agent manifest id must not be null");
        }

        writeLock.lock();
        try {
            final Optional<AgentManifest> deletedAgentManifest = agentPersistenceProvider.getAgentManifest(manifestId);
            if (!deletedAgentManifest.isPresent()) {
                throw new ResourceNotFoundException(
                        String.format("Agent manifest with id '%s' not found.", manifestId));
            }
            agentPersistenceProvider.deleteAgentManifest(manifestId);
            return deletedAgentManifest.get();
        } finally {
            writeLock.unlock();
        }
    }


    //****************************
    //***  Agent CRUD methods  ***
    //****************************

    @Override
    public Agent createAgent(Agent agent) {
        validate(agent, "Cannot create agent");
        writeLock.lock();
        try {
            return agentPersistenceProvider.saveAgent(agent);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<Agent> getAgents() {
        readLock.lock();
        try {
            return agentPersistenceProvider.getAgents();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Agent> getAgents(String agentClassName) {
        if (agentClassName == null) {
            throw new IllegalArgumentException("agentClassName cannot be null");
        }

        readLock.lock();
        try {
            return agentPersistenceProvider.getAgentsByClassName(agentClassName);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<Agent> getAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }

        readLock.lock();
        try {
            return agentPersistenceProvider.getAgent(agentId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Agent updateAgent(Agent agent) {
        validate(agent, "Cannot update agent");

        writeLock.lock();
        try {
            final Optional<Agent> oldAgent = agentPersistenceProvider.getAgent(agent.getIdentifier());
            if (!oldAgent.isPresent()) {
                throw new ResourceNotFoundException("Agent not found with id " + agent.getIdentifier());
            }
            agent.setFirstSeen(oldAgent.get().getFirstSeen());  // firstSeen timestamp is immutable
            return agentPersistenceProvider.saveAgent(agent);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Agent deleteAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }

        writeLock.lock();
        try {
            final Optional<Agent> deletedAgent = agentPersistenceProvider.getAgent(agentId);
            if (!deletedAgent.isPresent()) {
                throw new ResourceNotFoundException("Agent not found with id " + agentId);
            }
            agentPersistenceProvider.deleteAgent(agentId);
            return deletedAgent.get();
        } finally {
            writeLock.unlock();
        }
    }


    //*****************************
    //***  Device CRUD methods  ***
    //*****************************

    @Override
    public Device createDevice(Device device) {
        validate(device, "Cannot create device");
        writeLock.lock();
        try {
            return devicePersistenceProvider.saveDevice(device);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<Device> getDevices() {
        readLock.lock();
        try {
            return devicePersistenceProvider.getDevices();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<Device> getDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("devicId cannot be null");
        }

        readLock.lock();
        try {
            return devicePersistenceProvider.getDevice(deviceId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Device updateDevice(Device device) {
        validate(device, "Cannot update device");

        writeLock.lock();
        try {
            final Optional<Device> oldDevice = devicePersistenceProvider.getDevice(device.getIdentifier());
            if (!oldDevice.isPresent()) {
                throw new ResourceNotFoundException("Device not found with id " + device.getIdentifier());
            }
            device.setFirstSeen(oldDevice.get().getFirstSeen());  // firstSeen timestamp is immutable
            return devicePersistenceProvider.saveDevice(device);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Device deleteDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("devicId cannot be null");
        }

        writeLock.lock();
        try {
            final Optional<Device> deletedDevice = devicePersistenceProvider.getDevice(deviceId);
            if (!deletedDevice.isPresent()) {
                throw new ResourceNotFoundException("Device not found with id " + deviceId);
            }
            devicePersistenceProvider.deleteDevice(deviceId);
            return deletedDevice.get();
        } finally {
            writeLock.unlock();
        }
    }


    //***********************************
    //***  C2 Operation CRUD methods  ***
    //***********************************

    @Override
    public OperationRequest createOperation(OperationRequest operationRequest) {
        validate(operationRequest, "Cannot create operation");
        operationRequest.getOperation().setIdentifier(UUID.randomUUID().toString());

        writeLock.lock();
        try {
            return operationPersistenceProvider.saveOperation(operationRequest);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<OperationRequest> getOperations() {
        readLock.lock();
        try {
            return operationPersistenceProvider.getOperations();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<OperationRequest> getOperationsByAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }

        readLock.lock();
        try {
            return operationPersistenceProvider.getOperationsByAgent(agentId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<OperationRequest> getOperation(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }

        readLock.lock();
        try {
            return operationPersistenceProvider.getOperation(operationId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public OperationRequest updateOperationState(String operationId, OperationState state) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }

        writeLock.lock();
        try {
            final Optional<OperationRequest> existingOperation = operationPersistenceProvider.getOperation(operationId);
            if (!existingOperation.isPresent()) {
                throw new ResourceNotFoundException("Operation not found with id " + operationId);
            }
            final OperationRequest updatedOperation = existingOperation.get();
            updatedOperation.setState(state);
            return operationPersistenceProvider.saveOperation(updatedOperation);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public OperationRequest deleteOperation(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }

        writeLock.lock();
        try {
            final Optional<OperationRequest> deletedOperation = operationPersistenceProvider.getOperation(operationId);
            if (!deletedOperation.isPresent()) {
                throw new ResourceNotFoundException("Operation not found with id " + operationId);
            }
            operationPersistenceProvider.deleteOperation(operationId);
            return deletedOperation.get();
        } finally {
            writeLock.unlock();
        }
    }
}
