/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.minifi.bootstrap.configuration.ingestors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.methods.HttpGet;
import org.apache.nifi.minifi.bootstrap.ConfigurationFileHolder;
import org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeNotifier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3ChangeIngestorTest {

    private Properties properties;
    private byte[] originalConfig;
    private byte[] newConfig;

    @Before
    public void setUp() throws IOException {

        originalConfig = Files.readAllBytes(Paths.get("src/test/resources/config-minimal.yml"));
        newConfig = Files.readAllBytes(Paths.get("src/test/resources/default.yml"));

        properties = new Properties();
        properties.put(S3ChangeIngestor.S3_BUCKET, "test");
        properties.put(S3ChangeIngestor.S3_OBJECT_KEY, "config.yml");
        properties.put(S3ChangeIngestor.S3_POLLING_PERIOD_KEY, S3ChangeIngestor.DEFAULT_POLLING_PERIOD);
        properties.put(S3ChangeIngestor.S3_ENDPOINT, "s3.amazonaws.com");
        properties.put(S3ChangeIngestor.S3_REGION, "us-east-1");
        properties.put(S3ChangeIngestor.S3_ACCESS_KEY, "anything");
        properties.put(S3ChangeIngestor.S3_SECRET_KEY, "anything");

    }

    @Test
    public void testInitialize() {

        S3ChangeIngestor ingestor = new S3ChangeIngestor();
        ConfigurationFileHolder configurationFileHolder = Mockito.mock(ConfigurationFileHolder.class);
        ConfigurationChangeNotifier configurationChangeNotifier = Mockito.mock(ConfigurationChangeNotifier.class);
        ingestor.initialize(properties, configurationFileHolder, configurationChangeNotifier);
        assertEquals(ingestor.getAmazonS3().getRegionName(), properties.getProperty(S3ChangeIngestor.S3_REGION));

    }

    @Test
    public void testUpdatedConfiguration() throws IOException {

        final S3ObjectInputStream stream = new S3ObjectInputStream(new ByteArrayInputStream(newConfig), new HttpGet());

        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getETag()).thenReturn("randometag");

        S3Object s3Object = Mockito.mock(S3Object.class);
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);

        AmazonS3 client = Mockito.mock(AmazonS3.class);
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        when(client.doesObjectExist(any(String.class), any(String.class))).thenReturn(true);

        AtomicReference<ByteBuffer> configuration = new AtomicReference<>();
        configuration.set(ByteBuffer.wrap(originalConfig));

        ConfigurationFileHolder configurationFileHolder = Mockito.mock(ConfigurationFileHolder.class);
        when(configurationFileHolder.getConfigFileReference()).thenReturn(configuration);

        ConfigurationChangeNotifier configurationChangeNotifier = Mockito.mock(ConfigurationChangeNotifier.class);

        S3ChangeIngestor ingestor = new S3ChangeIngestor();

        ingestor.initialize(properties, configurationFileHolder, configurationChangeNotifier);
        ingestor.setAmazonS3(client);

        ingestor.run();

        verify(configurationChangeNotifier).notifyListeners(ByteBuffer.wrap(newConfig));

    }

    @Test
    public void testSameConfiguration() throws IOException {

        final S3ObjectInputStream stream = new S3ObjectInputStream(new ByteArrayInputStream(originalConfig), new HttpGet());

        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getETag()).thenReturn("randometag");

        S3Object s3Object = Mockito.mock(S3Object.class);
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);

        AmazonS3 client = Mockito.mock(AmazonS3.class);
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);

        AtomicReference<ByteBuffer> configuration = new AtomicReference<>();
        configuration.set(ByteBuffer.wrap(originalConfig));

        ConfigurationFileHolder configurationFileHolder = Mockito.mock(ConfigurationFileHolder.class);
        when(configurationFileHolder.getConfigFileReference()).thenReturn(configuration);

        ConfigurationChangeNotifier configurationChangeNotifier = Mockito.mock(ConfigurationChangeNotifier.class);

        S3ChangeIngestor ingestor = new S3ChangeIngestor();

        ingestor.initialize(properties, configurationFileHolder, configurationChangeNotifier);
        ingestor.setAmazonS3(client);

        ingestor.run();

        verify(configurationChangeNotifier, never()).notifyListeners(ByteBuffer.wrap(newConfig));

    }

    @Test
    public void testNullConfiguration() throws IOException {

        AmazonS3 client = Mockito.mock(AmazonS3.class);
        when(client.doesObjectExist(any(String.class), any(String.class))).thenReturn(false);

        AtomicReference<ByteBuffer> configuration = new AtomicReference<>();
        configuration.set(ByteBuffer.wrap(originalConfig));

        ConfigurationFileHolder configurationFileHolder = Mockito.mock(ConfigurationFileHolder.class);
        when(configurationFileHolder.getConfigFileReference()).thenReturn(configuration);

        ConfigurationChangeNotifier configurationChangeNotifier = Mockito.mock(ConfigurationChangeNotifier.class);

        S3ChangeIngestor ingestor = new S3ChangeIngestor();

        ingestor.initialize(properties, configurationFileHolder, configurationChangeNotifier);
        ingestor.setAmazonS3(client);

        ingestor.run();

        verify(configurationChangeNotifier, never()).notifyListeners(ByteBuffer.wrap(newConfig));

    }

    @Test
    public void testNonExistentConfiguration() throws IOException {

        AmazonS3 client = Mockito.mock(AmazonS3.class);
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(null);

        AtomicReference<ByteBuffer> configuration = new AtomicReference<>();
        configuration.set(ByteBuffer.wrap(originalConfig));

        ConfigurationFileHolder configurationFileHolder = Mockito.mock(ConfigurationFileHolder.class);
        when(configurationFileHolder.getConfigFileReference()).thenReturn(configuration);

        ConfigurationChangeNotifier configurationChangeNotifier = Mockito.mock(ConfigurationChangeNotifier.class);

        S3ChangeIngestor ingestor = new S3ChangeIngestor();

        ingestor.initialize(properties, configurationFileHolder, configurationChangeNotifier);
        ingestor.setAmazonS3(client);

        ingestor.run();

        verify(configurationChangeNotifier, never()).notifyListeners(ByteBuffer.wrap(newConfig));

    }

}