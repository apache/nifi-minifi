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
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.CONNECTIONS_KEY;

/**
 *
 */
public class ConfigSchemaV1 extends ConfigSchema {
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_NAMES = "Found the following duplicate connection names: ";
    public static final String EMPTY_NAME = "empty_name";
    public static final int CONFIG_VERSION = 1;

    public ConfigSchemaV1(Map map) {
        super(map);
    }

    /**
     * Will replace all characters not in [A-Za-z0-9_] with _
     * <p>
     * This has potential for collisions so it will also append numbers as necessary to prevent that
     *
     * @param ids  id map of already incremented numbers
     * @param name the name
     * @return a unique filesystem-friendly id
     */
    protected static String getUniqueId(Map<String, Integer> ids, String name) {
        String baseId = StringUtil.isNullOrEmpty(name) ? EMPTY_NAME : name.replaceAll("[^A-Za-z0-9_]", "_");
        String id = baseId;
        Integer idNum = ids.get(baseId);
        while (ids.containsKey(id)) {
            id = baseId + "_" + idNum++;
        }
        if (id != baseId) {
            ids.put(baseId, idNum);
        }
        ids.put(id, 2);
        return id;
    }

    @Override
    protected List<ConnectionSchema> getConnectionSchemas(List<Map> connectionMaps) {
        if (connectionMaps != null) {
            List<ConnectionSchemaV1> connections = convertListToType(connectionMaps, "connection", ConnectionSchemaV1.class, CONNECTIONS_KEY);
            Map<String, Integer> idMap = connections.stream().map(ConnectionSchema::getId).filter(s -> !StringUtil.isNullOrEmpty(s)).collect(Collectors.toMap(Function.identity(), s -> 2));

            // Set unset ids
            connections.stream().filter(connection -> StringUtil.isNullOrEmpty(connection.getId())).forEachOrdered(connection -> connection.setId(getUniqueId(idMap, connection.getName())));

            return new ArrayList<>(connections);
        }
        return null;
    }

    @Override
    public List<String> getValidationIssues() {
        List<String> validationIssues = new ArrayList<>(super.getValidationIssues());
        List<ConnectionSchema> connections = getConnections();
        if (connections != null) {
            checkForDuplicates(validationIssues::add, FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_NAMES, connections.stream().map(ConnectionSchema::getName).collect(Collectors.toList()));
        }
        return Collections.unmodifiableList(validationIssues);
    }

    @Override
    public int getVersion() {
        return CONFIG_VERSION;
    }
}
