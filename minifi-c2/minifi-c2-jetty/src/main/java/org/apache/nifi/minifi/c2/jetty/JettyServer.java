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

package org.apache.nifi.minifi.c2.jetty;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.minifi.c2.properties.C2Properties;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JettyServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);
    private static final String WEB_DEFAULTS_XML = "webdefault.xml";
    private static final int HEADER_BUFFER_SIZE = 16 * 1024; // 16kb

    private static final FileFilter WAR_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            final String nameToTest = pathname.getName().toLowerCase();
            return nameToTest.endsWith(".war") && pathname.isFile();
        }
    };

    private static final String C2_SERVER_HOME = System.getenv("C2_SERVER_HOME");
    private static final String MINIFI_C2_PROPERTIES_FILE_LOCATION = "conf/minifi-c2.properties";
    private static final String C2_API_CONTEXT_PATH = "/minifi-c2-api";

    private final C2Properties properties;
    private final Server server;
    private WebAppContext webApiContext;

    public static void main(String[] args) throws Exception {
        File propertiesFile = new File(MINIFI_C2_PROPERTIES_FILE_LOCATION);
        C2Properties properties = loadProperties(propertiesFile);

        new JettyServer(properties);

//        final HandlerCollection handlers = new HandlerCollection();
//        for (Path path : Files.list(Paths.get(C2_SERVER_HOME, "webapps")).collect(Collectors.toList())) {
//            handlers.addHandler(loadWar(path.toFile(), "/c2", JettyServer.class.getClassLoader()));
//        }
//
//        Server server;
//        int port = Integer.parseInt(properties.getProperty("minifi.c2.server.port", "10080"));
//        if (properties.isSecure()) {
//            SslContextFactory sslContextFactory = getSslContextFactory(properties);
//            HttpConfiguration config = new HttpConfiguration();
//            config.setSecureScheme("https");
//            config.setSecurePort(port);
//            config.addCustomizer(new SecureRequestCustomizer());
//
//            server = new Server();
//
//            ServerConnector serverConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(config));
//            serverConnector.setPort(port);
//
//            server.addConnector(serverConnector);
//        } else {
//            server = new Server(port);
//        }
//
//        server.setHandler(handlers);
//        server.start();
//
//        // ensure everything started successfully
//        for (Handler handler : server.getChildHandlers()) {
//            // see if the handler is a web app
//            if (handler instanceof WebAppContext) {
//                WebAppContext context = (WebAppContext) handler;
//
//                // see if this webapp had any exceptions that would
//                // cause it to be unavailable
//                if (context.getUnavailableException() != null) {
//
//                    System.err.println("Failed to start web server: " + context.getUnavailableException().getMessage());
//                    System.err.println("Shutting down...");
//                    logger.warn("Failed to start web server... shutting down.", context.getUnavailableException());
//                    server.stop();
//                    System.exit(1);
//                }
//            }
//        }
//
//        //server.dumpStdErr();
//        server.join();
    }

    public JettyServer(final C2Properties properties) {
        this.properties = properties;

        final QueuedThreadPool threadPool = new QueuedThreadPool(properties.getWebThreads());
        threadPool.setName("MiNiFi C2 Server");

        this.server = new Server(threadPool);

        // enable the annotation based configuration to ensure the jsp container is initialized properly
        final Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());

        try {
            configureConnectors();
            loadWars();
            start();
        } catch (final Throwable t) {
            startUpFailure(t);
        }
    }

    public void start() {
        try {
            // start the server
            server.start();

            // ensure everything started successfully
            for (Handler handler : server.getChildHandlers()) {
                // see if the handler is a web app
                if (handler instanceof WebAppContext) {
                    WebAppContext context = (WebAppContext) handler;

                    // see if this webapp had any exceptions that would
                    // cause it to be unavailable
                    if (context.getUnavailableException() != null) {
                        startUpFailure(context.getUnavailableException());
                    }
                }
            }

            dumpUrls();
        } catch (final Throwable t) {
            startUpFailure(t);
        }


    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception ex) {
            logger.warn("Failed to stop web server", ex);
        }
    }

    private void configureConnectors() {
        // create the http configuration
        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setRequestHeaderSize(HEADER_BUFFER_SIZE);
        httpConfiguration.setResponseHeaderSize(HEADER_BUFFER_SIZE);

        if (properties.getPort() != null && !properties.isTlsEnabled()) {
            final Integer port = properties.getPort();
            if (port < 0 || (int) Math.pow(2, 16) <= port) {
                throw new IllegalStateException("Invalid HTTP port: " + port);
            }

            logger.info("Configuring Jetty for HTTP on port: " + port);

            // create the connector
            final ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));

            // set host and port
            if (StringUtils.isNotBlank(properties.getHost())) {
                http.setHost(properties.getHost());
            }
            http.setPort(port);

            // add this connector
            server.addConnector(http);
        } else if (properties.getPort() != null && properties.isTlsEnabled()) {

            // TODO simplify this if/else block to combine duplication

            final Integer port = properties.getPort();
            if (port < 0 || (int) Math.pow(2, 16) <= port) {
                throw new IllegalStateException("Invalid HTTPs port: " + port);
            }

            if (StringUtils.isBlank(properties.getKeyStorePath())) {
                throw new IllegalStateException(C2Properties.SECURITY_TLS_KEYSTORE
                        + " must be provided to configure Jetty for HTTPs");
            }

            logger.info("Configuring Jetty for HTTPs on port: " + port);

            // add some secure config
            final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
            httpsConfiguration.setSecureScheme("https");
            httpsConfiguration.setSecurePort(properties.getPort());
            httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

            // build the connector
            final ServerConnector https = new ServerConnector(server,
                    new SslConnectionFactory(createSslContextFactory(), "http/1.1"),
                    new HttpConnectionFactory(httpsConfiguration));

            // set host and port
            if (StringUtils.isNotBlank(properties.getHost())) {
                https.setHost(properties.getHost());
            }
            https.setPort(port);

            // add this connector
            server.addConnector(https);
        }
    }

