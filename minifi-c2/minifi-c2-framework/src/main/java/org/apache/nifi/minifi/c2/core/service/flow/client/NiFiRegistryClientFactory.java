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
package org.apache.nifi.minifi.c2.core.service.flow.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.minifi.c2.properties.C2Properties;
import org.apache.nifi.registry.client.NiFiRegistryClient;
import org.apache.nifi.registry.client.NiFiRegistryClientConfig;
import org.apache.nifi.registry.client.impl.JerseyNiFiRegistryClient;
import org.apache.nifi.registry.security.util.KeystoreType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class does not follow the typical "factory bean" pattern of having a method annotated with @Bean because
 * we want to support running a C2 server that may not be configured to do flow deployments, which means we don't want
 * to create a NiFiRegistryClient during start-up because the registry URL may not be populated.
 *
 * An instance of this class can be injected into other services that need a NiFiRegistryClient, and if those services
 * get called then they can use this class to lazily obtain an instance, which can then throw an exception if the
 * appropriate configuration is not provided.
 */
@Component
public class NiFiRegistryClientFactory {

    private volatile NiFiRegistryClient client;

    private final C2Properties c2Properties;

    @Autowired
    public NiFiRegistryClientFactory(final C2Properties c2Properties) {
        this.c2Properties = c2Properties;
        Validate.notNull(this.c2Properties);
    }

    public String getNiFiRegistryUrl() {
        return c2Properties.getNifiRegistryUrl();
    }

    public String getNiFiRegistryBucketId() {
        return c2Properties.getNifiRegistryBucketId();
    }

    /**
     * Lazily creates a NiFiRegistryClient the first time this method is called.
     *
     * @return the NiFiRegistryClient held by this factory
     */
    public NiFiRegistryClient getClient() {
        if (client == null) {
            initializeClient();
        }
        return client;
    }

    private synchronized void initializeClient() {
        // make sure another thread hasn't initialized the client before we got into this synchronized method
        if (client != null) {
            return;
        }

        final String url = c2Properties.getNifiRegistryUrl();
        if (StringUtils.isBlank(url)) {
            throw new IllegalStateException("Unable to create NiFi Registry Client because NiFi Registry URL was not provided");
        }

        final String keystore = c2Properties.getKeyStorePath();
        final String keystoreType = c2Properties.getKeyStoreType();
        final String keystorePasswd = c2Properties.getKeyStorePassword();
        final String keyPasswd = c2Properties.getKeyPassword();

        final String truststore = c2Properties.getTrustStorePath();
        final String truststoreType = c2Properties.getTrustStoreType();
        final String truststorePasswd = c2Properties.getTrustStorePassword();

        final boolean secureUrl = url.startsWith("https");
        if (secureUrl && (StringUtils.isBlank(keystore) || StringUtils.isBlank(truststore))) {
            throw new IllegalStateException("Keystore and truststore must be provided when NiFi Registry URL is secure");
        }

        final NiFiRegistryClientConfig.Builder clientConfigBuilder = new NiFiRegistryClientConfig.Builder().baseUrl(url);

        if (secureUrl) {
            if (!StringUtils.isBlank(keystore)) {
                clientConfigBuilder.keystoreFilename(keystore);
            }
            if (!StringUtils.isBlank(keystoreType)) {
                clientConfigBuilder.keystoreType(KeystoreType.valueOf(keystoreType.toUpperCase()));
            }
            if (!StringUtils.isBlank(keystorePasswd)) {
                clientConfigBuilder.keystorePassword(keystorePasswd);
            }
            if (!StringUtils.isBlank(keyPasswd)) {
                clientConfigBuilder.keyPassword(keyPasswd);
            }
            if (!StringUtils.isBlank(truststore)) {
                clientConfigBuilder.truststoreFilename(truststore);
            }
            if (!StringUtils.isBlank(truststoreType)) {
                clientConfigBuilder.truststoreType(KeystoreType.valueOf(truststoreType.toUpperCase()));
            }
            if (!StringUtils.isBlank(truststorePasswd)) {
                clientConfigBuilder.truststorePassword(truststorePasswd);
            }
        }

        final NiFiRegistryClientConfig clientConfig = clientConfigBuilder.build();
        this.client = new JerseyNiFiRegistryClient.Builder().config(clientConfig).build();
    }

}
