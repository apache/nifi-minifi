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
import org.apache.nifi.minifi.c2.api.provider.agent.AgentManifestPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.agent.AgentPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.device.DevicePersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.heartbeat.HeartbeatPersistenceProvider;
import org.apache.nifi.minifi.c2.api.provider.operations.OperationPersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO, replace this with a factory that is externally configurable
 */
@Configuration
public class VolatilePersistenceProviderFactory {

    private final VolatileAgentPersistenceProvider agentPersistenceProvider;
    private final VolatileAgentClassPersistenceProvider agentClassPersistenceProvider;
    private final VolatileAgentManifestPersistenceProvider agentManifestPersistenceProvider;
    private final VolatileDevicePersistenceProvider devicePersistenceProvider;
    private final VolatileHeartbeatPersistenceProvider heartbeatPersistenceProvider;
    private final VolatileOperationPersistenceProvider operationPersistenceProvider;

    public VolatilePersistenceProviderFactory() {
        agentPersistenceProvider = new VolatileAgentPersistenceProvider();
        agentClassPersistenceProvider = new VolatileAgentClassPersistenceProvider();
        agentManifestPersistenceProvider = new VolatileAgentManifestPersistenceProvider();
        devicePersistenceProvider = new VolatileDevicePersistenceProvider();
        heartbeatPersistenceProvider = new VolatileHeartbeatPersistenceProvider();
        operationPersistenceProvider = new VolatileOperationPersistenceProvider();
    }

    @Bean
    public DevicePersistenceProvider getDevicePersistenceProvider() {
        return devicePersistenceProvider;
    }

    @Bean
    public OperationPersistenceProvider getOperationPersistenceProvider() {
        return operationPersistenceProvider;
    }

    @Bean
    public HeartbeatPersistenceProvider getHeartBeatPersistenceProvider() {
        return heartbeatPersistenceProvider;
    }

    @Bean
    public AgentClassPersistenceProvider getAgentClassPersistenceProvider() {
       return agentClassPersistenceProvider;
    }

    @Bean
    public AgentManifestPersistenceProvider getAgentManifestPersistenceProvider() {
        return agentManifestPersistenceProvider;
    }

    @Bean
    public AgentPersistenceProvider getAgentPersistenceProvider() {
        return agentPersistenceProvider;
    }

}
