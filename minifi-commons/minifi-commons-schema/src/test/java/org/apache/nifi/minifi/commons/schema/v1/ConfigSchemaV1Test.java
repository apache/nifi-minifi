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

package org.apache.nifi.minifi.commons.schema.v1;

import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.ConnectionSchema;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.ConfigSchemaTest.assertMessageDoesExist;
import static org.apache.nifi.minifi.commons.schema.ConfigSchemaTest.assertMessageDoesNotExist;
import static org.apache.nifi.minifi.commons.schema.ConfigSchemaTest.getListWithKeyValues;
import static org.junit.Assert.assertEquals;

public class ConfigSchemaV1Test {
    @Test
    public void testGetUniqueIdEmptySet() {
        String testId = "testId";
        assertEquals(testId + "___", ConfigSchemaV1.getUniqueId(new HashMap<>(), testId + "/ $"));
    }

    @Test
    public void testConnectionNameDuplicateValidationNegativeCase() {
        ConfigSchemaV1 configSchema = new ConfigSchemaV1(Collections.singletonMap(CommonPropertyKeys.CONNECTIONS_KEY, getListWithKeyValues(CommonPropertyKeys.NAME_KEY, "testName1", "testName2")));
        assertMessageDoesNotExist(configSchema, ConfigSchemaV1.FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_NAMES);
    }

    @Test
    public void testConnectionNameDuplicateValidationPositiveCase() {
        ConfigSchemaV1 configSchema = new ConfigSchemaV1(Collections.singletonMap(CommonPropertyKeys.CONNECTIONS_KEY, getListWithKeyValues(CommonPropertyKeys.NAME_KEY, "testName1", "testName1")));
        assertMessageDoesExist(configSchema, ConfigSchemaV1.FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_NAMES);
    }

    @Test
    public void testConnectionGeneratedIds() {
        List<Map<String, Object>> listWithKeyValues = getListWithKeyValues(CommonPropertyKeys.NAME_KEY, "test", "test", "test_2");

        // These ids should be honored even though they're last
        listWithKeyValues.addAll(getListWithKeyValues(CommonPropertyKeys.ID_KEY, "test", "test_2"));

        ConfigSchemaV1 configSchema = new ConfigSchemaV1(Collections.singletonMap(CommonPropertyKeys.CONNECTIONS_KEY, listWithKeyValues));
        assertMessageDoesNotExist(configSchema, ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_IDS);
        List<ConnectionSchema> connections = configSchema.getConnections();
        assertEquals(5, connections.size());

        // Generated unique ids
        assertEquals("test_3", connections.get(0).getId());
        assertEquals("test_4", connections.get(1).getId());
        assertEquals("test_2_2", connections.get(2).getId());

        // Specified ids
        assertEquals("test", connections.get(3).getId());
        assertEquals("test_2", connections.get(4).getId());
    }

    @Test
    public void testGetUniqueIdConflicts() {
        Map<String, Integer> ids = new HashMap<>();
        assertEquals("test_id", ConfigSchemaV1.getUniqueId(ids, "test/id"));
        assertEquals("test_id_2", ConfigSchemaV1.getUniqueId(ids, "test$id"));
        assertEquals("test_id_3", ConfigSchemaV1.getUniqueId(ids, "test$id"));
        assertEquals("test_id_4", ConfigSchemaV1.getUniqueId(ids, "test$id"));
        assertEquals("test_id_5", ConfigSchemaV1.getUniqueId(ids, "test$id"));
        assertEquals("test_id_2_2", ConfigSchemaV1.getUniqueId(ids, "test_id_2"));
    }
}
