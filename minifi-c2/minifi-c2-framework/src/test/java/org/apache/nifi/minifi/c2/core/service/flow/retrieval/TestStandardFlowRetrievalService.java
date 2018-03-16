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

import org.apache.nifi.minifi.c2.core.service.flow.client.NiFiRegistryClientFactory;
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapper;
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapperException;
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapperFactory;
import org.apache.nifi.minifi.c2.model.FlowUri;
import org.apache.nifi.registry.client.FlowSnapshotClient;
import org.apache.nifi.registry.client.NiFiRegistryClient;
import org.apache.nifi.registry.client.NiFiRegistryException;
import org.apache.nifi.registry.flow.VersionedFlowSnapshot;
import org.apache.nifi.registry.flow.VersionedFlowSnapshotMetadata;
import org.apache.nifi.registry.flow.VersionedProcessGroup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStandardFlowRetrievalService {

    private static final String CLASS_A = "Class A";
    private static final String CLASS_B = "Class B";

    private FlowMapperFactory flowMapperFactory;
    private FlowMapper flowMapper;

    private NiFiRegistryClientFactory clientFactory;
    private NiFiRegistryClient client;
    private FlowSnapshotClient flowSnapshotClient;

    private FlowRetrievalService flowRetrievalService;

    @Before
    public void setup() {
        flowMapper = mock(FlowMapper.class);

        flowMapperFactory = mock(FlowMapperFactory.class);
        when(flowMapperFactory.getFlowMapper()).thenReturn(flowMapper);

        flowSnapshotClient = mock(FlowSnapshotClient.class);

        client = mock(NiFiRegistryClient.class);
        when(client.getFlowSnapshotClient()).thenReturn(flowSnapshotClient);

        clientFactory = mock(NiFiRegistryClientFactory.class);
        when(clientFactory.getClient()).thenReturn(client);

        flowRetrievalService = new StandardFlowRetrievalService(flowMapperFactory, clientFactory);
    }

    @Test
    public void testRetrieveLatestWhenVersionsExist() throws ClassNotMappedException, IOException, FlowRetrievalException, FlowMapperException, NiFiRegistryException {
        final FlowUri flowUri = new FlowUri("http://localhost:18080", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.of(flowUri));

        final VersionedFlowSnapshot flowSnapshot = createSnapshot(flowUri.getBucketId(), flowUri.getFlowId(), 2);
        when(flowSnapshotClient.getLatest(flowUri.getBucketId(), flowUri.getFlowId())).thenReturn(flowSnapshot);

        final VersionedFlowSnapshot returnedFlowSnapshot = flowRetrievalService.getLatestFlow(CLASS_A);
        assertNotNull(returnedFlowSnapshot);
        assertEquals(flowSnapshot.getSnapshotMetadata().getBucketIdentifier(), returnedFlowSnapshot.getSnapshotMetadata().getBucketIdentifier());
        assertEquals(flowSnapshot.getSnapshotMetadata().getFlowIdentifier(), returnedFlowSnapshot.getSnapshotMetadata().getFlowIdentifier());
        assertEquals(flowSnapshot.getSnapshotMetadata().getVersion(), returnedFlowSnapshot.getSnapshotMetadata().getVersion());
    }

    @Test(expected = FlowRetrievalException.class)
    public void testRetrieveLatestWhenNoVersions() throws ClassNotMappedException, IOException, FlowRetrievalException, FlowMapperException, NiFiRegistryException {
        final FlowUri flowUri = new FlowUri("http://localhost:18080", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.of(flowUri));

        when(flowSnapshotClient.getLatest(flowUri.getBucketId(), flowUri.getFlowId())).thenThrow(new NiFiRegistryException("No Versions"));

        flowRetrievalService.getLatestFlow(CLASS_A);
    }

    @Test(expected = ClassNotMappedException.class)
    public void testRetrieveLatestWhenNoFlowForClass() throws ClassNotMappedException, IOException, FlowRetrievalException, FlowMapperException {
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.empty());
        flowRetrievalService.getLatestFlow(CLASS_A);
    }

    @Test
    public void testRetrieveSpecificWhenVersionsExist() throws ClassNotMappedException, IOException, FlowRetrievalException, NiFiRegistryException, FlowMapperException {
        final FlowUri flowUri = new FlowUri("http://localhost:18080", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.of(flowUri));

        final int version = 2;
        final VersionedFlowSnapshot flowSnapshot = createSnapshot(flowUri.getBucketId(), flowUri.getFlowId(), version);
        when(flowSnapshotClient.get(flowUri.getBucketId(), flowUri.getFlowId(), version)).thenReturn(flowSnapshot);

        final VersionedFlowSnapshot returnedFlowSnapshot = flowRetrievalService.getFlow(CLASS_A, version);
        assertNotNull(returnedFlowSnapshot);
        assertEquals(flowSnapshot.getSnapshotMetadata().getBucketIdentifier(), returnedFlowSnapshot.getSnapshotMetadata().getBucketIdentifier());
        assertEquals(flowSnapshot.getSnapshotMetadata().getFlowIdentifier(), returnedFlowSnapshot.getSnapshotMetadata().getFlowIdentifier());
        assertEquals(flowSnapshot.getSnapshotMetadata().getVersion(), returnedFlowSnapshot.getSnapshotMetadata().getVersion());
    }

    @Test(expected = FlowRetrievalException.class)
    public void testRetrieveSpecificWhenNoVersions() throws ClassNotMappedException, IOException, FlowRetrievalException, NiFiRegistryException, FlowMapperException {
        final FlowUri flowUri = new FlowUri("http://localhost:18080", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.of(flowUri));

        final int version = 2;
        when(flowSnapshotClient.get(flowUri.getBucketId(), flowUri.getFlowId(), version)).thenThrow(new NiFiRegistryException("No versions"));

        flowRetrievalService.getFlow(CLASS_A, version);
    }

    @Test(expected = ClassNotMappedException.class)
    public void testRetrieveSpecificWhenNoFlowForClass() throws ClassNotMappedException, IOException, FlowRetrievalException, FlowMapperException {
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.empty());
        flowRetrievalService.getFlow(CLASS_A, 2);
    }

    @Test
    public void testRetrieveVersionsListWhenVersionsExist() throws ClassNotMappedException, IOException, FlowRetrievalException, FlowMapperException, NiFiRegistryException {
        final FlowUri flowUri = new FlowUri("http://localhost:18080", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.of(flowUri));

        final VersionedFlowSnapshot flowSnapshot1 = createSnapshot(flowUri.getBucketId(), flowUri.getFlowId(), 1);
        final VersionedFlowSnapshot flowSnapshot2 = createSnapshot(flowUri.getBucketId(), flowUri.getFlowId(), 2);

        when(flowSnapshotClient.getSnapshotMetadata(flowUri.getBucketId(), flowUri.getFlowId()))
                .thenReturn(Arrays.asList(
                        flowSnapshot1.getSnapshotMetadata(),
                        flowSnapshot2.getSnapshotMetadata()));

        final List<VersionedFlowSnapshotMetadata> returnedVersions = flowRetrievalService.getVersions(CLASS_A);
        assertNotNull(returnedVersions);
        assertEquals(2, returnedVersions.size());
    }

    @Test
    public void testRetrieveVersionsListWhenNoVersions() throws ClassNotMappedException, IOException, FlowRetrievalException, NiFiRegistryException, FlowMapperException {
        final FlowUri flowUri = new FlowUri("http://localhost:18080", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.of(flowUri));

        when(flowSnapshotClient.getSnapshotMetadata(flowUri.getBucketId(), flowUri.getFlowId())).thenReturn(Collections.emptyList());

        final List<VersionedFlowSnapshotMetadata> returnedVersions = flowRetrievalService.getVersions(CLASS_A);
        assertNotNull(returnedVersions);
        assertEquals(0, returnedVersions.size());
    }

    @Test(expected = ClassNotMappedException.class)
    public void testRetrieveVersionsListWhenNoFlowForClass() throws ClassNotMappedException, IOException, FlowRetrievalException, FlowMapperException {
        when(flowMapper.getFlowMapping(CLASS_A)).thenReturn(Optional.empty());
        flowRetrievalService.getVersions(CLASS_A);
    }

    private static VersionedFlowSnapshot createSnapshot(final String bucketId, final String flowId, int version) {
        final VersionedFlowSnapshotMetadata snapshotMetadata = new VersionedFlowSnapshotMetadata();
        snapshotMetadata.setBucketIdentifier(bucketId);
        snapshotMetadata.setFlowIdentifier(flowId);
        snapshotMetadata.setVersion(version);
        snapshotMetadata.setComments("This is snapshot #" + version);

        final VersionedProcessGroup rootProcessGroup = new VersionedProcessGroup();
        rootProcessGroup.setIdentifier("root-pg");
        rootProcessGroup.setName("Root Process Group");

        final VersionedFlowSnapshot snapshot = new VersionedFlowSnapshot();
        snapshot.setSnapshotMetadata(snapshotMetadata);
        snapshot.setFlowContents(rootProcessGroup);
        return snapshot;
    }

}
