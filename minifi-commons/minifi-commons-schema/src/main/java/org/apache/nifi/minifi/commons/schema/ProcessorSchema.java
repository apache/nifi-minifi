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

import org.apache.nifi.minifi.commons.schema.common.BaseSchema;
import org.apache.nifi.scheduling.SchedulingStrategy;
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RelationshipDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.MAX_CONCURRENT_TASKS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.NAME_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.PROCESSORS_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.SCHEDULING_PERIOD_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.SCHEDULING_STRATEGY_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.YIELD_PERIOD_KEY;

/**
 *
 */
public class ProcessorSchema extends BaseSchema {
    public static final String CLASS_KEY = "class";
    public static final String PENALIZATION_PERIOD_KEY = "penalization period";
    public static final String RUN_DURATION_NANOS_KEY = "run duration nanos";
    public static final String AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY = "auto-terminated relationships list";
    public static final String PROCESSOR_PROPS_KEY = "Properties";
    public static final int DEFAULT_MAX_CONCURRENT_TASKS = 1;
    public static final String DEFAULT_PENALIZATION_PERIOD = "30 sec";
    public static final String DEFAULT_YIELD_DURATION = "1 sec";
    public static final long DEFAULT_RUN_DURATION_NANOS = 0;
    public static final List<String> DEFAULT_AUTO_TERMINATED_RELATIONSHIPS_LIST = Collections.emptyList();
    public static final Map<String, Object> DEFAULT_PROPERTIES = Collections.emptyMap();
    public static final String IT_IS_NOT_A_VALID_SCHEDULING_STRATEGY = "it is not a valid scheduling strategy";

    private String name;
    private String processorClass;
    private String schedulingStrategy;
    private String schedulingPeriod;
    private Number maxConcurrentTasks = DEFAULT_MAX_CONCURRENT_TASKS;
    private String penalizationPeriod = DEFAULT_PENALIZATION_PERIOD;
    private String yieldPeriod = DEFAULT_YIELD_DURATION;
    private Number runDurationNanos = DEFAULT_RUN_DURATION_NANOS;
    private List<String> autoTerminatedRelationshipsList = DEFAULT_AUTO_TERMINATED_RELATIONSHIPS_LIST;
    private Map<String, Object> properties = DEFAULT_PROPERTIES;

    public ProcessorSchema(ProcessorDTO processorDTO) {
        ProcessorConfigDTO processorDTOConfig = processorDTO.getConfig();

        this.name = getAndValidateNotNull(processorDTO::getName, NAME_KEY, PROCESSORS_KEY);
        this.processorClass = getAndValidateNotNull(processorDTO::getType, CLASS_KEY, PROCESSORS_KEY);
        this.schedulingStrategy = getAndValidateNotNull(processorDTOConfig::getSchedulingStrategy, SCHEDULING_STRATEGY_KEY, PROCESSORS_KEY);
        if (schedulingStrategy != null && !isSchedulingStrategy(schedulingStrategy)) {
            addValidationIssue(SCHEDULING_STRATEGY_KEY, PROCESSORS_KEY, IT_IS_NOT_A_VALID_SCHEDULING_STRATEGY);
        }
        this.schedulingPeriod = getAndValidateNotNull(processorDTOConfig::getSchedulingPeriod, SCHEDULING_PERIOD_KEY, PROCESSORS_KEY);

        this.maxConcurrentTasks = Optional.ofNullable(processorDTOConfig.getConcurrentlySchedulableTaskCount()).orElse(DEFAULT_MAX_CONCURRENT_TASKS);
        this.penalizationPeriod = Optional.ofNullable(processorDTOConfig.getPenaltyDuration()).orElse(DEFAULT_PENALIZATION_PERIOD);
        this.yieldPeriod = Optional.ofNullable(processorDTOConfig.getYieldDuration()).orElse(DEFAULT_YIELD_DURATION);
        Long runDurationMillis = processorDTOConfig.getRunDurationMillis();
        this.runDurationNanos = runDurationMillis != null ? runDurationMillis * 1000 : DEFAULT_RUN_DURATION_NANOS;
        this.autoTerminatedRelationshipsList = nullToEmpty(processorDTO.getRelationships()).stream()
                .filter(RelationshipDTO::isAutoTerminate)
                .map(RelationshipDTO::getName)
                .collect(Collectors.toList());
        this.properties = new HashMap<>(nullToEmpty(processorDTOConfig.getProperties()));
    }

