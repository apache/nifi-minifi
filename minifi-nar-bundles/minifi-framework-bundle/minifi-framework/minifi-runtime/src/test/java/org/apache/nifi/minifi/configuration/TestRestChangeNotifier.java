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

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestRestChangeNotifier extends TestRestChangeNotifierCommon {

    @BeforeClass
    public static void setUp() throws InterruptedException {
        Properties properties = new Properties();
        properties.setProperty(RestChangeNotifier.TOMCAT_HOME_KEY, "./target/tomcat_home");
        properties.setProperty(RestChangeNotifier.PORT_KEY, "8339");
        restChangeNotifier = new RestChangeNotifier(properties);
        restChangeNotifier.registerListener(mockChangeListener);
        restChangeNotifier.start();

        client = new OkHttpClient();

        assertEquals(8339,restChangeNotifier.getPort());

        url = "http://"+ restChangeNotifier.getHost()+":"+ restChangeNotifier.getPort();
        Thread.sleep(1000);
    }

    @AfterClass
    public static void stop() throws InterruptedException {
        restChangeNotifier.stop();
        client = null;
    }
}
