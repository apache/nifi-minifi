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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.minifi.c2.core.service.flow.client.NiFiRegistryClientFactory;
import org.apache.nifi.minifi.c2.model.FlowUri;
import org.apache.nifi.registry.client.FlowClient;
import org.apache.nifi.registry.client.NiFiRegistryClient;
import org.apache.nifi.registry.flow.VersionedFlow;

import java.util.List;
import java.util.Optional;

/**
 * A simple FlowMapper implementation that will return FlowMappings where the registry URL and bucket are fixed
 * values that are passed in from C2 properties, and the flow name follows a convention of being the agent class name.
 */
public class SimpleFlowMapper implements FlowMapper {

    private final NiFiRegistryClientFactory clientFactory;

    public SimpleFlowMapper(final NiFiRegistryClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        Validate.notNull(this.clientFactory);
    }

    @Override
    public Optional<FlowUri> getFlowMapping(final String agentClass) throws FlowMapperException {
        if (StringUtils.isBlank(agentClass)) {
            throw new IllegalArgumentException("Agent class cannot be null or blank");
        }

        try {
            final String registryUrl = clientFactory.getNiFiRegistryUrl();
            final String bucketId = clientFactory.getNiFiRegistryBucketId();

            final NiFiRegistryClient client = clientFactory.getClient();
            final FlowClient flowClient = client.getFlowClient();
            final List<VersionedFlow> flows = flowClient.getByBucket(bucketId);

            final Optional<String> flowId = flows.stream()
                    .filter(f -> f.getName().equals(agentClass))
                    .map(f -> f.getIdentifier())
                    .findFirst();

            if (flowId.isPresent()) {
                final FlowUri flowUri = new FlowUri(registryUrl, bucketId, flowId.get());
                return Optional.of(flowUri);
            } else {
                return Optional.empty();
            }

        } catch (final Exception e) {
            throw new FlowMapperException("Unable to get flow mapping for " + agentClass + " due to " + e.getMessage(), e);
        }
    }

}
