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

package org.apache.nifi.minifi.configuration;


import com.squareup.okhttp.OkHttpClient;
import org.apache.nifi.minifi.configuration.util.TestRestChangeNotifierCommon;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TestRestChangeNotifierSSL extends TestRestChangeNotifierCommon {


    @BeforeClass
    public static void setUpHttps() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException, InterruptedException {
        Files.copy(Paths.get("./src/test/resources/localhost-ts.jks"), Paths.get("./target/tomcat_home/localhost-ts.jks"), REPLACE_EXISTING);
        Files.copy(Paths.get("./src/test/resources/localhost-ks.jks"), Paths.get("./target/tomcat_home/localhost-ks.jks"), REPLACE_EXISTING);

        Properties properties = new Properties();
        properties.setProperty(RestChangeNotifier.TRUSTSTORE_LOCATION_KEY, "./localhost-ts.jks");
        properties.setProperty(RestChangeNotifier.TRUSTSTORE_PASSWORD_KEY, "localtest");
        properties.setProperty(RestChangeNotifier.KEYSTORE_LOCATION_KEY, "./localhost-ks.jks");
        properties.setProperty(RestChangeNotifier.KEYSTORE_PASSWORD_KEY, "localtest");
        properties.setProperty(RestChangeNotifier.KEY_ALIAS_KEY, "localhost");
        properties.setProperty(RestChangeNotifier.TOMCAT_HOME_KEY, "./target/tomcat_home");
        restChangeNotifier = new RestChangeNotifier(properties);
        restChangeNotifier.registerListener(mockChangeListener);
        restChangeNotifier.start();

        client = new OkHttpClient();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(readKeyStore("./src/test/resources/localhost-ks.jks"));

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(readKeyStore("./src/test/resources/localhost-ts.jks"), "localtest".toCharArray());

        sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(), new SecureRandom());
        client.setSslSocketFactory(sslContext.getSocketFactory());

        url = "https://"+ restChangeNotifier.getHost()+":"+ restChangeNotifier.getPort();
        Thread.sleep(1000);
    }

    @AfterClass
    public static void stop() throws InterruptedException {
        restChangeNotifier.stop();
        client = null;
    }

    private static KeyStore readKeyStore(String path) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        char[] password = "localtest".toCharArray();

        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(path);
            ks.load(fis, password);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return ks;
    }
}