//    private static SslContextFactory getSslContextFactory(C2Properties properties) throws GeneralSecurityException, IOException {
//        SslContextFactory sslContextFactory = new SslContextFactory();
//        KeyStore keyStore = KeyStore.getInstance(properties.getProperty(C2Properties.SECURITY_TLS_KEYSTORE_TYPE));
//        Path keyStorePath = Paths.get(C2_SERVER_HOME).resolve(properties.getProperty(C2Properties.SECURITY_TLS_KEYSTORE)).toAbsolutePath();
//        logger.debug("keystore path: " + keyStorePath);
//        try (InputStream inputStream = Files.newInputStream(keyStorePath)) {
//            keyStore.load(inputStream, properties.getProperty(C2Properties.SECURITY_TLS_KEYSTORE_PASSWD).toCharArray());
//        }
//        sslContextFactory.setKeyStore(keyStore);
//        sslContextFactory.setKeyManagerPassword(properties.getProperty(C2Properties.SECURITY_TLS_KEY_PASSWD));
//        sslContextFactory.setWantClientAuth(true);
//
//        String trustStorePath = Paths.get(C2_SERVER_HOME).resolve(properties.getProperty(C2Properties.TLS)).toAbsolutePath().toFile().getAbsolutePath();
//        logger.debug("truststore path: " + trustStorePath);
//        sslContextFactory.setTrustStorePath(trustStorePath);
//        sslContextFactory.setTrustStoreType(properties.getProperty(C2Properties.MINIFI_C2_SERVER_TRUSTSTORE_TYPE));
//        sslContextFactory.setTrustStorePassword(properties.getProperty(C2Properties.MINIFI_C2_SERVER_TRUSTSTORE_PASSWD));
//        try {
//            sslContextFactory.start();
//        } catch (Exception e) {
//            throw new IOException(e);
//        }
//        return sslContextFactory;
//    }

    private SslContextFactory createSslContextFactory() {
        final SslContextFactory contextFactory = new SslContextFactory();

        logger.info("Setting Jetty's SSLContextFactory needClientAuth to true");
        contextFactory.setNeedClientAuth(true);

        /* below code sets JSSE system properties when values are provided */
        // keystore properties
        if (StringUtils.isNotBlank(properties.getKeyStorePath())) {
            contextFactory.setKeyStorePath(properties.getKeyStorePath());
        }
        if (StringUtils.isNotBlank(properties.getKeyStoreType())) {
            contextFactory.setKeyStoreType(properties.getKeyStoreType());
        }
        final String keystorePassword = properties.getKeyStorePassword();
        final String keyPassword = properties.getKeyPassword();
        if (StringUtils.isNotBlank(keystorePassword)) {
            // if no key password was provided, then assume the keystore password is the same as the key password.
            final String defaultKeyPassword = (StringUtils.isBlank(keyPassword)) ? keystorePassword : keyPassword;
            contextFactory.setKeyManagerPassword(keystorePassword);
            contextFactory.setKeyStorePassword(defaultKeyPassword);
        } else if (StringUtils.isNotBlank(keyPassword)) {
            // since no keystore password was provided, there will be no keystore integrity check
            contextFactory.setKeyStorePassword(keyPassword);
        }

        // truststore properties
        if (StringUtils.isNotBlank(properties.getTrustStorePath())) {
            contextFactory.setTrustStorePath(properties.getTrustStorePath());
        }
        if (StringUtils.isNotBlank(properties.getTrustStoreType())) {
            contextFactory.setTrustStoreType(properties.getTrustStoreType());
        }
        if (StringUtils.isNotBlank(properties.getTrustStorePassword())) {
            contextFactory.setTrustStorePassword(properties.getTrustStorePassword());
        }

        return contextFactory;
    }

    private void loadWars() throws IOException {
        final File warDirectory = properties.getWarLibDirectory();
        final File[] wars = warDirectory.listFiles(WAR_FILTER);

        if (wars == null) {
            throw new RuntimeException("Unable to access war lib directory: " + warDirectory);
        }

        File webApiWar = null;
        for (final File war : wars) {
            if (war.getName().startsWith("minifi-c2-web-api")) {
                webApiWar = war;
            }
        }

        if (webApiWar == null) {
            throw new IllegalStateException("Unable to locate MiNiFi C2 Web API war.");
        }

        webApiContext = loadWar(webApiWar, C2_API_CONTEXT_PATH, C2Properties.class.getClassLoader());
//        logger.info("Adding {} object to ServletContext with key 'minifi-c2.properties'", properties.getClass().getName());
//        webApiContext.setAttribute("minifi-c2.properties", properties);

        // there is an issue scanning the asm repackaged jar so narrow down what we are scanning
        webApiContext.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", ".*/spring-[^/]*\\.jar$");

        final HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(webApiContext);
        server.setHandler(handlers);
    }

