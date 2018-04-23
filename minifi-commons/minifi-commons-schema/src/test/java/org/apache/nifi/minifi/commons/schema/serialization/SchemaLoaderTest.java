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
import org.apache.nifi.minifi.commons.schema.exception.SchemaLoaderException;
import org.apache.nifi.minifi.commons.schema.v1.ConfigSchemaV1;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SchemaLoaderTest {

    private static String TEST_BOOTSTRAP_FILE_LOCATION = "./src/test/resources/bootstrap.conf";

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
    public void testMinimalConfigV2Version() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v2.yml"));
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);
    }

    @Test
    public void testMinimalConfigV3Version() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v3.yml"));
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);
    }

    @Test
    public void testUnsupportedVersion() throws IOException, SchemaLoaderException {
        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v2.yml"));
        yamlAsMap.put(ConfigSchema.VERSION, "9999999");
        try {
            SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
            fail();
        } catch (SchemaLoaderException e) {
            assertEquals("YAML configuration version 9999999 not supported.  Supported versions: 1, 2, 3", e.getMessage());
        }
    }

    @Test
    public void testMinimalConfigV3VersionUnusedProperties() throws IOException, SchemaLoaderException {
        Properties inputProperties = getBootstrapProperties();
        inputProperties.setProperty("FLOW_NAME", "MiNiFi Flow");
        inputProperties.setProperty("PROCESSOR_1_CLASS", "class: org.apache.nifi.processors.standard.TailFile");
        inputProperties.setProperty("RELATIONSHIP_NAME", "- success");

        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v3.yml"), inputProperties);
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);
    }

    @Test
    public void testMinimalConfigV3VersionWithBasicProperties() throws IOException, SchemaLoaderException {
        Properties inputProperties = getBootstrapProperties();
        inputProperties.setProperty("FLOW_NAME", "MiNiFi Flow");
        inputProperties.setProperty("PROCESSOR_1_CLASS", "class: org.apache.nifi.processors.standard.TailFile");
        inputProperties.setProperty("RELATIONSHIP_NAME", "- success");
        inputProperties.setProperty("NOT_FOUND", "value");

        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v3_properties.yml"), inputProperties);
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);

        assertEquals("MiNiFi Flow", configSchema.getFlowControllerProperties().getName());
    }

    @Test
    public void testMinimalConfigV3VersionWithMultipleRelationshipProperties() throws IOException, SchemaLoaderException {
        Properties inputProperties = getBootstrapProperties();
        inputProperties.setProperty("FLOW_NAME", "MiNiFi Flow");
        inputProperties.setProperty("PROCESSOR_1_CLASS", "class: org.apache.nifi.processors.standard.TailFile");
        inputProperties.setProperty("RELATIONSHIP_NAME", "- success\n      - failure");

        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v3_properties.yml"), inputProperties);
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);

        assertEquals("MiNiFi Flow", configSchema.getFlowControllerProperties().getName());
        List<ConnectionSchema> connections = configSchema.getProcessGroupSchema().getConnections();
        assertEquals("failure", connections.get(0).getSourceRelationshipNames().get(1));
    }

    @Test
    public void testMinimalConfigV3VersionWithMissingProperty() throws IOException, SchemaLoaderException {
        Properties inputProperties = new Properties();
        inputProperties.setProperty("PROCESSOR_1_CLASS", "class: org.apache.nifi.processors.standard.TailFile");
        inputProperties.setProperty("RELATIONSHIP_NAME", "- success");

        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v3_properties.yml"), inputProperties);
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);

        assertEquals("___FLOW_NAME___", configSchema.getFlowControllerProperties().getName());
    }

    @Test
    public void testMinimalConfigV3VersionWithMultipleProperties() throws IOException, SchemaLoaderException {
        Properties inputProperties = getBootstrapProperties();
        inputProperties.setProperty("FLOW_NAME", "MiNiFi Flow");
        inputProperties.setProperty("PROCESSOR_1_CLASS", "class: org.apache.nifi.processors.standard.TailFile");
        inputProperties.setProperty("RELATIONSHIP_NAME", "- success");
        inputProperties.setProperty("NOT_FOUND", "value");
        inputProperties.setProperty("scheduling.strategy", "TIMER_DRIVEN");

        Map<String, Object> yamlAsMap = SchemaLoader.loadYamlAsMap(SchemaLoaderTest.class.getClassLoader().getResourceAsStream("config-minimal-v3_multiple-properties.yml"), inputProperties);
        yamlAsMap.put(ConfigSchema.VERSION, ConfigSchema.CONFIG_VERSION);
        ConfigSchema configSchema = SchemaLoader.loadConfigSchemaFromYaml(yamlAsMap);
        validateMinimalConfigVersion1Parse(configSchema);

        List<ProcessorSchema> processors = configSchema.getProcessGroupSchema().getProcessors();
        processors.forEach(p -> assertEquals("TIMER_DRIVEN", p.getSchedulingStrategy()));
    }

    private void validateMinimalConfigVersion1Parse(ConfigSchema configSchema) {
        assertTrue(configSchema instanceof ConfigSchema);

        List<ConnectionSchema> connections = configSchema.getProcessGroupSchema().getConnections();
        assertNotNull(connections);
        assertEquals(1, connections.size());
        assertNotNull(connections.get(0).getId());
        assertEquals("success", connections.get(0).getSourceRelationshipNames().get(0));

        List<ProcessorSchema> processors = configSchema.getProcessGroupSchema().getProcessors();
        assertNotNull(processors);
        assertEquals(2, processors.size());
        processors.forEach(p -> assertNotNull(p.getId()));

        assertEquals("Expected no errors, got: " + configSchema.getValidationIssues(), 0, configSchema.getValidationIssues().size());
    }

    private Properties getBootstrapProperties() throws IOException {
        final Properties bootstrapProperties = new Properties();
        try (final FileInputStream fis = new FileInputStream(TEST_BOOTSTRAP_FILE_LOCATION)) {
            bootstrapProperties.load(fis);
        }
        return bootstrapProperties;
    }
}
