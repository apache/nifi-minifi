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
import org.apache.nifi.minifi.c2.api.provider.agent.AgentPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.device.DevicePersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.heartbeat.HeartbeatPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.operations.OperationPersistenceProvider;
import org.apache.nifi.minifi.c2.model.Agent;
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.apache.nifi.minifi.c2.model.AgentManifest;
import org.apache.nifi.minifi.c2.model.C2Heartbeat;
import org.apache.nifi.minifi.c2.model.Device;
import org.apache.nifi.minifi.c2.model.OperationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple in-memory implementation of persistence providers in order to test the service layer.
 *
 * This is not designed for real use outside of development. For example:
 *   - it does not support transactions
 *   - it does not clone objects on save/retrieval, so any modifications made after interacting with this service
 *     also modify the "persisted" copies.
 *
 * TODO, deep copy objects on save/get so that they cannot be modified outside this class.
 */
// TODO, in the future, replace this Spring bean with a Provider Factory that provides configured providers
@Component
public class VolatilePersistenceProvider implements
        AgentPersistenceProvider,
        DevicePersistenceProvider,
        OperationPersistenceProvider,
        HeartbeatPersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatilePersistenceProvider.class);

    private Map<String, AgentClass> agentClasses = new ConcurrentHashMap<>();
    private Map<String, AgentManifest> agentManifests = new ConcurrentHashMap<>();
    private Map<String, Agent> agents = new ConcurrentHashMap<>();
    private Map<String, Device> devices = new ConcurrentHashMap<>();
    private Map<String, OperationRequest> operations = new ConcurrentHashMap<>();

    private VolatileHeartbeatPersistenceProvider heartbeatPersistenceProvider = new VolatileHeartbeatPersistenceProvider();

    @Override
    public void onConfigured(ProviderConfigurationContext configurationContext) throws ProviderCreationException {
    }

    //*** AgentPersistenceProvider ***

    @Override
    public long getAgentClassCount() {
        return agentClasses.size();
    }

    @Override
    public AgentClass saveAgentClass(AgentClass agentClass) {

        if (agentClass == null || agentClass.getName() == null) {
            throw new IllegalArgumentException("Agent Class must be not null and have a name in order to be saved.");
        }
        agentClasses.put(agentClass.getName(), agentClass);
        logger.debug("Saved AgentClass with name={}", agentClass.getName());
        return agentClass;

    }

    @Override
    public List<AgentClass> getAgentClasses() {
        return new ArrayList<>(agentClasses.values());
    }

    @Override
    public boolean agentClassExists(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        return agentClasses.containsKey(name);
    }

    @Override
    public Optional<AgentClass> getAgentClass(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        return Optional.ofNullable(agentClasses.get(name));
    }

    @Override
    public void deleteAgentClass(String name) {
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
    public long getAgentManifestCount() {
        return agentManifests.size();
    }

    @Override
    public AgentManifest saveAgentManifest(AgentManifest agentManifest) {
        if (agentManifest == null || agentManifest.getIdentifier() == null) {
            throw new IllegalArgumentException("Agent Manifest must be not null and have an id in order to be saved.");
        }
        agentManifests.put(agentManifest.getIdentifier(), agentManifest);
        return agentManifest;
    }

    @Override
    public List<AgentManifest> getAgentManifests() {
        return new ArrayList<>(agentManifests.values());
    }

    @Override
    public List<AgentManifest> getAgentManifestsByClass(String agentClassName) {
        if (agentClassName == null) {
            throw new IllegalArgumentException("Agent class name must not be null");
        }
        List<AgentManifest> agentManifestsForClass = new ArrayList<>();
        AgentClass agentClass = agentClasses.get(agentClassName);
        if (agentClass != null) {
            if (agentClass.getAgentManifests() != null) {
                agentClass.getAgentManifests().forEach(id -> {
                        AgentManifest manifest = agentManifests.get(id);
                        if (manifest != null) {
                            agentManifestsForClass.add(manifest);
                        }
                });
            }
        }
        return agentManifestsForClass;
    }

    @Override
    public Optional<AgentManifest> getAgentManifest(String agentManifestId) {
        if (agentManifestId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        return Optional.ofNullable(agentManifests.get(agentManifestId));
    }

    @Override
    public void deleteAgentManifest(String agentManifestId) {
        if (agentManifestId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        agentManifests.remove(agentManifestId);
    }

    @Override
    public long getAgentCount() {
        return agents.size();
    }

    @Override
    public Agent saveAgent(Agent agent) {
        if (agent == null || agent.getIdentifier() == null) {
            throw new IllegalArgumentException("Agent must be not null and have a name in order to be saved.");
        }
        agents.put(agent.getIdentifier(), agent);
        return agent;
    }

    @Override
    public List<Agent> getAgents() {
        return new ArrayList<>(agents.values());
    }

    @Override
    public List<Agent> getAgentsByClassName(String agentClassName) {
        return agents.values().stream().filter(agent -> agentClassName.equals(agent.getAgentClass())).collect(Collectors.toList());
    }

    @Override
    public Optional<Agent> getAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Id must be not null.");
        }
        return Optional.ofNullable(agents.get(agentId));
    }

    @Override
    public void deleteAgent(String agentId) {
        agents.remove(agentId);
    }


    //*** DevicePersistenceProvider ***

    @Override
    public long getDeviceCount() {
        return devices.size();
    }

    @Override
    public Device saveDevice(Device device) {
        if (device == null || device.getIdentifier() == null) {
            throw new IllegalArgumentException("Device must be not null and have an id in order to be saved.");
        }
        devices.put(device.getIdentifier(), device);
        return device;
    }

    @Override
    public List<Device> getDevices() {
        return new ArrayList<>(devices.values());
    }

    @Override
    public Optional<Device> getDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device id cannot be null");
        }
        return Optional.ofNullable(devices.get(deviceId));
    }

    @Override
    public void deleteDevice(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device id cannot be null");
        }
        devices.remove(deviceId);
    }


    //*** OperationPersistenceProvider ***

    @Override
    public long getOperationCount() {
        return operations.size();
    }


    @Override
    public OperationRequest saveOperation(OperationRequest operationRequest) {
        if (operationRequest == null || operationRequest.getOperation() == null || operationRequest.getOperation().getIdentifier() == null) {
            throw new IllegalArgumentException("operation must be not null and have id");
        }
        operations.put(operationRequest.getOperation().getIdentifier(), operationRequest);
        return operationRequest;
    }

    @Override
    public List<OperationRequest> getOperations() {
        return new ArrayList<>(operations.values());
    }

    @Override
    public List<OperationRequest> getOperationsByAgent(String agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Agent id cannot be null");
        }
        return operations.values().stream().filter(operation -> agentId.equals(operation.getTargetAgentIdentifier())).collect(Collectors.toList());
    }

    @Override
    public Optional<OperationRequest> getOperation(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        return Optional.ofNullable(operations.get(operationId));
    }

    @Override
    public void deleteOperation(String operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("operationId cannot be null");
        }
        operations.remove(operationId);
    }


    //*** HeartbeatPersistenceProvider ***

    @Override
    public C2Heartbeat saveHeartbeat(C2Heartbeat heartbeat) {
        return heartbeatPersistenceProvider.saveHeartbeat(heartbeat);
    }

    @Override
    public List<C2Heartbeat> getHeartbeats() {
        return heartbeatPersistenceProvider.getHeartbeats();
    }

    @Override
    public List<C2Heartbeat> getHeartbeatsByAgent(String agentId) {
        return heartbeatPersistenceProvider.getHeartbeatsByAgent(agentId);
    }

    @Override
    public List<C2Heartbeat> getHeartbeatsByDevice(String deviceId) {
        return heartbeatPersistenceProvider.getHeartbeatsByDevice(deviceId);
    }

    @Override
    public Optional<C2Heartbeat> getHeartbeat(String heartbeatId) {
        return heartbeatPersistenceProvider.getHeartbeat(heartbeatId);
    }

    @Override
    public void deleteHeartbeat(String heartbeatId) {
        heartbeatPersistenceProvider.deleteAllHeartbeats();
    }

    @Override
    public void deleteAllHeartbeats() {
        heartbeatPersistenceProvider.deleteAllHeartbeats();
    }
}
