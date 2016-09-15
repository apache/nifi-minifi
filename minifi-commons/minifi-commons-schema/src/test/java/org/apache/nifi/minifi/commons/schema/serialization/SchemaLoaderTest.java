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

package org.apache.nifi.minifi.commons.schema.serialization;

import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.ConnectionSchema;
import org.apache.nifi.minifi.commons.schema.ProcessorSchema;
import org.apache.nifi.minifi.commons.schema.common.BaseSchema;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;
import org.apache.nifi.minifi.commons.schema.exception.SchemaLoaderException;
import org.apache.nifi.minifi.commons.schema.v1.ConfigSchemaV1;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SchemaLoaderTest {
    @Test
    public void testMinimalConfigNoVersion() throws IOException, SchemaLoaderException {
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal.yml"));
        validateMinimalConfigVersion1Parse(configSchema);
    }

    @Test
    public void testMinimalConfigEmptyVersion() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal.yml"));
        yamlAsMap.put(ConfigSchema.VERSION, "");
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);
    }

    @Test
    public void testMinimalConfigV1Version() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal.yml"));
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchemaV1.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);
    }

    @Test
    public void testMinimalConfigCurrentVersion() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal.yml"));
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        assertEquals(ConfigSchema.class, configSchema.getClass());

        List<ConnectionSchema> connections = configSchema.getConnections();
        assertNotNull(connections);
        assertEquals(1, connections.size());
        assertNull(connections.get(0).getId());
        List<String> validationIssues = configSchema.getValidationIssues();
        assertEquals(6, validationIssues.size());
        assertEquals(ConfigSchema.FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_IDS + "null", validationIssues.get(0));
        assertEquals(BaseSchema.getIssueText(CommonPropertyKeys.ID_KEY, CommonPropertyKeys.PROCESSORS_KEY, BaseSchema.IT_WAS_NOT_FOUND_AND_IT_IS_REQUIRED), validationIssues.get(1));
        assertEquals(BaseSchema.getIssueText(CommonPropertyKeys.ID_KEY, CommonPropertyKeys.PROCESSORS_KEY, BaseSchema.IT_WAS_NOT_FOUND_AND_IT_IS_REQUIRED), validationIssues.get(2));
        assertEquals(BaseSchema.getIssueText(CommonPropertyKeys.ID_KEY, CommonPropertyKeys.CONNECTIONS_KEY, BaseSchema.IT_WAS_NOT_FOUND_AND_IT_IS_REQUIRED), validationIssues.get(3));
        assertEquals(BaseSchema.getIssueText(ConnectionSchema.SOURCE_ID_KEY, CommonPropertyKeys.CONNECTIONS_KEY, BaseSchema.IT_WAS_NOT_FOUND_AND_IT_IS_REQUIRED), validationIssues.get(4));
        assertEquals(BaseSchema.getIssueText(ConnectionSchema.DESTINATION_ID_KEY, CommonPropertyKeys.CONNECTIONS_KEY, BaseSchema.IT_WAS_NOT_FOUND_AND_IT_IS_REQUIRED), validationIssues.get(5));
    }

    private void validateMinimalConfigVersion1Parse(ConfigSchema configSchema) {
        assertTrue(configSchema instanceof ConfigSchemaV1);

        List<ConnectionSchema> connections = configSchema.getConnections();
        assertNotNull(connections);
        assertEquals(1, connections.size());
        assertNotNull(connections.get(0).getId());

        List<ProcessorSchema> processors = configSchema.getProcessors();
        assertNotNull(processors);
        assertEquals(2, processors.size());
        processors.forEach(p -> assertNotNull(p.getId()));

        assertEquals(0, configSchema.getValidationIssues().size());
    }
}
