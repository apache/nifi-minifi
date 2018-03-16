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
package org.apache.nifi.minifi.c2.core.service

import org.apache.nifi.minifi.c2.api.provider.agent.AgentPersistenceProvider
import org.apache.nifi.minifi.c2.api.provider.device.DevicePersistenceProvider
import org.apache.nifi.minifi.c2.api.provider.operations.OperationPersistenceProvider
import org.apache.nifi.minifi.c2.core.exception.ResourceNotFoundException
import org.apache.nifi.minifi.c2.core.provider.persistence.VolatilePersistenceProvider
import org.apache.nifi.minifi.c2.model.*
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation

class StandardC2ServiceSpec extends Specification{

    AgentPersistenceProvider agentPersistenceProvider
    DevicePersistenceProvider devicePersistenceProvider
    OperationPersistenceProvider operationPersistenceProvider
    C2Service c2Service

    def setup() {
        def persistenceProvider = new VolatilePersistenceProvider()
        agentPersistenceProvider = persistenceProvider
        devicePersistenceProvider = persistenceProvider
        operationPersistenceProvider = persistenceProvider
        def validator = Validation.buildDefaultValidatorFactory().getValidator();
        c2Service = new StandardC2Service(persistenceProvider, persistenceProvider, persistenceProvider, validator)
    }

    //**********************************
    //***  Agent Class CRUD methods  ***
    //**********************************

    def "create agent class"() {

        when: "arg is null"
        c2Service.createAgentClass(null)

        then: "exception is thrown"
        thrown IllegalArgumentException


        when: "class name is null"
        c2Service.createAgentClass(new AgentClass())

        then: "exception is thrown"
        thrown ConstraintViolationException


        when: "valid class is created"
        def createdClass = c2Service.createAgentClass(new AgentClass([name: "myClass", description: "myDescription"]))

        then: "created class is returned"
        with(createdClass) {
            name == "myClass"
            description == "myDescription"
        }

    }

