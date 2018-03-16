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
package org.apache.nifi.minifi.c2.core.service.flow.mapping;

import org.apache.commons.lang3.Validate;
import org.apache.nifi.minifi.c2.core.service.flow.client.NiFiRegistryClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory that controls the creation of the FlowMapper bean. This factory doesn't follow the standard factory approach
 * of having a method annotated with @Bean because we want to lazily create the FlowMapper and not fail start-up if
 * the C2 server wasn't configured with a registry URL or bucket id.
 *
 * If we want to change implementations, or provide wrapped impls, the initializeFlowMapper() method can be modified.
 */
@Component
public class FlowMapperFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowMapperFactory.class);

    private final NiFiRegistryClientFactory niFiRegistryClientFactory;

    private volatile FlowMapper flowMapper;

    @Autowired
    public FlowMapperFactory(final NiFiRegistryClientFactory niFiRegistryClientFactory) {
        this.niFiRegistryClientFactory = niFiRegistryClientFactory;
        Validate.notNull(this.niFiRegistryClientFactory);
    }

    /**
     * Lazily creates a FlowMapper.
     *
     * @return the FlowMapper instance
     */
    public FlowMapper getFlowMapper() {
        if (flowMapper == null) {
            initializeFlowMapper();
        }

        return this.flowMapper;
    }

    private synchronized void initializeFlowMapper() {
        if (this.flowMapper != null) {
            return;
        }

        this.flowMapper = new SimpleFlowMapper(niFiRegistryClientFactory);
    }

}
