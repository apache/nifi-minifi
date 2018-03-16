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
import org.apache.nifi.minifi.c2.core.service.flow.mapping.FlowMapperFactory;
import org.apache.nifi.minifi.c2.properties.C2Properties;
import org.apache.nifi.registry.bucket.Bucket;
import org.apache.nifi.registry.client.NiFiRegistryClient;
import org.apache.nifi.registry.client.NiFiRegistryException;
import org.apache.nifi.registry.flow.VersionedFlow;
import org.apache.nifi.registry.flow.VersionedFlowSnapshot;
import org.apache.nifi.registry.flow.VersionedFlowSnapshotMetadata;
import org.apache.nifi.registry.flow.VersionedProcessGroup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class StandardFlowRetrievalServiceIT {

    private static final String CLASS_A = "Class A";
    private static final String CLASS_B = "Class B";

    private static StandardFlowRetrievalService flowRetrievalService;

    @BeforeClass
    public static void setup() throws IOException, NiFiRegistryException {
        final C2Properties c2Properties = new C2Properties();
        c2Properties.setProperty(C2Properties.NIFI_REGISTRY_URL, "http://localhost:18080");

        final NiFiRegistryClientFactory niFiRegistryClientFactory = new NiFiRegistryClientFactory(c2Properties);
        final NiFiRegistryClient niFiRegistryClient = niFiRegistryClientFactory.getClient();

        // Create a bucket to use for this test
        final Bucket bucket = new Bucket();
        bucket.setName("C2 Flow Retrieval IT - " + System.currentTimeMillis());

        // Set the id of the created bucket into the C2 properties before we create the FlowMapperFactory
        final Bucket createdBucket = niFiRegistryClient.getBucketClient().create(bucket);
        c2Properties.setProperty(C2Properties.NIFI_REGISTRY_BUCKET_ID, createdBucket.getIdentifier());

        final FlowMapperFactory flowMapperFactory = new FlowMapperFactory(niFiRegistryClientFactory);
        flowRetrievalService = new StandardFlowRetrievalService(flowMapperFactory, niFiRegistryClientFactory);

        // Create a flow for Class A
        final VersionedFlow classAFlow = new VersionedFlow();
        classAFlow.setBucketIdentifier(createdBucket.getIdentifier());
        classAFlow.setName(CLASS_A);

        final VersionedFlow createdClassAFlow = niFiRegistryClient.getFlowClient().create(classAFlow);

        // Create a snapshot #1 for Class A
        final VersionedFlowSnapshot classASnapshot1 = createSnapshot(createdClassAFlow, 1);
        niFiRegistryClient.getFlowSnapshotClient().create(classASnapshot1);

        // Create a snapshot #2 for Class A
        final VersionedFlowSnapshot classASnapshot2 = createSnapshot(createdClassAFlow, 2);
        niFiRegistryClient.getFlowSnapshotClient().create(classASnapshot2);

        // Create a flow for Class B
        final VersionedFlow classBFlow = new VersionedFlow();
        classBFlow.setBucketIdentifier(createdBucket.getIdentifier());
        classBFlow.setName(CLASS_B);
        niFiRegistryClient.getFlowClient().create(classBFlow);

        // Don't create any versions for Class B so we can test what happens when no versions exist
    }

    @Test
    public void testRetrieveLatestWhenVersionsExist() throws ClassNotMappedException, IOException, FlowRetrievalException {
        final VersionedFlowSnapshot versionedFlowSnapshot = flowRetrievalService.getLatestFlow(CLASS_A);
        Assert.assertNotNull(versionedFlowSnapshot);
        Assert.assertEquals(2, versionedFlowSnapshot.getSnapshotMetadata().getVersion());
    }

    @Test(expected = FlowRetrievalException.class)
    public void testRetrieveLatestWhenNoVersions() throws ClassNotMappedException, IOException, FlowRetrievalException {
        flowRetrievalService.getLatestFlow(CLASS_B);
    }

    @Test(expected = ClassNotMappedException.class)
    public void testRetrieveLatestWhenNoFlowForClass() throws ClassNotMappedException, IOException, FlowRetrievalException {
        flowRetrievalService.getLatestFlow("DOES-NOT-EXIST");
    }

    @Test
    public void testRetrieveSpecificWhenVersionsExist() throws ClassNotMappedException, IOException, FlowRetrievalException {
        final VersionedFlowSnapshot versionedFlowSnapshot = flowRetrievalService.getFlow(CLASS_A, 1);
        Assert.assertNotNull(versionedFlowSnapshot);
        Assert.assertEquals(1, versionedFlowSnapshot.getSnapshotMetadata().getVersion());
    }

    @Test(expected = FlowRetrievalException.class)
    public void testRetrieveSpecificWhenNoVersions() throws ClassNotMappedException, IOException, FlowRetrievalException {
        flowRetrievalService.getFlow(CLASS_B, 1);
    }

    @Test(expected = ClassNotMappedException.class)
    public void testRetrieveSpecificWhenNoFlowForClass() throws ClassNotMappedException, IOException, FlowRetrievalException {
        flowRetrievalService.getFlow("DOES-NOT-EXIST", 1);
    }

    @Test
    public void testRetrieveVersionsListWhenVersionsExist() throws ClassNotMappedException, IOException, FlowRetrievalException {
        final List<VersionedFlowSnapshotMetadata> versions = flowRetrievalService.getVersions(CLASS_A);
        Assert.assertNotNull(versions);
        Assert.assertEquals(2, versions.size());
    }

    @Test
    public void testRetrieveVersionsListWhenNoVersions() throws ClassNotMappedException, IOException, FlowRetrievalException {
        final List<VersionedFlowSnapshotMetadata> versions = flowRetrievalService.getVersions(CLASS_B);
        Assert.assertNotNull(versions);
        Assert.assertEquals(0, versions.size());
    }

    @Test(expected = ClassNotMappedException.class)
    public void testRetrieveVersionsListWhenNoFlowForClass() throws ClassNotMappedException, IOException, FlowRetrievalException {
        flowRetrievalService.getVersions("DOES-NOT-EXIST");
    }

    private static VersionedFlowSnapshot createSnapshot(final VersionedFlow versionedFlow, int num) {
        final VersionedFlowSnapshotMetadata snapshotMetadata = new VersionedFlowSnapshotMetadata();
        snapshotMetadata.setBucketIdentifier(versionedFlow.getBucketIdentifier());
        snapshotMetadata.setFlowIdentifier(versionedFlow.getIdentifier());
        snapshotMetadata.setVersion(num);
        snapshotMetadata.setComments("This is snapshot #" + num);

        final VersionedProcessGroup rootProcessGroup = new VersionedProcessGroup();
        rootProcessGroup.setIdentifier("root-pg");
        rootProcessGroup.setName("Root Process Group");

        final VersionedFlowSnapshot snapshot = new VersionedFlowSnapshot();
        snapshot.setSnapshotMetadata(snapshotMetadata);
        snapshot.setFlowContents(rootProcessGroup);
        return snapshot;
    }


}