    def "get agent classes"() {

        setup:
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "class1"]))
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "class2"]))
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "class3"]))

        when:
        def classes = c2Service.getAgentClasses()

        then:
        classes != null
        classes.size() == 3

    }

    def "get agent class"() {

        when: "class does not exist"
        def ac1 = c2Service.getAgentClass("myClass")

        then: "empty optional is returned"
        !ac1.isPresent()


        when: "class does exist"
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "myClass"]))
        def ac2 = c2Service.getAgentClass("myClass")

        then: "class is returned"
        ac2.isPresent()
        with(ac2.get()) {
            name == "myClass"
        }

    }

    def "update agent class"() {

        when: "class does not exist"
        c2Service.updateAgentClass(new AgentClass([name: "myClass", description: "new description"]))

        then:
        thrown ResourceNotFoundException


        when: "class exists"
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "myClass"]))
        def updatedClass = c2Service.updateAgentClass(new AgentClass([name: "myClass", description: "new description"]))

        then:
        with(updatedClass) {
            name == "myClass"
            description == "new description"
        }

    }

    def "delete agent class"() {

        when: "class does not exist"
        c2Service.deleteAgent("myClass")

        then:
        thrown ResourceNotFoundException


        when: "class exists"
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "myClass"]))
        def deletedClass = c2Service.deleteAgentClass("myClass")

        then:
        with(deletedClass) {
            name == "myClass"
        }

        and: "class no longer exists in persistence provider"
        agentPersistenceProvider.getAgentClassCount() == 0

    }


    //*************************************
    //***  Agent Manifest CRUD methods  ***
    //*************************************

    def "create agent manifest"() {

        when: "arg is null"
        c2Service.createAgentManifest(null)

        then: "exception is thrown"
        thrown IllegalArgumentException


        when: "valid manifest is created"
        def created = c2Service.createAgentManifest(new AgentManifest([agentType: "java"]))

        then: "manifest is created and assigned an id"
        with(created) {
            identifier != null
        }

    }

    def "get agent manifests"() {

        setup:
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "manifest1"]))
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "manifest2"]))
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "manifest3"]))

        when:
        def manifests = c2Service.getAgentManifests()

        then:
        manifests != null
        manifests.size() == 3

    }

    def "get agent manifests by class name"() {

        setup:
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "manifest1"]))
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "manifest2"]))
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "manifest3"]))
        agentPersistenceProvider.saveAgentClass(new AgentClass([name: "myClass", agentManifests: ["manifest2"]]))

        when:
        def manifests = c2Service.getAgentManifests("myClass")

        then:
        manifests != null
        manifests.size() == 1
        manifests.get(0).getIdentifier() == "manifest2"

    }

    def "get agent manifest"() {

        when: "manifest does not exist"
        def manifest1 = c2Service.getAgentManifest("myManifest")

        then: "empty optional is returned"
        !manifest1.isPresent()


        when: "manifest exists"
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "myManifest"]))
        def manifest2 = c2Service.getAgentManifest("myManifest")

        then: "manifest is returned"
        manifest2.isPresent()
        with(manifest2.get()) {
            identifier == "myManifest"
        }

    }

    def "delete agent manifest"() {

        when: "manifest does not exist"
        c2Service.deleteAgentManifest("myManifest")

        then: "empty optional is returned"
        thrown ResourceNotFoundException


        when: "manifest exists"
        agentPersistenceProvider.saveAgentManifest(new AgentManifest([identifier: "myManifest"]))
        def deleted = c2Service.deleteAgentManifest("myManifest")

        then: "manifest is returned"
        with(deleted) {
            identifier == "myManifest"
        }

        and: "manifest no longer exists in persistence provider"
        agentPersistenceProvider.getAgentManifestCount() == 0

    }


    //****************************
    //***  Agent CRUD methods  ***
    //****************************

    def "create agent"() {

        when: "arg is null"
        c2Service.createAgent(null)

        then: "exception is thrown"
        thrown IllegalArgumentException


        when: "valid agent is created"
        def created = c2Service.createAgent(new Agent([identifier: "agent1"]))

        then: "created agent is returned"
        with(created) {
            identifier == "agent1"
        }

    }

    def "get agents"() {

        setup:
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent1"]))
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent2"]))
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent3"]))

        when:
        def agents = c2Service.getAgents()

        then:
        agents.size() == 3

    }

    def "get agents by class name"() {

        setup:
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent1"]))
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent2", agentClass: "myClass"]))
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent3", agentClass: "yourClass"]))

        when:
        def agents = c2Service.getAgents("myClass")

        then:
        agents.size() == 1
        agents.get(0).identifier == "agent2"

    }

    def "get agent"() {

        when: "agent does not exist"
        def agent1 = c2Service.getAgent("agent1")

        then: "empty optional is returned"
        !agent1.isPresent()


        when: "agent exists"
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent2"]))
        def agent2 = c2Service.getAgent("agent2")

        then: "agent is returned"
        agent2.isPresent()
        with(agent2.get()) {
            identifier == "agent2"
        }

    }

    def "update agent"() {

        when: "agent does not exist"
        c2Service.updateAgent(new Agent([identifier: "agent1", name: "a better agent"]))

        then:
        thrown ResourceNotFoundException


        when: "agent exists"
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent1"]))
        def updated = c2Service.updateAgent(new Agent([identifier: "agent1", name: "a better agent"]))

        then:
        with(updated) {
            identifier == "agent1"
            name == "a better agent"
        }

    }

    def "delete agent"() {

        when: "agent does not exist"
        c2Service.deleteAgent("agent1")

        then:
        thrown ResourceNotFoundException


        when: "agent exists"
        agentPersistenceProvider.saveAgent(new Agent([identifier: "agent1"]))
        def deleted = c2Service.deleteAgent("agent1")

        then:
        with(deleted) {
            identifier == "agent1"
        }

    }

    //*****************************
    //***  Device CRUD methods  ***
    //*****************************

    def "create device"() {

        when: "arg is null"
        c2Service.createDevice(null)

        then: "exception is thrown"
        thrown IllegalArgumentException


        when: "arg is invalid"
        c2Service.createDevice(new Device())

        then: "exception is thrown"
        thrown ConstraintViolationException


        when: "valid device is created"
        def created = c2Service.createDevice(new Device([identifier: "device1"]))

        then: "created device is returned"
        with(created) {
            identifier == "device1"
        }

    }

    def "get devices"() {

        setup:
        devicePersistenceProvider.saveDevice(new Device([identifier: "device1"]))
        devicePersistenceProvider.saveDevice(new Device([identifier: "device2"]))
        devicePersistenceProvider.saveDevice(new Device([identifier: "device3"]))

        when:
        def devices = c2Service.getDevices()

        then:
        devices.size() == 3

    }

    def "get device"() {

        when: "device does not exist"
        def device1 = c2Service.getDevice("device1")

        then: "empty optional is returned"
        !device1.isPresent()


        when: "device exists"
        devicePersistenceProvider.saveDevice(new Device([identifier: "device2"]))
        def device2 = c2Service.getDevice("device2")

        then: "device is returned"
        device2.isPresent()
        with(device2.get()) {
            identifier == "device2"
        }

    }

    def "update device"() {

        when: "device does not exist"
        c2Service.updateDevice(new Device([identifier: "device1", name: "MiNiFi Device"]))

        then:
        thrown ResourceNotFoundException


        when: "agent exists"
        devicePersistenceProvider.saveDevice(new Device([identifier: "device1"]))
        def updated = c2Service.updateDevice(new Device([identifier: "device1", name: "MiNiFi Device"]))

        then:
        with(updated) {
            identifier == "device1"
            name == "MiNiFi Device"
        }

    }

    def "delete device"() {

        when: "device does not exist"
        c2Service.deleteDevice("device1")

        then:
        thrown ResourceNotFoundException


        when: "agent exists"
        devicePersistenceProvider.saveDevice(new Device([identifier: "device1"]))
        def deleted = c2Service.deleteDevice("device1")

        then:
        with(deleted) {
            identifier == "device1"
        }

    }


    //***********************************
    //***  C2 Operation CRUD methods  ***
    //***********************************

    def "create operaton"() {

        when: "arg is null"
        c2Service.createOperation(null)

        then: "exception is thrown"
        thrown IllegalArgumentException


        when: "arg is invalid"
        c2Service.createOperation(new OperationRequest())

        then: "exception is thrown"
        thrown ConstraintViolationException


        when: "valid operation is created"
        def created = c2Service.createOperation(
                new OperationRequest([
                        operation: new C2Operation([operation: OperationType.DESCRIBE]),
                        targetAgentIdentifier: "agent1",
                        state: OperationState.NEW
                ]))

        then: "created operation is returned, generated id"
        with(created) {
            targetAgentIdentifier == "agent1"
            operation.getIdentifier() != null
        }

    }

    def "get operations"() {

        setup:
        operationPersistenceProvider.saveOperation(new OperationRequest([
                operation: new C2Operation([identifier: "operation1", operation: OperationType.DESCRIBE]),
                targetAgentIdentifier: "agent1",
                state: OperationState.NEW
        ]))
        operationPersistenceProvider.saveOperation(new OperationRequest([
                operation: new C2Operation([identifier: "operation2", operation: OperationType.RESTART]),
                targetAgentIdentifier: "agent2",
                state: OperationState.NEW
        ]))

        when: "get all operations"
        def operations = c2Service.getOperations()

        then:
        operations.size() == 2

        when: "get operations for agent2"
        operations = c2Service.getOperationsByAgent("agent2")

        then:
        operations.size() == 1
        operations.get(0).operation.identifier == "operation2"

    }

    def "get operation"() {

        when: "operation does not exist"
        def operation1 = c2Service.getOperation("operation1")

        then: "empty optional is returned"
        !operation1.isPresent()


        when: "operation exists"
        operationPersistenceProvider.saveOperation(new OperationRequest([
                operation: new C2Operation([identifier: "operation1", operation: OperationType.DESCRIBE]),
                targetAgentIdentifier: "agent1",
                state: OperationState.NEW
        ]))
        def operation2 = c2Service.getOperation("operation1")

        then: "operation is returned"
        operation2.isPresent()
        with(operation2.get()) {
            operation.identifier == "operation1"
        }

    }

    def "update operation state"() {

        when: "operation does not exist"
        c2Service.updateOperationState("operation1", OperationState.DONE)

        then:
        thrown ResourceNotFoundException


        when: "operation exists"
        operationPersistenceProvider.saveOperation(new OperationRequest([
                operation: new C2Operation([identifier: "operation1", operation: OperationType.DESCRIBE]),
                targetAgentIdentifier: "agent1",
                state: OperationState.NEW
        ]))
        def updated = c2Service.updateOperationState("operation1", OperationState.DONE)

        then:
        with(updated) {
            state == OperationState.DONE
        }

    }

    def "delete operation"() {

        when: "operation does not exist"
        c2Service.deleteOperation("operation1")

        then:
        thrown ResourceNotFoundException


        when: "operation exists"
        operationPersistenceProvider.saveOperation(new OperationRequest([
                operation: new C2Operation([identifier: "operation1", operation: OperationType.DESCRIBE]),
                targetAgentIdentifier: "agent1",
                state: OperationState.NEW
        ]))
        def deleted = c2Service.deleteOperation("operation1")

        then:
        with(deleted) {
            operation.identifier == "operation1"
        }

    }

}