    public ProcessorSchema(Map map) {
        name = getRequiredKeyAsType(map, NAME_KEY, String.class, PROCESSORS_KEY);
        processorClass = getRequiredKeyAsType(map, CLASS_KEY, String.class, PROCESSORS_KEY);
        schedulingStrategy = getRequiredKeyAsType(map, SCHEDULING_STRATEGY_KEY, String.class, PROCESSORS_KEY);
        if (schedulingStrategy != null && !isSchedulingStrategy(schedulingStrategy)) {
            addValidationIssue(SCHEDULING_STRATEGY_KEY, PROCESSORS_KEY, IT_IS_NOT_A_VALID_SCHEDULING_STRATEGY);
        }
        schedulingPeriod = getRequiredKeyAsType(map, SCHEDULING_PERIOD_KEY, String.class, PROCESSORS_KEY);

        maxConcurrentTasks = getOptionalKeyAsType(map, MAX_CONCURRENT_TASKS_KEY, Number.class, PROCESSORS_KEY, DEFAULT_MAX_CONCURRENT_TASKS);
        penalizationPeriod = getOptionalKeyAsType(map, PENALIZATION_PERIOD_KEY, String.class, PROCESSORS_KEY, DEFAULT_PENALIZATION_PERIOD);
        yieldPeriod = getOptionalKeyAsType(map, YIELD_PERIOD_KEY, String.class, PROCESSORS_KEY, DEFAULT_YIELD_DURATION);
        runDurationNanos = getOptionalKeyAsType(map, RUN_DURATION_NANOS_KEY, Number.class, PROCESSORS_KEY, DEFAULT_RUN_DURATION_NANOS);
        autoTerminatedRelationshipsList = getOptionalKeyAsType(map, AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY, List.class, PROCESSORS_KEY, DEFAULT_AUTO_TERMINATED_RELATIONSHIPS_LIST);
        properties = getOptionalKeyAsType(map, PROCESSOR_PROPS_KEY, Map.class, PROCESSORS_KEY, DEFAULT_PROPERTIES);
    }

    private static boolean isSchedulingStrategy(String string) {
        try {
            SchedulingStrategy.valueOf(string);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = mapSupplier.get();
        result.put(NAME_KEY, name);
        result.put(CLASS_KEY, processorClass);
        result.put(MAX_CONCURRENT_TASKS_KEY, maxConcurrentTasks);
        result.put(SCHEDULING_STRATEGY_KEY, schedulingStrategy);
        result.put(SCHEDULING_PERIOD_KEY, schedulingPeriod);
        result.put(PENALIZATION_PERIOD_KEY, penalizationPeriod);
        result.put(YIELD_PERIOD_KEY, yieldPeriod);
        result.put(RUN_DURATION_NANOS_KEY, runDurationNanos);
        result.put(AUTO_TERMINATED_RELATIONSHIPS_LIST_KEY, autoTerminatedRelationshipsList);
        result.put(PROCESSOR_PROPS_KEY, new TreeMap<>(properties));
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public Number getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    public String getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public String getSchedulingPeriod() {
        return schedulingPeriod;
    }

    public String getPenalizationPeriod() {
        return penalizationPeriod;
    }

    public String getYieldPeriod() {
        return yieldPeriod;
    }

    public Number getRunDurationNanos() {
        return runDurationNanos;
    }

    public List<String> getAutoTerminatedRelationshipsList() {
        return autoTerminatedRelationshipsList;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
