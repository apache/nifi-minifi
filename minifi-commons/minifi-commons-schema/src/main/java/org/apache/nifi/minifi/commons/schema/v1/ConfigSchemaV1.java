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

package org.apache.nifi.minifi.commons.schema.v1;

import org.apache.nifi.minifi.commons.schema.ComponentStatusRepositorySchema;
import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.ConnectionSchema;
import org.apache.nifi.minifi.commons.schema.ContentRepositorySchema;
import org.apache.nifi.minifi.commons.schema.CorePropertiesSchema;
import org.apache.nifi.minifi.commons.schema.FlowControllerSchema;
import org.apache.nifi.minifi.commons.schema.FlowFileRepositorySchema;
import org.apache.nifi.minifi.commons.schema.ProcessorSchema;
import org.apache.nifi.minifi.commons.schema.ProvenanceReportingSchema;
import org.apache.nifi.minifi.commons.schema.ProvenanceRepositorySchema;
import org.apache.nifi.minifi.commons.schema.RemoteInputPortSchema;
import org.apache.nifi.minifi.commons.schema.RemoteProcessingGroupSchema;
import org.apache.nifi.minifi.commons.schema.SecurityPropertiesSchema;
import org.apache.nifi.minifi.commons.schema.common.BaseSchema;
import org.apache.nifi.minifi.commons.schema.common.ConvertableSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.commons.schema.ConfigSchema.TOP_LEVEL_NAME;
import static org.apache.nifi.minifi.commons.schema.ConfigSchema.VERSION;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.COMPONENT_STATUS_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.CONNECTIONS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.CONTENT_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.CORE_PROPS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.FLOWFILE_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.FLOW_CONTROLLER_PROPS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.PROCESSORS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.PROVENANCE_REPORTING_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.PROVENANCE_REPO_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.REMOTE_PROCESSING_GROUPS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.SECURITY_PROPS_KEY;

public class ConfigSchemaV1 extends BaseSchema implements ConvertableSchema<ConfigSchema> {
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_NAMES = "Found the following duplicate processor names: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_NAMES = "Found the following duplicate connection names: ";
    public static final String FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_PROCESSING_GROUP_NAMES = "Found the following duplicate remote processing group names: ";
    public static final String CANNOT_LOOK_UP_PROCESSOR_ID_FROM_PROCESSOR_NAME_DUE_TO_DUPLICATE_PROCESSOR_NAMES = "Cannot look up Processor id from Processor name due to duplicate Processor names: ";
    public static final int CONFIG_VERSION = 1;
    public static final String CONNECTIONS_REFER_TO_PROCESSOR_NAMES_THAT_DONT_EXIST = "Connection(s) refer to Processor names that don't exist: ";
    private FlowControllerSchema flowControllerProperties;
    private CorePropertiesSchema coreProperties;
    private FlowFileRepositorySchema flowfileRepositoryProperties;
    private ContentRepositorySchema contentRepositoryProperties;
    private ComponentStatusRepositorySchema componentStatusRepositoryProperties;
    private SecurityPropertiesSchema securityProperties;
    private List<ProcessorSchemaV1> processors;
    private List<ConnectionSchemaV1> connections;
    private List<RemoteProcessingGroupSchema> remoteProcessingGroups;
    private ProvenanceReportingSchema provenanceReportingProperties;

    private ProvenanceRepositorySchema provenanceRepositorySchema;

