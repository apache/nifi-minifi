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

import org.apache.nifi.minifi.c2.model.Agent;
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.apache.nifi.minifi.c2.model.AgentManifest;
import org.apache.nifi.minifi.c2.model.Device;
import org.apache.nifi.minifi.c2.model.OperationRequest;
import org.apache.nifi.minifi.c2.model.OperationState;

import java.util.List;
import java.util.Optional;

/**
 *  Standard CRUD method semantics apply to these methods. That is:
 *
 *    - getWidgets: return List of Widget,
 *                  or empty List if no Widgets exist
 *
 *    - getWidget(String): return Optional Widget with matching id,
 *                         or empty Optional if no Widget with matching id exists
 *
 *    - createWidget(Widget): create Widget and assign it a generated id,
 *                            return created widget (including any fields that got generated such as id or creation timestamp),
 *                            throw IllegalStateException if Widget with matching id already exists
 *                            throw IllegalArgumentException if Widget is not valid (e.g., missing required fields)
 *
 *    - updateWidget(Widget): update Widget with the id to match the incoming Widget
 *                            return updated Widget
 *                            throw IllegalArgumentException if Widget is not valid (e.g., missing required fields. Note, id is required when updating existing Widget)
 *                            throw ResourceNotFoundException if no Widget with matching id exists
 *
 *    - deleteWidget(String): delete Widget with id,
 *                            return Widget that was deleted,
 *                            throw ResourceNotFoundException if no Widget with matching id exists
 *
 *  Any invalid arguments (eg, null where required) will result in an IllegalArgumentException
 */
public interface C2Service {

    //**********************************
    //***  Agent Class CRUD methods  ***
    //**********************************

    AgentClass createAgentClass(AgentClass agentClass);
    List<AgentClass> getAgentClasses();
    Optional<AgentClass> getAgentClass(String name);
    AgentClass updateAgentClass(AgentClass agentClass);
    AgentClass deleteAgentClass(String name);


    //*************************************
    //***  Agent Manifest CRUD methods  ***
    //*************************************

    AgentManifest createAgentManifest(AgentManifest agentManifest);
    List<AgentManifest> getAgentManifests();
    List<AgentManifest> getAgentManifests(String agentClassName);
    Optional<AgentManifest> getAgentManifest(String manifestId);
    AgentManifest deleteAgentManifest(String manifestId);


    //****************************
    //***  Agent CRUD methods  ***
    //****************************

    Agent createAgent(Agent agent);
    List<Agent> getAgents();
    List<Agent> getAgents(String agentClassName);
    Optional<Agent> getAgent(String agentId);
    Agent updateAgent(Agent agent);
    Agent deleteAgent(String agentId);


    //*****************************
    //***  Device CRUD methods  ***
    //*****************************

    Device createDevice(Device device);
    List<Device> getDevices();
    Optional<Device> getDevice(String deviceId);
    Device updateDevice(Device device);
    Device deleteDevice(String deviceId);


    //***********************************
    //***  C2 Operation CRUD methods  ***
    //***********************************

    OperationRequest createOperation(OperationRequest operationRequest);
    List<OperationRequest> getOperations();
    List<OperationRequest> getOperationsByAgent(String agentId);
    Optional<OperationRequest> getOperation(String operationId);
    OperationRequest updateOperationState(String operationId, OperationState state);
    OperationRequest deleteOperation(String operationId);

}
