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

package org.apache.nifi.minifi.bootstrap.configuration.ingestors;

import static org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeCoordinator.NOTIFIER_INGESTORS_KEY;
import static org.apache.nifi.minifi.bootstrap.configuration.differentiators.WholeConfigDifferentiator.WHOLE_CONFIG_KEY;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.minifi.bootstrap.ConfigurationFileHolder;
import org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeNotifier;
import org.apache.nifi.minifi.bootstrap.configuration.differentiators.WholeConfigDifferentiator;
import org.apache.nifi.minifi.bootstrap.configuration.differentiators.interfaces.Differentiator;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Polls an S3 object for configuration changes.
 *
 */
public class S3ChangeIngestor extends AbstractPullChangeIngestor {

    private static final Map<String, Supplier<Differentiator<ByteBuffer>>> DIFFERENTIATOR_CONSTRUCTOR_MAP;

    static {
        HashMap<String, Supplier<Differentiator<ByteBuffer>>> tempMap = new HashMap<>();
        tempMap.put(WHOLE_CONFIG_KEY, WholeConfigDifferentiator::getByteBufferDifferentiator);

        DIFFERENTIATOR_CONSTRUCTOR_MAP = Collections.unmodifiableMap(tempMap);
    }

    private static final String S3_BASE_KEY = NOTIFIER_INGESTORS_KEY + ".pull.s3";
    public static final String S3_BUCKET = S3_BASE_KEY + ".bucket";
    public static final String S3_OBJECT_KEY = S3_BASE_KEY + ".object.key";
    public static final String S3_ACCESS_KEY = S3_BASE_KEY + ".access.key";
    public static final String S3_SECRET_KEY = S3_BASE_KEY + ".secret.key";
    public static final String S3_ENDPOINT = S3_BASE_KEY + ".endpoint";
    public static final String S3_REGION = S3_BASE_KEY + ".region";
    public static final String S3_POLLING_PERIOD_KEY = S3_BASE_KEY + ".period.ms";
    public static final String DIFFERENTIATOR_KEY = S3_BASE_KEY + ".differentiator";

    private final AtomicReference<AmazonS3> amazonS3Reference = new AtomicReference<>();
    private final AtomicReference<String> s3BucketReference = new AtomicReference<>();
    private final AtomicReference<String> s3ObjectKeyReference = new AtomicReference<>();
    private final AtomicReference<String> s3AccessKeyReference = new AtomicReference<>();
    private final AtomicReference<String> s3SecretKeyReference = new AtomicReference<>();
    private final AtomicReference<String> s3EndpointReference = new AtomicReference<>();
    private final AtomicReference<String> s3RegionReference = new AtomicReference<>();

    private volatile Differentiator<ByteBuffer> differentiator;

    private volatile String lastEtag = "";

    public S3ChangeIngestor() {
        logger = LoggerFactory.getLogger(S3ChangeIngestor.class);
    }