    public ConfigSchemaV1(Map map) {
        flowControllerProperties = getMapAsType(map, FLOW_CONTROLLER_PROPS_KEY, FlowControllerSchema.class, TOP_LEVEL_NAME, true);

        coreProperties = getMapAsType(map, CORE_PROPS_KEY, CorePropertiesSchema.class, TOP_LEVEL_NAME, false);
        flowfileRepositoryProperties = getMapAsType(map, FLOWFILE_REPO_KEY, FlowFileRepositorySchema.class, TOP_LEVEL_NAME, false);
        contentRepositoryProperties = getMapAsType(map, CONTENT_REPO_KEY, ContentRepositorySchema.class, TOP_LEVEL_NAME, false);
        provenanceRepositorySchema = getMapAsType(map, PROVENANCE_REPO_KEY, ProvenanceRepositorySchema.class, TOP_LEVEL_NAME, false);
        componentStatusRepositoryProperties = getMapAsType(map, COMPONENT_STATUS_REPO_KEY, ComponentStatusRepositorySchema.class, TOP_LEVEL_NAME, false);
        securityProperties = getMapAsType(map, SECURITY_PROPS_KEY, SecurityPropertiesSchema.class, TOP_LEVEL_NAME, false);

        processors = convertListToType(getOptionalKeyAsType(map, PROCESSORS_KEY, List.class, TOP_LEVEL_NAME, new ArrayList<>()), PROCESSORS_KEY, ProcessorSchemaV1.class, TOP_LEVEL_NAME);

        remoteProcessingGroups = convertListToType(getOptionalKeyAsType(map, REMOTE_PROCESSING_GROUPS_KEY, List.class, TOP_LEVEL_NAME, new ArrayList<>()), "remote processing group",
                RemoteProcessingGroupSchema.class, REMOTE_PROCESSING_GROUPS_KEY);

        connections = convertListToType(getOptionalKeyAsType(map, CONNECTIONS_KEY, List.class, TOP_LEVEL_NAME, new ArrayList<>()), CONNECTIONS_KEY, ConnectionSchemaV1.class, TOP_LEVEL_NAME);

        provenanceReportingProperties = getMapAsType(map, PROVENANCE_REPORTING_KEY, ProvenanceReportingSchema.class, TOP_LEVEL_NAME, false, false);

        addIssuesIfNotNull(flowControllerProperties);
        addIssuesIfNotNull(coreProperties);
        addIssuesIfNotNull(flowfileRepositoryProperties);
        addIssuesIfNotNull(contentRepositoryProperties);
        addIssuesIfNotNull(componentStatusRepositoryProperties);
        addIssuesIfNotNull(securityProperties);
        addIssuesIfNotNull(provenanceReportingProperties);
        addIssuesIfNotNull(provenanceRepositorySchema);
        addIssuesIfNotNull(processors);
        addIssuesIfNotNull(connections);
        addIssuesIfNotNull(remoteProcessingGroups);

        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_PROCESSOR_NAMES, processors.stream().map(ProcessorSchemaV1::getName).collect(Collectors.toList()));
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_CONNECTION_NAMES, connections.stream().map(ConnectionSchemaV1::getName).collect(Collectors.toList()));
        checkForDuplicates(this::addValidationIssue, FOUND_THE_FOLLOWING_DUPLICATE_REMOTE_PROCESSING_GROUP_NAMES, remoteProcessingGroups.stream().map(RemoteProcessingGroupSchema::getName)
                .collect(Collectors.toList()));
    }

    protected List<ProcessorSchema> getProcessorSchemas() {
        Map<String, Integer> idMap = new HashMap<>();
        List<ProcessorSchema> processorSchemas = new ArrayList<>(processors.size());

        for (ProcessorSchemaV1 processor : processors) {
            ProcessorSchema processorSchema = processor.convert();
            processorSchema.setId(getUniqueId(idMap, processorSchema.getName()));
            processorSchemas.add(processorSchema);
        }

        return processorSchemas;
    }

    protected List<ConnectionSchema> getConnectionSchemas(List<ProcessorSchema> processors, List<String> validationIssues) {
        Map<String, Integer> idMap = new HashMap<>();

        Map<String, String> processorNameToIdMap = new HashMap<>();

        // We can't look up id by name for names that appear more than once
        Set<String> duplicateProcessorNames = new HashSet<>();

        if (processors != null) {
            processors.stream().forEachOrdered(p -> processorNameToIdMap.put(p.getName(), p.getId()));

            Set<String> processorNames = new HashSet<>();
            processors.stream().map(ProcessorSchema::getName).forEachOrdered(n -> {
                if (!processorNames.add(n)) {
                    duplicateProcessorNames.add(n);
                }
            });
        }

        Set<String> remoteInputPortIds = new HashSet<>();
        if (remoteProcessingGroups != null) {
            remoteInputPortIds.addAll(remoteProcessingGroups.stream().filter(r -> r.getInputPorts() != null)
                    .flatMap(r -> r.getInputPorts().stream()).map(RemoteInputPortSchema::getId).collect(Collectors.toSet()));
        }

        Set<String> problematicDuplicateNames = new HashSet<>();
        Set<String> missingProcessorNames = new HashSet<>();

        List<ConnectionSchema> connectionSchemas = new ArrayList<>(connections.size());
        for (ConnectionSchemaV1 connection : connections) {
            ConnectionSchema convert = connection.convert();
            convert.setId(getUniqueId(idMap, convert.getName()));

            String sourceName = connection.getSourceName();
            if (remoteInputPortIds.contains(sourceName)) {
                convert.setSourceId(sourceName);
            } else {
                if (duplicateProcessorNames.contains(sourceName)) {
                    problematicDuplicateNames.add(sourceName);
                }
                String sourceId = processorNameToIdMap.get(sourceName);
                if (StringUtil.isNullOrEmpty(sourceId)) {
                    missingProcessorNames.add(sourceName);
                } else {
                    convert.setSourceId(sourceId);
                }
            }

            String destinationName = connection.getDestinationName();
            if (remoteInputPortIds.contains(destinationName)) {
                convert.setDestinationId(destinationName);
            } else {
                if (duplicateProcessorNames.contains(destinationName)) {
                    problematicDuplicateNames.add(destinationName);
                }
                String destinationId = processorNameToIdMap.get(destinationName);
                if (StringUtil.isNullOrEmpty(destinationId)) {
                    missingProcessorNames.add(destinationName);
                } else {
                    convert.setDestinationId(destinationId);
                }
            }
            connectionSchemas.add(convert);
        }

        if (problematicDuplicateNames.size() > 0) {
            validationIssues.add(CANNOT_LOOK_UP_PROCESSOR_ID_FROM_PROCESSOR_NAME_DUE_TO_DUPLICATE_PROCESSOR_NAMES
                    + problematicDuplicateNames.stream().collect(Collectors.joining(", ")));
        }
        if (missingProcessorNames.size() > 0) {
            validationIssues.add(CONNECTIONS_REFER_TO_PROCESSOR_NAMES_THAT_DONT_EXIST + missingProcessorNames.stream().sorted().collect(Collectors.joining(", ")));
        }
        return connectionSchemas;
    }

    @Override
    public ConfigSchema convert() {
        Map<String, Object> map = new HashMap<>();
        map.put(VERSION, getVersion());
        putIfNotNull(map, FLOW_CONTROLLER_PROPS_KEY, flowControllerProperties);
        putIfNotNull(map, CORE_PROPS_KEY, coreProperties);
        putIfNotNull(map, FLOWFILE_REPO_KEY, flowfileRepositoryProperties);
        putIfNotNull(map, CONTENT_REPO_KEY, contentRepositoryProperties);
        putIfNotNull(map, PROVENANCE_REPO_KEY, provenanceRepositorySchema);
        putIfNotNull(map, COMPONENT_STATUS_REPO_KEY, componentStatusRepositoryProperties);
        putIfNotNull(map, SECURITY_PROPS_KEY, securityProperties);
        List<ProcessorSchema> processorSchemas = getProcessorSchemas();
        putListIfNotNull(map, PROCESSORS_KEY, processorSchemas);
        List<String> validationIssues = getValidationIssues();
        putListIfNotNull(map, CONNECTIONS_KEY, getConnectionSchemas(processorSchemas, validationIssues));
        putListIfNotNull(map, REMOTE_PROCESSING_GROUPS_KEY, remoteProcessingGroups);
        putIfNotNull(map, PROVENANCE_REPORTING_KEY, provenanceReportingProperties);
        return new ConfigSchema(map, validationIssues);
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
    public static String getUniqueId(Map<String, Integer> ids, String name) {
        String baseId = StringUtil.isNullOrEmpty(name) ? EMPTY_NAME : ID_REPLACE_PATTERN.matcher(name).replaceAll("_");
        String id = baseId;
        Integer idNum = ids.get(baseId);
        while (ids.containsKey(id)) {
            id = baseId + "_" + idNum++;
        }
        // Using != on a string comparison here is intentional.  The two will be reference equal iff the body of the while loop was never executed.
        if (id != baseId) {
            ids.put(baseId, idNum);
        }
        ids.put(id, 2);
        return id;
    }

    public int getVersion() {
        return CONFIG_VERSION;
    }
}