//    private static WebAppContext loadWar(final File warFile, final String contextPath, final ClassLoader parentClassLoader) throws IOException {
//        final WebAppContext webappContext = new WebAppContext(warFile.getPath(), contextPath);
//        webappContext.setContextPath(contextPath);
//        webappContext.setDisplayName(contextPath);
//
//        // instruction jetty to examine these jars for tlds, web-fragments, etc
//        webappContext.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\\\.jar$|.*/[^/]*taglibs.*\\.jar$" );
//
//        // remove slf4j server class to allow WAR files to have slf4j dependencies in WEB-INF/lib
//        List<String> serverClasses = new ArrayList<>(Arrays.asList(webappContext.getServerClasses()));
//        serverClasses.remove("org.slf4j.");
//        webappContext.setServerClasses(serverClasses.toArray(new String[0]));
//        webappContext.setDefaultsDescriptor(WEB_DEFAULTS_XML);
//
//        // get the temp directory for this webapp
//        File tempDir = Paths.get(C2_SERVER_HOME, "tmp", warFile.getName()).toFile();
//        if (tempDir.exists() && !tempDir.isDirectory()) {
//            throw new RuntimeException(tempDir.getAbsolutePath() + " is not a directory");
//        } else if (!tempDir.exists()) {
//            final boolean made = tempDir.mkdirs();
//            if (!made) {
//                throw new RuntimeException(tempDir.getAbsolutePath() + " could not be created");
//            }
//        }
//        if (!(tempDir.canRead() && tempDir.canWrite())) {
//            throw new RuntimeException(tempDir.getAbsolutePath() + " directory does not have read/write privilege");
//        }
//
//        // configure the temp dir
//        webappContext.setTempDirectory(tempDir);
//
//        // configure the max form size (3x the default)
//        webappContext.setMaxFormContentSize(600000);
//
//        webappContext.setClassLoader(new WebAppClassLoader(parentClassLoader, webappContext));
//
//        logger.info("Loading WAR: " + warFile.getAbsolutePath() + " with context path set to " + contextPath);
//        return webappContext;
//    }

    private WebAppContext loadWar(final File warFile, final String contextPath, final ClassLoader parentClassLoader) throws IOException {
        final WebAppContext webappContext = new WebAppContext(warFile.getPath(), contextPath);
        webappContext.setContextPath(contextPath);
        webappContext.setDisplayName(contextPath);

        // remove slf4j server class to allow WAR files to have slf4j dependencies in WEB-INF/lib
        List<String> serverClasses = new ArrayList<>(Arrays.asList(webappContext.getServerClasses()));
        serverClasses.remove("org.slf4j.");
        webappContext.setServerClasses(serverClasses.toArray(new String[0]));
        webappContext.setDefaultsDescriptor(WEB_DEFAULTS_XML);

        // get the temp directory for this webapp
        final File webWorkingDirectory = properties.getWebWorkingDirectory();
        final File tempDir = new File(webWorkingDirectory, warFile.getName());
        if (tempDir.exists() && !tempDir.isDirectory()) {
            throw new RuntimeException(tempDir.getAbsolutePath() + " is not a directory");
        } else if (!tempDir.exists()) {
            final boolean made = tempDir.mkdirs();
            if (!made) {
                throw new RuntimeException(tempDir.getAbsolutePath() + " could not be created");
            }
        }
        if (!(tempDir.canRead() && tempDir.canWrite())) {
            throw new RuntimeException(tempDir.getAbsolutePath() + " directory does not have read/write privilege");
        }

        // configure the temp dir
        webappContext.setTempDirectory(tempDir);

        // configure the max form size (3x the default)
        webappContext.setMaxFormContentSize(600000);

        // webappContext.setClassLoader(new WebAppClassLoader(ClassLoader.getSystemClassLoader(), webappContext));
        webappContext.setClassLoader(new WebAppClassLoader(parentClassLoader, webappContext));

        logger.info("Loading WAR: " + warFile.getAbsolutePath() + " with context path set to " + contextPath);
        return webappContext;
    }

    private void dumpUrls() throws SocketException {
        final List<String> urls = new ArrayList<>();

        for (Connector connector : server.getConnectors()) {
            if (connector instanceof ServerConnector) {
                final ServerConnector serverConnector = (ServerConnector) connector;

                Set<String> hosts = new HashSet<>();

                // determine the hosts
                if (StringUtils.isNotBlank(serverConnector.getHost())) {
                    hosts.add(serverConnector.getHost());
                } else {
                    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                    if (networkInterfaces != null) {
                        for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                                hosts.add(inetAddress.getHostAddress());
                            }
                        }
                    }
                }

                // ensure some hosts were found
                if (!hosts.isEmpty()) {
                    String scheme = properties.isTlsEnabled() ? "https" : "http";

                    // dump each url
                    for (String host : hosts) {
                        urls.add(String.format("%s://%s:%s", scheme, host, serverConnector.getPort()));
                    }
                }
            }
        }

        if (urls.isEmpty()) {
            logger.warn("MiNiFi C2 Server has started, but the API is not available on any hosts. Please verify the host properties.");
        } else {
            // log the api location
            logger.info("MiNiFi C2 Server has started. The API is available at the following URLs:");
            for (final String url : urls) {
                logger.info(String.format("%s%s", url, C2_API_CONTEXT_PATH));
            }
        }
    }

    private void startUpFailure(Throwable t) {
        System.err.println("Failed to start web server: " + t.getMessage());
        System.err.println("Shutting down...");
        logger.warn("Failed to start web server... shutting down.", t);
        System.exit(1);
    }

    private static C2Properties loadProperties(File file) throws Exception {
        if (file == null || !file.exists() || !file.canRead()) {
            String path = (file == null ? "missing file" : file.getAbsolutePath());
            logger.error("Cannot read from '{}' -- file is missing or not readable", path);
            throw new IllegalArgumentException("NiFi Registry properties file missing or unreadable");
        }

        final C2Properties rawProperties = new C2Properties();
        try (final FileReader reader = new FileReader(file)) {
            rawProperties.load(reader);
            logger.info("Loaded {} properties from {}", rawProperties.size(), file.getAbsolutePath());
        } catch (final IOException ioe) {
            logger.error("Cannot load properties file due to " + ioe.getLocalizedMessage());
            throw new RuntimeException("Cannot load properties file due to " + ioe.getLocalizedMessage(), ioe);
        }

        return rawProperties;
    }

}
