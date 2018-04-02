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

import org.apache.nifi.minifi.c2.api.provider.agent.AgentClassPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.agent.AgentManifestPersistenceProvider;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class StandardC2Service implements C2Service {

    private static final Logger logger = LoggerFactory.getLogger(StandardC2Service.class);

    private final AgentPersistenceProvider agentPersistenceProvider;
    private final AgentClassPersistenceProvider agentClassPersistenceProvider;
    private final AgentManifestPersistenceProvider agentManifestPersistenceProvider;
    private final DevicePersistenceProvider devicePersistenceProvider;
    private final OperationPersistenceProvider operationPersistenceProvider;
    private final Validator validator;

    private final Lock agentLock = new ReentrantLock();
    private final Lock agentClassLock = new ReentrantLock();
    private final Lock agentManifestLock = new ReentrantLock();
    private final Lock deviceLock = new ReentrantLock();
    private final Lock operationLock = new ReentrantLock();

    @Autowired
    public StandardC2Service(
            final AgentPersistenceProvider agentPersistenceProviderFactory,
            final AgentClassPersistenceProvider agentClassPersistenceProvider,
            final AgentManifestPersistenceProvider agentManifestPersistenceProvider,
            final DevicePersistenceProvider devicePersistenceProvider,
            final OperationPersistenceProvider operationPersistenceProvider,
            final Validator validator) {
        this.agentPersistenceProvider = agentPersistenceProviderFactory;
        this.agentClassPersistenceProvider = agentClassPersistenceProvider;
        this.agentManifestPersistenceProvider = agentManifestPersistenceProvider;
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
        return iterableToList(agentClassPersistenceProvider.getAll());
    }

    @Override
    public AgentClass createAgentClass(AgentClass agentClass) {
        validate(agentClass, "Cannot create agent class");

        agentClassLock.lock();
        try {
            if (agentClassPersistenceProvider.existsById(agentClass.getName())) {
                throw new IllegalStateException(
                        String.format("Agent class not found with name='%s'", agentClass.getName()));
            }
            return agentClassPersistenceProvider.save(agentClass);
        } finally {
            agentClassLock.unlock();
        }
    }

    @Override
    public Optional<AgentClass> getAgentClass(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        return agentClassPersistenceProvider.getById(name);
    }

    @Override
    public AgentClass updateAgentClass(AgentClass agentClass) {
        validate(agentClass, "Cannot update agent class");
        agentClassLock.lock();
        try {
            if (!agentClassPersistenceProvider.existsById(agentClass.getName())) {
                throw new ResourceNotFoundException(
                        String.format("Agent class not found with name='%s'", agentClass.getName()));
            }
            return agentClassPersistenceProvider.save(agentClass);
        } finally {
            agentClassLock.unlock();
        }
    }

    @Override
    public AgentClass deleteAgentClass(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        agentClassLock.lock();
        try {
            Optional<AgentClass> deletedAgentClass = agentClassPersistenceProvider.getById(name);
            if (!deletedAgentClass.isPresent()) {
                throw new ResourceNotFoundException(
                        String.format("Agent class not found with name='%s'", name));
            }
            agentClassPersistenceProvider.deleteById(name);
            return deletedAgentClass.get();
        } finally {
            agentClassLock.unlock();
        }
    }


    //*************************************
    //***  Agent Manifest CRUD methods  ***
    //*************************************

    @Override
    public AgentManifest createAgentManifest(AgentManifest agentManifest) {
        validate(agentManifest, "Could not create agent manifest");
        if (agentManifest.getIdentifier() == null) {
            agentManifest.setIdentifier(UUID.randomUUID().toString());
        }

        agentManifestLock.lock();
        try {
            if (agentManifestPersistenceProvider.existsById(agentManifest.getIdentifier())) {
                throw new IllegalStateException(
                        String.format("Agent manifest already exists with identifier='%s", agentManifest.getIdentifier()));
            }
            return agentManifestPersistenceProvider.save(agentManifest);
        } finally {
            agentManifestLock.unlock();
        }
    }

    @Override
    public List<AgentManifest> getAgentManifests() {
        return iterableToList(agentManifestPersistenceProvider.getAll());
    }

    @Override
    public List<AgentManifest> getAgentManifests(String agentClassName) {
        if (agentClassName == null) {
            throw new IllegalArgumentException("Agent class name cannot be null");
        }

        Optional<AgentClass> agentClass = agentClassPersistenceProvider.getById(agentClassName);
        if (agentClass.isPresent()) {
            Iterable<String> manifestIds = agentClass.get().getAgentManifests();
            if (manifestIds != null) {
                return iterableToList(agentManifestPersistenceProvider.getAllById(manifestIds));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<AgentManifest> getAgentManifest(String manifestId) {
        if (manifestId == null) {
            throw new IllegalArgumentException("Agent manifest id must not be null");
        }

        return agentManifestPersistenceProvider.getById(manifestId);
    }

    @Override
    public AgentManifest deleteAgentManifest(String manifestId) {
        if (manifestId == null) {
            throw new IllegalArgumentException("Agent manifest id must not be null");
        }

        agentManifestLock.lock();
        try {
            final Optional<AgentManifest> deletedAgentManifest = agentManifestPersistenceProvider.getById(manifestId);
            if (!deletedAgentManifest.isPresent()) {
                throw new ResourceNotFoundException(
                        String.format("Agent manifest with id '%s' not found.", manifestId));
            }
            agentManifestPersistenceProvider.deleteById(manifestId);
            return deletedAgentManifest.get();
        } finally {
            agentManifestLock.unlock();
        }
    }


    //****************************
    //***  Agent CRUD methods  ***
    //****************************

    @Override
    public Agent createAgent(Agent agent) {
        validate(agent, "Cannot create agent");
        agentLock.lock();
        try {
            if (agentPersistenceProvider.existsById(agent.getIdentifier())) {
                throw new IllegalStateException(
                        String.format("Agent not found with identifier='%s'", agent.getIdentifier()));
            }
            return agentPersistenceProvider.save(agent);
        } finally {
            agentLock.unlock();
        }
    }

    @Override
    public List<Agent> getAgents() {
        return iterableToList(agentPersistenceProvider.getAll());
    }

    @Override
    public List<Agent> getAgents(String agentClassName) {
        if (agentClassName == null) {
            throw new IllegalArgumentException("agentClassName cannot be null");
        }

        return iterableToList(agentPersistenceProvider.getByClassName(agentClassName));
    }

    @Override
    public Optional<Agent> getAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }

        return agentPersistenceProvider.getById(agentId);
    }

    @Override
    public Agent updateAgent(Agent agent) {
        validate(agent, "Cannot update agent");

        agentLock.lock();
        try {
            final Optional<Agent> oldAgent = agentPersistenceProvider.getById(agent.getIdentifier());
            if (!oldAgent.isPresent()) {
                throw new ResourceNotFoundException("Agent not found with id " + agent.getIdentifier());
            }
            agent.setFirstSeen(oldAgent.get().getFirstSeen());  // firstSeen timestamp is immutable
            return agentPersistenceProvider.save(agent);
        } finally {
            agentLock.unlock();
        }
    }

    @Override
    public Agent deleteAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }

        agentLock.lock();
        try {
            final Optional<Agent> deletedAgent = agentPersistenceProvider.getById(agentId);
            if (!deletedAgent.isPresent()) {
                throw new ResourceNotFoundException("Agent not found with id " + agentId);
            }
            agentPersistenceProvider.deleteById(agentId);
            return deletedAgent.get();
        } finally {
            agentLock.unlock();
        }
    }


    //*****************************
    //***  Device CRUD methods  ***
    //*****************************

    @Override
    public Device createDevice(Device device) {
        validate(device, "Cannot create device");
        deviceLock.lock();
        try {
            if (devicePersistenceProvider.existsById(device.getIdentifier())) {
                throw new IllegalStateException(
                        String.format("Device already exists with id='%s", device.getIdentifier()));
            }
            return devicePersistenceProvider.save(device);
        } finally {
            deviceLock.unlock();
        }
    }

    @Override
    public List<Device> getDevices() {
        return iterableToList(devicePersistenceProvider.getAll());
    }

    @Override
    public Optional<Device> getDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("devicId cannot be null");
        }

        return devicePersistenceProvider.getById(deviceId);
    }

    @Override
    public Device updateDevice(Device device) {
        validate(device, "Cannot update device");

        deviceLock.lock();
        try {
            final Optional<Device> oldDevice = devicePersistenceProvider.getById(device.getIdentifier());
            if (!oldDevice.isPresent()) {
                throw new ResourceNotFoundException("Device not found with id " + device.getIdentifier());
            }
            device.setFirstSeen(oldDevice.get().getFirstSeen());  // firstSeen timestamp is immutable
            return devicePersistenceProvider.save(device);
        } finally {
            deviceLock.unlock();
        }
    }

    @Override
    public Device deleteDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("devicId cannot be null");
        }

        deviceLock.lock();
        try {
            final Optional<Device> deletedDevice = devicePersistenceProvider.getById(deviceId);
            if (!deletedDevice.isPresent()) {
                throw new ResourceNotFoundException("Device not found with id " + deviceId);
            }
            devicePersistenceProvider.deleteById(deviceId);
            return deletedDevice.get();
        } finally {
            deviceLock.unlock();
        }
    }


    //***********************************
    //***  C2 Operation CRUD methods  ***
    //***********************************

    @Override
    public OperationRequest createOperation(OperationRequest operationRequest) {
        validate(operationRequest, "Cannot create operation");
        operationRequest.getOperation().setIdentifier(UUID.randomUUID().toString());

        return operationPersistenceProvider.save(operationRequest);
    }

    @Override
    public List<OperationRequest> getOperations() {
        return iterableToList(operationPersistenceProvider.getAll());
    }

    @Override
    public List<OperationRequest> getOperationsByAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId cannot be null");
        }

        return iterableToList(operationPersistenceProvider.getByAgent(agentId));
    }

    @Override
    public Optional<OperationRequest> getOperation(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }

        return operationPersistenceProvider.getById(operationId);
    }

    @Override
    public OperationRequest updateOperationState(String operationId, OperationState state) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }

        operationLock.lock();
        try {
            final Optional<OperationRequest> existingOperation = operationPersistenceProvider.getById(operationId);
            if (!existingOperation.isPresent()) {
                throw new ResourceNotFoundException("Operation not found with id " + operationId);
            }
            final OperationRequest updatedOperation = existingOperation.get();
            logger.debug("C2 operation state transition for operationId={}, fromState={}, toState={}", operationId, updatedOperation.getState(), state);
            updatedOperation.setState(state);
            return operationPersistenceProvider.save(updatedOperation);
        } finally {
            operationLock.unlock();
        }
    }

    @Override
    public OperationRequest deleteOperation(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }

        operationLock.lock();
        try {
            final Optional<OperationRequest> deletedOperation = operationPersistenceProvider.getById(operationId);
            if (!deletedOperation.isPresent()) {
                throw new ResourceNotFoundException("Operation not found with id " + operationId);
            }
            operationPersistenceProvider.deleteById(operationId);
            return deletedOperation.get();
        } finally {
            operationLock.unlock();
        }
    }

    private static <T> List<T> iterableToList(Iterable<T> iterable) {
        final List<T> retList;
        if (iterable instanceof List) {
           retList = (List<T>)iterable;
        } else if (iterable instanceof Collection) {
            retList = new ArrayList<>((Collection<T>)iterable);
        } else {
            retList = new ArrayList<>();
            iterable.forEach(retList::add);
        }
        return retList;
    }

}
