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

package org.apache.nifi.minifi.c2.properties;

// TODO, this is in commons for now because minifi-c2-jetty needs it as well. Consider moving it to its own module.

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class C2Properties extends Properties {

    // Web Properties
    public static final String WEB_WAR_DIR = "minifi.c2.server.web.war.directory";
    public static final String WEB_HTTP_HOST = "minifi.c2.server.web.host";
    public static final String WEB_HTTP_PORT = "minifi.c2.server.web.port";
    public static final String WEB_WORKING_DIR = "minifi.c2.server.web.jetty.working.directory";
    public static final String WEB_THREADS = "minifi.c2.server.web.jetty.threads";

    // TLS Properties
    public static final String SECURITY_TLS_ENABLED = "minifi.c2.server.security.tls.enabled";
    public static final String SECURITY_TLS_KEYSTORE = "minifi.c2.server.security.tls.keystore";
    public static final String SECURITY_TLS_KEYSTORE_TYPE = "minifi.c2.server.security.tls.keystoreType";
    public static final String SECURITY_TLS_KEYSTORE_PASSWD = "minifi.c2.server.security.tls.keystorePasswd";
    public static final String SECURITY_TLS_KEY_PASSWD = "minifi.c2.server.security.tls.keyPasswd";
    public static final String SECURITY_TLS_TRUSTSTORE = "minifi.c2.server.security.tls.truststore";
    public static final String SECURITY_TLS_TRUSTSTORE_TYPE = "minifi.c2.server.security.tls.truststoreType";
    public static final String SECURITY_TLS_TRUSTSTORE_PASSWD = "minifi.c2.server.security.tls.truststorePasswd";

    // Authorizer Properties
    public static final String SECURITY_IDENTITY_MAPPING_PATTERN_PREFIX = "minifi.c2.server.security.identity.mapping.pattern.";
    public static final String SECURITY_IDENTITY_MAPPING_VALUE_PREFIX = "minifi.c2.server.security.identity.mapping.value.";

    // NiFi Registry properties
    public static final String NIFI_REGISTRY_URL = "minifi.c2.server.nifi.registry.url";
    public static final String NIFI_REGISTRY_BUCKET_ID = "minifi.c2.server.nifi.registry.bucket.id";

    // Default Values
    public static final String DEFAULT_WEB_WORKING_DIR = "./work/jetty";
    public static final String DEFAULT_WAR_DIR = "./lib";
    public static final String DEFAULT_AUTHENTICATION_EXPIRATION = "12 hours";

    private static final Logger logger = LoggerFactory.getLogger(C2Properties.class);

    private static final C2Properties properties = initProperties();
    private static final String C2_SERVER_HOME = System.getenv("C2_SERVER_HOME");

    private static C2Properties initProperties() {
        C2Properties properties = new C2Properties();
        try (InputStream inputStream = C2Properties.class.getClassLoader().getResourceAsStream("minifi-c2.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load minifi-c2.properties", e);
        }
        return properties;
    }

    public static C2Properties getInstance() {
        return properties;
    }

    public int getWebThreads() {
        int webThreads = 100;
        try {
            webThreads = Integer.parseInt(getProperty(WEB_THREADS));
        } catch (final NumberFormatException nfe) {
            logger.warn(String.format("%s must be an integer value. Defaulting to %s", WEB_THREADS, webThreads));
        }
        return webThreads;
    }

    public File getWarLibDirectory() {
        return new File(getProperty(WEB_WAR_DIR, DEFAULT_WAR_DIR));
    }

    public File getWebWorkingDirectory() {
        return new File(getProperty(WEB_WORKING_DIR, DEFAULT_WEB_WORKING_DIR));
    }

    public String getHost() {
        return getProperty(WEB_HTTP_HOST);
    }

    public Integer getPort() {
        return getPropertyAsInteger(WEB_HTTP_PORT);
    }

    public boolean isTlsEnabled() {
        return Boolean.valueOf(getProperty(SECURITY_TLS_ENABLED, "false"));
    }

//    public boolean getNeedClientAuth() {
//        boolean needClientAuth = true;
//        String rawNeedClientAuth = getProperty(SECURITY_NEED_CLIENT_AUTH);
//        if ("false".equalsIgnoreCase(rawNeedClientAuth)) {
//            needClientAuth = false;
//        }
//        return needClientAuth;
//    }

    public String getKeyStorePath() {
        return getProperty(SECURITY_TLS_KEYSTORE);
    }

    public String getKeyStoreType() {
        return getProperty(SECURITY_TLS_KEYSTORE_TYPE);
    }

    public String getKeyStorePassword() {
        return getProperty(SECURITY_TLS_KEYSTORE_PASSWD);
    }

    public String getKeyPassword() {
        return getProperty(SECURITY_TLS_KEY_PASSWD);
    }

    public String getTrustStorePath() {
        return getProperty(SECURITY_TLS_TRUSTSTORE);
    }

    public String getTrustStoreType() {
        return getProperty(SECURITY_TLS_TRUSTSTORE_TYPE);
    }

    public String getTrustStorePassword() {
        return getProperty(SECURITY_TLS_TRUSTSTORE_PASSWD);
    }

    public String getNifiRegistryUrl() {
        return getProperty(NIFI_REGISTRY_URL);
    }

    public String getNifiRegistryBucketId() {
        return getProperty(NIFI_REGISTRY_BUCKET_ID);
    }

    // Helper functions for common ways of interpreting property values

    private String getPropertyAsTrimmedString(String key) {
        final String value = getProperty(key);
        if (!StringUtils.isBlank(value)) {
            return value.trim();
        } else {
            return null;
        }
    }

    private Integer getPropertyAsInteger(String key) {
        final String value = getProperty(key);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            throw new IllegalStateException(String.format("%s must be an integer value.", key));
        }
    }

    private File getPropertyAsFile(String key) {
        final String filePath = getProperty(key);
        if (filePath != null && filePath.trim().length() > 0) {
            return new File(filePath.trim());
        } else {
            return null;
        }
    }

    private File getPropertyAsFile(String propertyKey, String defaultFileLocation) {
        final String value = getProperty(propertyKey);
        if (StringUtils.isBlank(value)) {
            return new File(defaultFileLocation);
        } else {
            return new File(value);
        }
    }
}
