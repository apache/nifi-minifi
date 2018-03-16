/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.minifi.c2.core.service;

import org.apache.nifi.minifi.c2.api.provider.heartbeat.HeartbeatPersistenceProvider;
import org.apache.nifi.minifi.c2.model.Agent;
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.apache.nifi.minifi.c2.model.AgentInfo;
import org.apache.nifi.minifi.c2.model.C2Heartbeat;
import org.apache.nifi.minifi.c2.model.C2HeartbeatResponse;
import org.apache.nifi.minifi.c2.model.C2Operation;
import org.apache.nifi.minifi.c2.model.C2OperationAck;
import org.apache.nifi.minifi.c2.model.Device;
import org.apache.nifi.minifi.c2.model.DeviceInfo;
import org.apache.nifi.minifi.c2.model.OperationRequest;
import org.apache.nifi.minifi.c2.model.OperationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class StandardC2ProtocolService implements C2ProtocolService {

    private static final Logger logger = LoggerFactory.getLogger(StandardC2ProtocolService.class);

    private C2Service c2Service;
    private HeartbeatPersistenceProvider heartbeatPersistenceProvider;


    @Autowired
    public StandardC2ProtocolService(
            C2Service c2Service,
            HeartbeatPersistenceProvider heartbeatPersistenceProvider) {
        this.c2Service = c2Service;
        this.heartbeatPersistenceProvider = heartbeatPersistenceProvider;
    }

    @Override
    public C2HeartbeatResponse processHeartbeat(C2Heartbeat heartbeat) {

        if (heartbeat == null) {
            throw new IllegalArgumentException("Heartbeat cannot be null");
        }

        heartbeat.setTimestamp(System.currentTimeMillis());
        heartbeat.setIdentifier(UUID.randomUUID().toString());

        logger.info("Processing heartbeat: {}", heartbeat.toString());

        persistHeartbeat(heartbeat);
        processHeartbeatDeviceInfo(heartbeat);
        processHeartbeatAgentInfo(heartbeat);

        C2HeartbeatResponse response = new C2HeartbeatResponse();
        List<C2Operation> requestedOperations = new ArrayList<>(getQueuedC2Operations(heartbeat));
        // TODO, detect if NiFi Registry integration is configured,
        // and if so, call Flow Retrieval Service to detect if we need to add a Flow Update operation
        if (!requestedOperations.isEmpty()) {
            response.setRequestedOperations(requestedOperations);
            for (C2Operation op : requestedOperations) {
                try {
                    c2Service.updateOperationState(op.getIdentifier(), OperationState.DEPLOYED);
                } catch (Exception e) {
                    logger.warn("Encountered exception while updating operation state", e);
                }
            }
        }

        return response;
    }

    @Override
    public void processOperationAck(C2OperationAck operationAck) {

        try {
            // TODO, add operation status (eg success/failed) to operationAck. For now, assume ack indicates successful execution.
            c2Service.updateOperationState(operationAck.getOperationId(), OperationState.DONE);
        } catch (Exception e) {
            logger.warn("Encountered exception while processing operation ack", e);
        }

    }

    private void persistHeartbeat(C2Heartbeat heartbeat) {
        try {
            heartbeatPersistenceProvider.saveHeartbeat(heartbeat);
        } catch (Exception e) {
            logger.warn("Encountered exception while trying to record heartbeat", e);
        }
    }

    private void processHeartbeatDeviceInfo(C2Heartbeat heartbeat) {
        try {
            final String deviceIdentifier;
            final DeviceInfo deviceInfo = heartbeat.getDeviceInfo();
            if (deviceInfo != null) {
                deviceIdentifier = deviceInfo.getIdentifier();

                if (deviceIdentifier == null) {
                    logger.info("Could not register device without identifier: {} ", deviceInfo);
                    return;
                }

                logger.debug("Creating/updating device info for deviceId={}", deviceIdentifier);
                Optional<Device> existingDevice = c2Service.getDevice(deviceIdentifier);
                boolean deviceExists = (existingDevice.isPresent());
                Device device = existingDevice.orElse(new Device());
                if (!deviceExists) {
                    device.setIdentifier(deviceIdentifier);
                    device.setFirstSeen(heartbeat.getTimestamp());
                }
                device.setLastSeen(heartbeat.getTimestamp());
                device.setSystemInfo(deviceInfo.getSystemInfo());
                device.setNetworkInfo(deviceInfo.getNetworkInfo());

                if (!deviceExists) {
                    c2Service.createDevice(device);
                } else {
                    c2Service.updateDevice(device);
                }
            }
        } catch (Exception e) {
            logger.warn("Encountered exception while trying to update device info", e);
        }
    }

    private void processHeartbeatAgentInfo(C2Heartbeat heartbeat) {
        try {
            final String agentIdentifier;
            final AgentInfo agentInfo = heartbeat.getAgentInfo();
            if (agentInfo != null) {
                agentIdentifier = agentInfo.getIdentifier();

                if (agentIdentifier == null) {
                    logger.info("Could not register agent without identifier: {} ", agentInfo);
                    return;
                }

                logger.debug("Creating/updating agent info for agentId={}", agentIdentifier);
                Optional<Agent> existingAgent = c2Service.getAgent(agentIdentifier);
                boolean agentExists = existingAgent.isPresent();
                final Agent agent = existingAgent.orElse(new Agent());
                if (!agentExists) {
                    agent.setIdentifier(agentIdentifier);
                    agent.setFirstSeen(heartbeat.getTimestamp());
                }
                agent.setLastSeen(heartbeat.getTimestamp());
                if (agentInfo.getAgentClass() != null) {
                    agent.setAgentClass(agentInfo.getAgentClass());
                }
                if (agentInfo.getAgentManifest() != null) {
                    agent.setAgentManifest(agentInfo.getAgentManifest());
                }
                if (agentInfo.getStatus() != null) {
                    agent.setStatus(agentInfo.getStatus());
                }

                // Create agent manifest if this is the first time we've seen it
                String agentManifestIdentifier = null;
                if (agentInfo.getAgentManifest() != null) {
                    agent.setAgentManifest(agentInfo.getAgentManifest());
                    agentManifestIdentifier = agent.getAgentManifest().getIdentifier();

                    // Note, a client-set agent manifest identifier is required so that we don't create infinite manifests
                    // Alternatively, we need some way of deterministically generating a manifest id from the manifest contents,
                    // So that two heartbeats with a matching agent manifest do not register separate manifests
                    if (!c2Service.getAgentManifest(agentManifestIdentifier).isPresent()) {
                        c2Service.createAgentManifest(agent.getAgentManifest());
                    }
                }

                // Create agent class if this is the first time we've seen it
                if (agentInfo.getAgentClass() != null) {
                    agent.setAgentClass(agentInfo.getAgentClass());
                    Optional<AgentClass> existingAgentClass = c2Service.getAgentClass(agent.getAgentClass());
                    if (existingAgentClass.isPresent()) {
                        if (agentManifestIdentifier != null) {
                            AgentClass updatedAgentClass = existingAgentClass.get();
                            Set<String> agentManifests = updatedAgentClass.getAgentManifests();
                            if (agentManifests == null) {
                                agentManifests = new HashSet<>();
                            }
                            agentManifests.add(agentManifestIdentifier);
                            c2Service.updateAgentClass(updatedAgentClass);
                        }
                    } else {
                        AgentClass newAgentClass = new AgentClass();
                        newAgentClass.setName(agent.getAgentClass());
                        newAgentClass.setAgentManifests(agentManifestIdentifier != null ? new HashSet<>(Collections.singleton(agentManifestIdentifier)) : null);
                        c2Service.createAgentClass(newAgentClass);
                    }
                }

                if (!agentExists) {
                    c2Service.createAgent(agent);
                } else {
                    c2Service.updateAgent(agent);
                }
            }
        } catch (Exception e) {
            logger.warn("Encountered exception while trying to update agent info", e);
        }
    }

    private List<C2Operation> getQueuedC2Operations(C2Heartbeat heartbeat) {

        final List<C2Operation> queuedC2Operations = new ArrayList<>();
        if (heartbeat.getAgentInfo() != null) {
            final String agentIdentifier = heartbeat.getAgentInfo().getIdentifier();
            if (agentIdentifier != null) {
                for (OperationRequest op : c2Service.getOperationsByAgent(agentIdentifier)) {
                    if (op.getState() == OperationState.QUEUED) {
                        queuedC2Operations.add(op.getOperation());
                    }
                }
            }
        }
        return queuedC2Operations;

    }

}