    @Override
    public void initialize(Properties properties, ConfigurationFileHolder configurationFileHolder, ConfigurationChangeNotifier configurationChangeNotifier) {
        super.initialize(properties, configurationFileHolder, configurationChangeNotifier);

        pollingPeriodMS.set(Integer.parseInt(properties.getProperty(S3_POLLING_PERIOD_KEY, DEFAULT_POLLING_PERIOD)));
        if (pollingPeriodMS.get() < 1) {
            throw new IllegalArgumentException("Property, " + S3_POLLING_PERIOD_KEY + ", for the polling period ms must be set with a positive integer.");
        }

        final String s3Bucket = properties.getProperty(S3_BUCKET);
        if (s3Bucket == null || s3Bucket.isEmpty()) {
            throw new IllegalArgumentException("Property, " + S3_BUCKET + ", for the configuration bucket on S3 must be specified.");
        }
        s3BucketReference.set(s3Bucket);

        final String s3ObjectKey = properties.getProperty(S3_OBJECT_KEY);
        if (s3Bucket == null || s3Bucket.isEmpty()) {
            throw new IllegalArgumentException("Property, " + S3_OBJECT_KEY + ", for the configuration object key on S3 must be specified.");
        }
        s3ObjectKeyReference.set(s3ObjectKey);

        final String s3AccesKey = properties.getProperty(S3_ACCESS_KEY, "");
        s3AccessKeyReference.set(s3AccesKey);

        final String s3SecretKey = properties.getProperty(S3_SECRET_KEY, "");
        s3SecretKeyReference.set(s3SecretKey);

        final String s3Region = properties.getProperty(S3_REGION, Regions.US_EAST_1.getName());
        s3RegionReference.set(s3Region);

        final String s3Endpoint = properties.getProperty(S3_ENDPOINT, "s3.amazonaws.com");
        s3EndpointReference.set(s3Endpoint);

        AWSCredentialsProvider credentialsProvider = null;

        if(StringUtils.isEmpty(s3AccesKey)) {
            credentialsProvider = new DefaultAWSCredentialsProviderChain();
        } else {
            credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(s3AccesKey, s3SecretKey));
        }

        EndpointConfiguration endpointConfiguration = new EndpointConfiguration(s3EndpointReference.get(), s3RegionReference.get());

        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withEndpointConfiguration(endpointConfiguration)
                .build();

        amazonS3Reference.set(amazonS3);

        final String differentiatorName = properties.getProperty(DIFFERENTIATOR_KEY);

        if (differentiatorName != null && !differentiatorName.isEmpty()) {

            Supplier<Differentiator<ByteBuffer>> differentiatorSupplier = DIFFERENTIATOR_CONSTRUCTOR_MAP.get(differentiatorName);

            if (differentiatorSupplier == null) {
                throw new IllegalArgumentException("Property, " + DIFFERENTIATOR_KEY + ", has value " + differentiatorName + " which does not " +
                        "correspond to any in the PullHttpChangeIngestor Map:" + DIFFERENTIATOR_CONSTRUCTOR_MAP.keySet());
            }

            differentiator = differentiatorSupplier.get();

        } else {
            differentiator = WholeConfigDifferentiator.getByteBufferDifferentiator();
        }

        differentiator.initialize(properties, configurationFileHolder);

    }

    @Override
    public void run() {

        try {
            logger.debug("Attempting to pull new config");

            AmazonS3 amazonS3Client = amazonS3Reference.get();

            if(amazonS3Client.doesObjectExist(s3BucketReference.get(), s3ObjectKeyReference.get())) {

                GetObjectRequest getObjectRequest = new GetObjectRequest(s3BucketReference.get(), s3ObjectKeyReference.get())
                    .withNonmatchingETagConstraint(lastEtag);

                S3Object s3Object = amazonS3Client.getObject(getObjectRequest);

                if(s3Object != null) {

                    byte[] body = IOUtils.toByteArray(s3Object.getObjectContent());
                    s3Object.close();

                    ByteBuffer bodyByteBuffer = ByteBuffer.wrap(body);

                    if (differentiator.isNew(bodyByteBuffer)) {
                        logger.debug("New change, notifying listener");

                        ByteBuffer readOnlyNewConfig = bodyByteBuffer.asReadOnlyBuffer();

                        configurationChangeNotifier.notifyListeners(readOnlyNewConfig);
                        logger.debug("Listeners notified");
                    } else {
                        logger.debug("Pulled config same as currently running.");
                    }

                    lastEtag = s3Object.getObjectMetadata().getETag();

                }

            } else {
                logger.warn("Config on S3 not found at s3://" + s3BucketReference.get() + s3ObjectKeyReference.get());
            }

        } catch (Exception e) {
            logger.error("Hit an exception while trying to get configuration from S3.", e);
        }
    }

    public void setAmazonS3(AmazonS3 amazonS3) {
        amazonS3Reference.set(amazonS3);
    }

    public AmazonS3 getAmazonS3() {
        return amazonS3Reference.get();
    }

}
