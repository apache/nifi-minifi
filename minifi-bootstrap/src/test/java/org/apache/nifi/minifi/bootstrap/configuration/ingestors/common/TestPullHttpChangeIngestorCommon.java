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

package org.apache.nifi.minifi.bootstrap.configuration.ingestors.common;

import org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeNotifier;
import org.apache.nifi.minifi.bootstrap.configuration.ListenerHandleResult;
import org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor;
import org.apache.nifi.minifi.bootstrap.configuration.mocks.MockChangeListener;
import org.apache.nifi.minifi.bootstrap.configuration.mocks.MockDifferentiator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.verify;

public abstract class TestPullHttpChangeIngestorCommon {

    public static volatile Server jetty;
    public static volatile int port;
    public static volatile PullHttpChangeIngestor pullHttpChangeIngestor;
    public static ConfigurationChangeNotifier testNotifier;
    public static MockDifferentiator<ByteBuffer> mockDifferentiator = new MockDifferentiator<>();;
    public static final String RESPONSE_STRING = "test";
    public static ByteBuffer configBuffer= ByteBuffer.wrap(RESPONSE_STRING.getBytes());
    public static final String ETAG = "testEtag";
    public static final String QUOTED_ETAG = "\"testEtag\"";

    public static void init() {
        QueuedThreadPool queuedThreadPool = new QueuedThreadPool();
        queuedThreadPool.setDaemon(true);
        jetty = new Server(queuedThreadPool);

        HandlerCollection handlerCollection = new HandlerCollection(true);
        handlerCollection.addHandler(new JettyHandler(RESPONSE_STRING));
        jetty.setHandler(handlerCollection);
    }

    @Before
    public void before() {
        Mockito.reset(testNotifier);
        Mockito.when(testNotifier.notifyListeners(Mockito.any())).thenReturn(Collections.singleton(new ListenerHandleResult(new MockChangeListener())));
    }

    @AfterClass
    public static void shutdown() throws Exception {
        jetty.stop();
    }

    @Test
    public void testNewUpdate(){
        pullHttpChangeIngestor.setUseEtag(false);
        mockDifferentiator.setNew(true);

        pullHttpChangeIngestor.run();

        verify(testNotifier, Mockito.times(1)).notifyListeners(Mockito.eq(configBuffer.asReadOnlyBuffer()));
    }


    @Test
    public void testNoUpdate() {
        pullHttpChangeIngestor.setUseEtag(false);

        mockDifferentiator.setNew(false);

        pullHttpChangeIngestor.run();

        verify(testNotifier, Mockito.never()).notifyListeners(Mockito.any());
    }


    @Test
    public void testUseEtag() {
        pullHttpChangeIngestor.setLastEtag("");
        pullHttpChangeIngestor.setUseEtag(true);

        mockDifferentiator.setNew(true);

        pullHttpChangeIngestor.run();

        verify(testNotifier, Mockito.times(1)).notifyListeners(Mockito.eq(configBuffer));

        pullHttpChangeIngestor.run();

        verify(testNotifier, Mockito.times(1)).notifyListeners(Mockito.any());

    }

    static class JettyHandler extends AbstractHandler {
        volatile String configResponse;


        public JettyHandler(String configResponse){
            this.configResponse = configResponse;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {

            baseRequest.setHandled(true);

            if ("GET".equals(request.getMethod())) {

                if (QUOTED_ETAG.equals(baseRequest.getHeader("If-None-Match"))){
                    writeOutput(response, null, 304);
                } else {
                    writeOutput(response, configResponse, 200);
                }
            } else {
                writeOutput(response, "not a GET request", 404);
            }
        }

        private void writeOutput(HttpServletResponse response, String responseBuffer, int responseCode) throws IOException {
            response.setStatus(responseCode);
            response.setHeader("ETag", ETAG);
            if (responseBuffer != null) {
                response.setContentType("text/plain");
                response.setContentLength(responseBuffer.length());
                response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
                try (PrintWriter writer = response.getWriter()) {
                    writer.print(responseBuffer);
                    writer.flush();
                }
            }
        }

    }
}
