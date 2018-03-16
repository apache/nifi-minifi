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

import org.apache.nifi.minifi.c2.api.provider.heartbeat.HeartbeatPersistenceProvider
import org.apache.nifi.minifi.c2.model.*
import spock.lang.Specification

class StandardC2ProtocolServiceSpec extends Specification {

    def c2Service = Mock(C2Service)
    def heartbeatPersistenceProvider = Mock(HeartbeatPersistenceProvider)
    C2ProtocolService c2ProtocolService

    def setup() {
        c2ProtocolService = new StandardC2ProtocolService(c2Service, heartbeatPersistenceProvider)
    }

    def "process heartbeat"() {

        setup:
        C2Heartbeat heartbeat1 = createTestHeartbeat("agent1", "agentClass1")
        c2Service.getOperationsByAgent("agent1") >> Collections.emptyList()

        C2Heartbeat heartbeat2 = createTestHeartbeat("agent2", "agentClass2")
        c2Service.getOperationsByAgent("agent2") >> Collections.singletonList(createOperation("agent2", OperationState.QUEUED))

        c2Service.getAgent(_ as String) >> Optional.empty()
        c2Service.getAgentManifest(_ as String) >> Optional.empty()
        c2Service.getAgentClass(_ as String) >> Optional.empty()
        c2Service.getDevice(_ as String) >> Optional.empty()


        when: "heartbeat is processed while no operations are queued"
        def hbResponse1 = c2ProtocolService.processHeartbeat(heartbeat1)

        then: "empty heartbeat response is generated"
        with(hbResponse1) {
            requestedOperations == null
        }


        when: "heartbeat is processed while operations are queued"
        def hbResponse2 = c2ProtocolService.processHeartbeat(heartbeat2)

        then: "heartbeat response is generated with requested operations"
        with(hbResponse2) {
            requestedOperations.size() == 1
            requestedOperations.get(0).operation == OperationType.DESCRIBE
        }


        when: "heartbeat contains new agent manifest"
        c2ProtocolService.processHeartbeat(heartbeat1)

        then: "the agent manifest is registered"
        1 * c2Service.getAgentManifest(_ as String) >> Optional.empty()
        1 * c2Service.createAgentManifest(_ as AgentManifest)


        when: "heartbeat contains existing agent manifest"
        c2ProtocolService.processHeartbeat(heartbeat1)

        then: "the agent manifest is not registered"
        1 * c2Service.getAgentManifest(_ as String) >> { id -> Optional.of(new AgentManifest([identifier: id])) }
        0 * c2Service.createAgentManifest(*_)


        when: "heartbeat contains new agent class"
        c2ProtocolService.processHeartbeat(heartbeat1)

        then: "the agent class is registered"
        1 * c2Service.getAgentClass(_ as String) >> Optional.empty()
        1 * c2Service.createAgentClass(_ as AgentClass)


        when: "heartbeat contains existing agent class"
        c2ProtocolService.processHeartbeat(heartbeat1)

        then: "the existing agent class is update"
        1 * c2Service.getAgentClass(_ as String) >> {name -> Optional.of(new AgentClass([name: name])) }
        0 * c2Service.createAgentClass(_ as AgentClass)
        1 * c2Service.updateAgentClass(_ as AgentClass)


        when: "heartbeat contains new device"
        c2ProtocolService.processHeartbeat(heartbeat1)

        then: "the device is registered"
        1 * c2Service.getDevice(_ as String) >> Optional.empty()
        1 * c2Service.createDevice(_ as Device)


        when: "heartbeat contains existing device"
        c2ProtocolService.processHeartbeat(heartbeat1)

        then: "the device is not registered"
        1 * c2Service.getDevice(_ as String) >> { id -> Optional.of(new Device([identifier: id])) }
        0 * c2Service.createDevice(*_)

    }

    def "process ack"() {

        setup:
        C2OperationAck ack = new C2OperationAck([operationId: "operation1"])

        when: "operationAck is processed"
        c2ProtocolService.processOperationAck(ack)

        then: "operation state is updated"
        1 * c2Service.updateOperationState("operation1", OperationState.DONE)

    }


        // --- Helper methods

    def createTestHeartbeat(String agentId, String agentClass) {
        return new C2Heartbeat([
                identifier: "test-heartbeat-id",
                timestamp: 1514764800000L,
                deviceInfo: new DeviceInfo([
                        identifier: "test-device-id",
                ]),
                agentInfo: new AgentInfo([
                        identifier: agentId,
                        agentClass: agentClass,
                        agentManifest: new AgentManifest([
                                identifier: "test-agent-manifest-id"
                        ])
                ]),
                flowInfo: new FlowInfo([
                        flowId: "test-flow-id"
                ])
        ])
    }

    def createOperation(String targetAgentId, OperationState state) {
        return new OperationRequest([
                operation: new C2Operation([
                        identifier: "test-operation-id",
                        operation: OperationType.DESCRIBE
                ]),
                targetAgentIdentifier: targetAgentId,
                state: state
        ])
    }

}
