/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.nifi.minifi.commons.schema;

import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class RemoteProcessGroupSchemaTest {
    @Test
    public void testNoPropertiesSet() {
        RemoteProcessGroupSchema remoteProcessGroupSchema = new RemoteProcessGroupSchema(new HashMap<>());
        validateIssuesNumMatches(3, remoteProcessGroupSchema);
        assertEquals(RemoteProcessGroupSchema.DEFAULT_PROXY_HOST, remoteProcessGroupSchema.getProxyHost());
        assertEquals(RemoteProcessGroupSchema.DEFAULT_PROXY_PORT, remoteProcessGroupSchema.getProxyPort());
        assertEquals(RemoteProcessGroupSchema.DEFAULT_PROXY_USER, remoteProcessGroupSchema.getProxyUser());
        assertEquals(RemoteProcessGroupSchema.DEFAULT_PROXY_PASSWORD, remoteProcessGroupSchema.getProxyPassword());
    }

    @Test
    public void testInputPortsRootGroup() {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonPropertyKeys.INPUT_PORTS_KEY, Arrays.asList(createPortSchema("f94d2469-39f8-4f07-a0d8-acd9396f639e", "testName", ConfigSchema.TOP_LEVEL_NAME).toMap()));
        map.put(RemoteProcessGroupSchema.URL_KEY, "http://localhost:8080/nifi");
        map.put(CommonPropertyKeys.ID_KEY, "a58d2fab-7efe-4cb7-8224-12a60bd8003d");
        validateIssuesNumMatches(0, new RemoteProcessGroupSchema(map));
    }

    @Test
    public void testTransportProtocol() {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonPropertyKeys.INPUT_PORTS_KEY, Arrays.asList(createPortSchema("f94d2469-39f8-4f07-a0d8-acd9396f639e", "testName", ConfigSchema.TOP_LEVEL_NAME).toMap()));
        map.put(RemoteProcessGroupSchema.URL_KEY, "http://localhost:8080/nifi");
        map.put(CommonPropertyKeys.ID_KEY, "a58d2fab-7efe-4cb7-8224-12a60bd8003d");
        map.put(RemoteProcessGroupSchema.TRANSPORT_PROTOCOL_KEY, "not valid");
        validateIssuesNumMatches(1, new RemoteProcessGroupSchema(map));

        map.put(RemoteProcessGroupSchema.TRANSPORT_PROTOCOL_KEY, "RAW");
        RemoteProcessGroupSchema first =  new RemoteProcessGroupSchema(map);
        validateIssuesNumMatches(0,first);
        assertEquals(first.getTransportProtocol(), "RAW");

        map.put(RemoteProcessGroupSchema.TRANSPORT_PROTOCOL_KEY, "HTTP");
        RemoteProcessGroupSchema second =  new RemoteProcessGroupSchema(map);
        validateIssuesNumMatches(0, second);
        assertEquals(second.getTransportProtocol(), "HTTP");
    }

    @Test
    public void testProxySettings() {
        Map<String, Object> map = new HashMap<>();
        map.put(RemoteProcessGroupSchema.PROXY_HOST_KEY, "host");
        map.put(RemoteProcessGroupSchema.PROXY_PORT_KEY, 1234);
        map.put(RemoteProcessGroupSchema.PROXY_USER_KEY, "user");
        map.put(RemoteProcessGroupSchema.PROXY_PASSWORD_KEY, "password");
        RemoteProcessGroupSchema remoteProcessGroupSchema = new RemoteProcessGroupSchema(map);

        assertEquals("host", remoteProcessGroupSchema.getProxyHost());
        assertEquals(Integer.valueOf(1234), remoteProcessGroupSchema.getProxyPort());
        assertEquals("user", remoteProcessGroupSchema.getProxyUser());
        assertEquals("password", remoteProcessGroupSchema.getProxyPassword());
    }

    private PortSchema createPortSchema(String id, String name, String wrapperName) {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonPropertyKeys.ID_KEY, id);
        map.put(CommonPropertyKeys.NAME_KEY, name);
        return new PortSchema(map, wrapperName);
    }

    private void validateIssuesNumMatches(int expected, RemoteProcessGroupSchema remoteProcessGroupSchema) {
        int actual = remoteProcessGroupSchema.getValidationIssues().size();
        String issues = "[" + System.lineSeparator() + remoteProcessGroupSchema.getValidationIssues().stream().collect(Collectors.joining("," + System.lineSeparator())) + "]";
        assertEquals("Expected " + expected + " issue(s), got " + actual + ": " + issues, expected, actual);
    }
}
