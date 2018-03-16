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

import org.apache.nifi.minifi.c2.core.service.flow.client.NiFiRegistryClientFactory;
import org.apache.nifi.minifi.c2.model.FlowUri;
import org.apache.nifi.registry.client.FlowClient;
import org.apache.nifi.registry.client.NiFiRegistryClient;
import org.apache.nifi.registry.client.NiFiRegistryException;
import org.apache.nifi.registry.flow.VersionedFlow;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSimpleFlowMapper {

    static final String REGISTRY_URL = "http://localhost:18080";
    static final String BUCKET_ID = UUID.randomUUID().toString();

    private NiFiRegistryClient client;
    private FlowClient flowClient;

    private NiFiRegistryClientFactory clientFactory;

    private FlowMapper flowMapper;

    @Before
    public void setup() {
        flowClient = mock(FlowClient.class);

        client = mock(NiFiRegistryClient.class);
        when(client.getFlowClient()).thenReturn(flowClient);

        clientFactory = mock(NiFiRegistryClientFactory.class);
        when(clientFactory.getClient()).thenReturn(client);
        when(clientFactory.getNiFiRegistryUrl()).thenReturn(REGISTRY_URL);
        when(clientFactory.getNiFiRegistryBucketId()).thenReturn(BUCKET_ID);

        flowMapper = new SimpleFlowMapper(clientFactory);
    }

    @Test
    public void testGetFlowMappingWhenAgentClassExists() throws FlowMapperException, IOException, NiFiRegistryException {
        final String agentClass = "Class A";

        final VersionedFlow versionedFlow = new VersionedFlow();
        versionedFlow.setName(agentClass);
        versionedFlow.setIdentifier(UUID.randomUUID().toString());
        versionedFlow.setBucketIdentifier(BUCKET_ID);

        when(flowClient.getByBucket(BUCKET_ID)).thenReturn(Collections.singletonList(versionedFlow));

        final Optional<FlowUri> flowUri = flowMapper.getFlowMapping(agentClass);
        assertNotNull(flowUri);
        assertTrue(flowUri.isPresent());
        assertEquals(REGISTRY_URL, flowUri.get().getRegistryUrl());
        assertEquals(BUCKET_ID, flowUri.get().getBucketId());
        assertEquals(versionedFlow.getIdentifier(), flowUri.get().getFlowId());
    }

    @Test
    public void testGetFlowMappingWhenAgentClassDoesNotExist() throws FlowMapperException, IOException, NiFiRegistryException {
        final String agentClass = "Class A";

        final VersionedFlow versionedFlow = new VersionedFlow();
        versionedFlow.setName("SOME-OTHER-FLOW");
        versionedFlow.setIdentifier(UUID.randomUUID().toString());
        versionedFlow.setBucketIdentifier(BUCKET_ID);

        when(flowClient.getByBucket(BUCKET_ID)).thenReturn(Collections.singletonList(versionedFlow));

        final Optional<FlowUri> flowUri = flowMapper.getFlowMapping(agentClass);
        assertNotNull(flowUri);
        assertFalse(flowUri.isPresent());
    }

    @Test(expected = FlowMapperException.class)
    public void testGetFlowMappingWhenExceptionHappens() throws FlowMapperException, IOException, NiFiRegistryException {
        final String agentClass = "Class A";
        when(flowClient.getByBucket(BUCKET_ID)).thenThrow(new NiFiRegistryException("Bucket not found"));
        flowMapper.getFlowMapping(agentClass);
    }

}
