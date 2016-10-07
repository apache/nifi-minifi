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

package org.apache.nifi.minifi.commons.schema;

import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;
import org.apache.nifi.minifi.commons.schema.exception.SchemaLoaderException;
import org.apache.nifi.minifi.commons.schema.serialization.SchemaLoader;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class ConfigSchemaTest {
    @Test
    public void testValid() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(ConfigSchemaTest.class.getClassLoader().getResourceAsStream("config-minimal-v2.yml"));
        ConfigSchema configSchema = new ConfigSchema(yamlAsMap);
        List<String> validationIssues = configSchema.getValidationIssues();
        assertEquals(0, validationIssues.size());
    }
    @Test
    public void testValidationIssuesFromOlder() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(ConfigSchemaTest.class.getClassLoader().getResourceAsStream("config-minimal.yml"));
        ConfigSchema configSchema = new ConfigSchema(yamlAsMap);
        List<String> validationIssues = configSchema.getValidationIssues();
        assertNotEquals(0, validationIssues.size());
    }

    @Test
    public void testProcessorDuplicateValidationNegativeCase() {
        ConfigSchema configSchema = new ConfigSchema(Collections.singletonMap(CommonPropertyKeys.PROCESSORS_KEY, getListWithKeyValues(CommonPropertyKeys.ID_KEY, "testId1", "testId2")));
        assertMessageDoesNotExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_IDS);
    }

    @Test
    public void testProcessorDuplicateValidationPositiveCase() {
        ConfigSchema configSchema = new ConfigSchema(Collections.singletonMap(CommonPropertyKeys.PROCESSORS_KEY, getListWithKeyValues(CommonPropertyKeys.ID_KEY, "testId1", "testId1")));
        assertMessageDoesExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_IDS);
    }

    @Test
    public void testConnectionDuplicateValidationNegativeCase() {
        ConfigSchema configSchema = new ConfigSchema(Collections.singletonMap(CommonPropertyKeys.CONNECTIONS_KEY, getListWithKeyValues(CommonPropertyKeys.ID_KEY, "testId1", "testId2")));
        assertMessageDoesNotExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_IDS);
    }

    @Test
    public void testConnectionDuplicateValidationPositiveCase() {
        ConfigSchema configSchema = new ConfigSchema(Collections.singletonMap(CommonPropertyKeys.CONNECTIONS_KEY, getListWithKeyValues(CommonPropertyKeys.ID_KEY, "testId1", "testId1")));
        assertMessageDoesExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_IDS);
    }

    @Test
    public void testRemoteProcessingGroupDuplicateValidationNegativeCase() {
        ConfigSchema configSchema = new ConfigSchema(Collections.singletonMap(CommonPropertyKeys.REMOTE_PROCESSING_GROUPS_KEY, getListWithNames("testName1", "testName2")));
        assertMessageDoesNotExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_PROCESSING_GROUP_NAMES);
    }

    @Test
    public void testRemoteProcessingGroupDuplicateValidationPositiveCase() {
        ConfigSchema configSchema = new ConfigSchema(Collections.singletonMap(CommonPropertyKeys.REMOTE_PROCESSING_GROUPS_KEY, getListWithNames("testName1", "testName1")));
        assertMessageDoesExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_PROCESSING_GROUP_NAMES);
    }

    public static List<Map<String, Object>> getListWithNames(String... names) {
        return getListWithKeyValues(CommonPropertyKeys.NAME_KEY, names);
    }

    public static List<Map<String, Object>> getListWithKeyValues(String key, String... values) {
        List<Map<String, Object>> result = new ArrayList<>(values.length);
        for (String value : values) {
            result.add(Collections.singletonMap(key, value));
        }
        return result;
    }

    public static void assertMessageDoesNotExist(ConfigSchema configSchema, String message) {
        for (String validationIssue : configSchema.getValidationIssues()) {
            assertFalse("Did not expect to find message: " + validationIssue, validationIssue.startsWith(message));
        }
    }

    public static void assertMessageDoesExist(ConfigSchema configSchema, String message) {
        for (String validationIssue : configSchema.getValidationIssues()) {
            if (validationIssue.startsWith(message)) {
                return;
            }
        }
        fail("Expected to find message starting with: " + message);
    }
}
