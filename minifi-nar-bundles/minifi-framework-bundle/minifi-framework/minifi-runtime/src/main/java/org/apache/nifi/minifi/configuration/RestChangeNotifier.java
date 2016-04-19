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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Pippo;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.WebServerSettings;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RoutePreDispatchListener;
import ro.pippo.tomcat.TomcatSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class RestChangeNotifier implements ConfigurationChangeNotifier {

    private final Set<ConfigurationChangeListener> configurationChangeListeners = new HashSet<>();
    private final static Logger log = LoggerFactory.getLogger(RestChangeNotifier.class);
    private final Pippo pippo;
    private final Thread serverThread;
    public static final String GET_TEXT = "This is a config change listener for an Apache NiFi - MiNiFi instance.\n" +
            "Use this rest server to upload a conf.yml to configure the MiNiFi instance.\n" +
            "Send a POST http request to '/upload' to upload the file.";
    private static String configFile = null;


    public static final String PORT_KEY = "nifi.minifi.notifier.http.port";
    public static final String HOST_KEY = "nifi.minifi.notifier.http.host";
    public static final String TRUSTSTORE_LOCATION_KEY = "nifi.minifi.notifier.http.truststore.location";
    public static final String TRUSTSTORE_PASSWORD_KEY = "nifi.minifi.notifier.http.truststore.password";
    public static final String KEYSTORE_LOCATION_KEY = "nifi.minifi.notifier.http.keystore.location";
    public static final String KEYSTORE_PASSWORD_KEY = "nifi.minifi.notifier.http.keystore.password";
    public static final String KEY_ALIAS_KEY = "nifi.minifi.notifier.http.keystore.alias";
    public static final String TOMCAT_HOME_KEY = "nifi.minifi.notifier.http.tomcat.home";

    public RestChangeNotifier(Properties properties){
        pippo = new Pippo();
        this.initialize(properties);
        serverThread = new Thread(new RunServer());
    }

    @Override
    public void initialize(Properties properties) {
        WebServerSettings webServerSettings = pippo.getServer().getSettings();

        String host = properties.getProperty(HOST_KEY);
        webServerSettings.host(host != null ? host : "localhost");

        String portRaw = properties.getProperty(PORT_KEY);
        webServerSettings.port(Integer.parseInt(portRaw != null ? portRaw : "8338"));

        webServerSettings.truststoreFile(properties.getProperty(TRUSTSTORE_LOCATION_KEY));
        webServerSettings.truststorePassword(properties.getProperty(TRUSTSTORE_PASSWORD_KEY));
        webServerSettings.keystoreFile(properties.getProperty(KEYSTORE_LOCATION_KEY));
        webServerSettings.keystorePassword(properties.getProperty(KEYSTORE_PASSWORD_KEY));
        ((TomcatSettings) webServerSettings).keyAlias(properties.getProperty(KEY_ALIAS_KEY));

        String tomcatHome = properties.getProperty(TOMCAT_HOME_KEY);
        if(tomcatHome != null) {
            ((TomcatSettings) webServerSettings).baseFolder(tomcatHome);
        }

        pippo.getApplication().getPippoSettings().overrideSetting("tomcat.baseFolder","./target/tomcat_home/");


        pippo.getApplication().GET("/", new RouteHandler() {
            @Override
            public void handle(RouteContext routeContext) {
                routeContext.send(GET_TEXT);
            }
        });

        pippo.getApplication().POST("/upload",  new RouteHandler() {
            @Override
            public void handle(RouteContext routeContext) {

                configFile = routeContext.getRequest().getBody();
                notifyListeners();
                routeContext.send("Configuration received, notifying listeners");
            }
        });

        pippo.getApplication().getRoutePreDispatchListeners().add(new RoutePreDispatchListener() {
            @Override
            public void onPreDispatch(Request request, Response response) {
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                log.info("request method = " + request.getMethod());
                log.info("request IP = " + request.getClientIp());
                log.info("request url = " + request.getUrl());
                log.info("context path = " + request.getContextPath());
                log.info("request content type = " + request.getContentType());
                log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }
        });
    }


    @Override
    public Set<ConfigurationChangeListener> getChangeListeners() {
        return configurationChangeListeners;
    }

    @Override
    public boolean registerListener(ConfigurationChangeListener listener) {
        configurationChangeListeners.add(listener);
        return true;
    }

    @Override
    public void notifyListeners() {
        if (configFile == null){
            throw new IllegalStateException("Attempting to notify listeners when there is no new config file.");
        }

        for (final ConfigurationChangeListener listener : getChangeListeners()) {
            try (final ByteArrayInputStream fis = new ByteArrayInputStream(configFile.getBytes());) {
                listener.handleChange(fis);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read the changed file " + configFile, ex);
            }
        }

        configFile = null;
    }

    public void stop() throws InterruptedException {
        pippo.stop();
        serverThread.interrupt();

        try {
            serverThread.join(5000L);
        } catch (InterruptedException var4) {
            serverThread.stop();
        }
    }

    public String getHost(){
        return pippo.getServer().getSettings().getHost();
    }
    public int getPort(){
        return pippo.getServer().getSettings().getPort();
    }

    public String getConfigString(){
        return configFile;
    }

    @Override
    public void start(){
        serverThread.start();
    }

    private class RunServer implements Runnable {

        public void run() {
            pippo.start();
        }
    }
}
