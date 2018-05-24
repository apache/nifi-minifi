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
import org.apache.nifi.minifi.commons.schema.common.ConvertableSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;
import org.apache.nifi.minifi.commons.schema.exception.SchemaLoaderException;
import org.apache.nifi.minifi.commons.schema.v1.ConfigSchemaV1;
import org.apache.nifi.minifi.commons.schema.v2.ConfigSchemaV2;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SchemaLoader {
    private static final String ESCAPE_CHARACTERS = "___";

    private static final Map<String, Function<Map, ConvertableSchema<ConfigSchema>>> configSchemaFactories = initConfigSchemaFactories();

    private static Map<String, Function<Map, ConvertableSchema<ConfigSchema>>> initConfigSchemaFactories() {
        Map<String, Function<Map, ConvertableSchema<ConfigSchema>>> result = new HashMap<>();
        result.put(String.valueOf((Object) null), ConfigSchemaV1::new);
        result.put("", ConfigSchemaV1::new);
        result.put(Integer.toString(ConfigSchemaV1.CONFIG_VERSION), ConfigSchemaV1::new);
        result.put(Integer.toString(ConfigSchemaV2.CONFIG_VERSION), ConfigSchemaV2::new);
        result.put(Integer.toString(ConfigSchema.CONFIG_VERSION), ConfigSchema::new);
        return result;
    }

    /*
        Would've liked to not pull the config file into one String and instead replace as the sourceStream is read by the underlying yaml parser but SnakeYaml wrote it's
        own 'StreamReader' and it would take relatively significant work to replace as the Stream is read (i.e. by writing an implementation of a bufferedReader). That said, the whole config
        file shouldn't be that large and is going to all be in memory eventually as it's parsed by SnakeYaml so returning a String here is less worrying.
     */
    private static String readInputAndTranslateProperties(InputStream sourceStream, Properties inputProperties) throws IOException {
        // Create a map of the patterns to look for and their respective replace values
        final Map<Pattern, String> propertyPatterns = new HashMap<>();
        for (String key :inputProperties.stringPropertyNames()) {
            final Pattern propertyPattern = Pattern.compile(ESCAPE_CHARACTERS + "(" + key + ")" + ESCAPE_CHARACTERS);

            propertyPatterns.put(propertyPattern, inputProperties.getProperty(key));
        }

        StringBuilder returnValue = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(sourceStream));

        // Line by line, find and replace the properties using the compiled patterns
        for(String line; (line = br.readLine()) != null; ) {
            String lineToReturn = line;

            for (Entry<Pattern, String> patternEntry: propertyPatterns.entrySet()) {
                final Pattern pattern = patternEntry.getKey();

                final Matcher matcher = pattern.matcher(lineToReturn);
                lineToReturn = matcher.replaceAll(patternEntry.getValue());
            }

            returnValue.append(lineToReturn);
            // Read by Yaml parser and not written to disk so no need to be configurable
            returnValue.append(System.lineSeparator());
        }
        return returnValue.toString();
    }

    public static Map<String, Object> loadYamlAsMap(InputStream sourceStream) throws IOException, SchemaLoaderException {
        return loadYamlAsMap(sourceStream, new Properties());
    }

    public static Map<String, Object> loadYamlAsMap(InputStream sourceStream, Properties inputProperties) throws IOException, SchemaLoaderException {
        try {
            String configString = readInputAndTranslateProperties(sourceStream, inputProperties);
            Yaml yaml = new Yaml();

            // Parse the YAML file
            final Object loadedObject = yaml.load(configString);

            // Verify the parsed object is a Map structure
            if (loadedObject instanceof Map) {
                return (Map<String, Object>) loadedObject;
            } else {
                throw new SchemaLoaderException("Provided YAML configuration is not a Map");
            }
        } catch (YAMLException e) {
            throw new IOException(e);
        } finally {
            sourceStream.close();
        }
    }

    public static ConfigSchema loadConfigSchemaFromYaml(InputStream sourceStream) throws IOException, SchemaLoaderException {
        return loadConfigSchemaFromYaml(loadYamlAsMap(sourceStream));
    }

    public static ConfigSchema loadConfigSchemaFromYaml(InputStream sourceStream, Properties inputProperties) throws IOException, SchemaLoaderException {
        return loadConfigSchemaFromYaml(loadYamlAsMap(sourceStream, inputProperties));
    }

    public static ConfigSchema loadConfigSchemaFromYaml(Map<String, Object> yamlAsMap) throws SchemaLoaderException {
        return loadConvertableSchemaFromYaml(yamlAsMap).convert();
    }

    public static ConvertableSchema<ConfigSchema> loadConvertableSchemaFromYaml(InputStream inputStream) throws SchemaLoaderException, IOException {
        return loadConvertableSchemaFromYaml(loadYamlAsMap(inputStream));
    }

    public static ConvertableSchema<ConfigSchema> loadConvertableSchemaFromYaml(InputStream inputStream, Properties inputProperties) throws SchemaLoaderException, IOException {
        return loadConvertableSchemaFromYaml(loadYamlAsMap(inputStream, inputProperties));
    }

    public static ConvertableSchema<ConfigSchema> loadConvertableSchemaFromYaml(Map<String, Object> yamlAsMap) throws SchemaLoaderException {
        String version = String.valueOf(yamlAsMap.get(ConfigSchema.VERSION));
        Function<Map, ConvertableSchema<ConfigSchema>> schemaFactory = configSchemaFactories.get(version);
        if (schemaFactory == null) {
            throw new SchemaLoaderException("YAML configuration version " + version + " not supported.  Supported versions: "
                    + configSchemaFactories.keySet().stream().filter(s -> !StringUtil.isNullOrEmpty(s) && !String.valueOf((Object) null).equals(s)).sorted().collect(Collectors.joining(", ")));
        }
        return schemaFactory.apply(yamlAsMap);
    }
}
