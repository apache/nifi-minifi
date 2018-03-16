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
package org.apache.nifi.minifi.c2.core.service.flow.retrieval;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.minifi.c2.core.service.flow.client.NiFiRegistryClientFactory;
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapper;
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapperException;
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapperFactory;
import org.apache.nifi.minifi.c2.model.FlowUri;
import org.apache.nifi.registry.client.NiFiRegistryClient;
import org.apache.nifi.registry.client.NiFiRegistryException;
import org.apache.nifi.registry.flow.VersionedFlowSnapshot;
import org.apache.nifi.registry.flow.VersionedFlowSnapshotMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Standard implementation of FlowRetrievalService.
 *
 * NOTE: Currently there is an assumption that a given C2 server only interacts with a single NiFi Registry instance, so
 * even though the FlowMapping instances coming from the FlowMapper have registry URL, it is assumed for now that this
 * URL will always be the same and thus we can avoid maintaining multiple registry clients for now.
 */
@Service
public class StandardFlowRetrievalService implements FlowRetrievalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardFlowRetrievalService.class);

    private final FlowMapperFactory flowMapperFactory;
    private final NiFiRegistryClientFactory clientFactory;

    @Autowired
    public StandardFlowRetrievalService(final FlowMapperFactory flowMapperFactory,
                                        final NiFiRegistryClientFactory clientFactory) {
        this.flowMapperFactory = flowMapperFactory;
        this.clientFactory = clientFactory;
        Validate.notNull(this.flowMapperFactory);
        Validate.notNull(this.clientFactory);
    }

    @Override
    public List<VersionedFlowSnapshotMetadata> getVersions(final String agentClass)
            throws IOException, ClassNotMappedException, FlowRetrievalException {

        if (StringUtils.isBlank(agentClass)) {
            throw new IllegalArgumentException("Agent class cannot be null or blank");
        }

        final FlowUri flowUri = getFlowUri(agentClass);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Getting flow versions for {} using flow mapping {}", new Object[] {agentClass, flowUri});
        }

        final NiFiRegistryClient client = clientFactory.getClient();
        try {
            return client.getFlowSnapshotClient().getSnapshotMetadata(flowUri.getBucketId(), flowUri.getFlowId());
        } catch (IOException ioe) {
            throw ioe;
        } catch (NiFiRegistryException nre) {
            throw new FlowRetrievalException("Error retrieving flow versions for " + agentClass + ": " + nre.getMessage(), nre);
        }
    }

    @Override
    public VersionedFlowSnapshot getFlow(final String agentClass, final int version)
            throws IOException, ClassNotMappedException, FlowRetrievalException {

        if (StringUtils.isBlank(agentClass)) {
            throw new IllegalArgumentException("Agent class cannot be null or blank");
        }

        if (version < 1) {
            throw new IllegalArgumentException("Version must be greater than or equal to 1");
        }

        final FlowUri flowUri = getFlowUri(agentClass);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Getting flow for {} using flow mapping {} and version {}", new Object[] {agentClass, flowUri, Integer.valueOf(version)});
        }

        final NiFiRegistryClient client = clientFactory.getClient();
        try {
            return client.getFlowSnapshotClient().get(flowUri.getBucketId(), flowUri.getFlowId(), version);
        } catch (IOException ioe) {
            throw ioe;
        } catch (NiFiRegistryException nre) {
            throw new FlowRetrievalException("Error retrieving flow for " + agentClass + ": " + nre.getMessage(), nre);
        }
    }

    @Override
    public VersionedFlowSnapshot getLatestFlow(final String agentClass)
            throws IOException, ClassNotMappedException, FlowRetrievalException {

        if (StringUtils.isBlank(agentClass)) {
            throw new IllegalArgumentException("Agent class cannot be null or blank");
        }

        final FlowUri flowUri = getFlowUri(agentClass);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Getting latest flow for {} using flow mapping {}", new Object[] {agentClass, flowUri});
        }

        final NiFiRegistryClient client = clientFactory.getClient();
        try {
            return client.getFlowSnapshotClient().getLatest(flowUri.getBucketId(), flowUri.getFlowId());
        } catch (IOException ioe) {
            throw ioe;
        } catch (NiFiRegistryException nre) {
            throw new FlowRetrievalException("Error retrieving latest flow for " + agentClass + ": " + nre.getMessage(), nre);
        }
    }

    /**
     * Gets the FlowMapping for the given agent class.
     *
     * @param agentClass the class to get the flow mapping for
     * @return the FlowUri for the agent class if one exists
     * @throws FlowRetrievalException if an error occurs obtaining the FlowMapping from the FlowMapper
     * @throws ClassNotMappedException if the FlowMapper does not contain a mapping for the given agent class
     */
    private FlowUri getFlowUri(final String agentClass) throws FlowRetrievalException, ClassNotMappedException {
        final Optional<FlowUri> flowUri;
        try {
            final FlowMapper flowMapper = flowMapperFactory.getFlowMapper();
            flowUri = flowMapper.getFlowMapping(agentClass);
        } catch (FlowMapperException fme) {
            throw new FlowRetrievalException("Error retrieving flow mapping for " + agentClass, fme);
        }

        if (flowUri.isPresent()) {
            return flowUri.get();
        } else {
            throw new ClassNotMappedException("No flow mapping exists for " + agentClass);
        }
    }

}
