/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.minifi.c2.model.extension;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApiModel
public class Processor extends ExtensionComponent {

    private Map<String, PropertyDescriptor> propertyDescriptors;
    private Boolean supportsDynamicProperties;
    private String inputRequirement;

    // TODO figure out the best way to include metadata for scheduling variants
    // private Boolean supportsEventDriven;
    // private Boolean supportsBatching;
    private List<String> supportedSchedulingStrategies;
    private String defaultSchedulingStrategy;
    private Map<String, Map<String, String>> defaultValuesBySchedulingStrategy;


    @ApiModelProperty("Descriptions of configuration properties applicable to this reporting task")
    public Map<String, PropertyDescriptor> getPropertyDescriptors() {
        return (propertyDescriptors != null ? Collections.unmodifiableMap(propertyDescriptors) : null);
    }

    public void setPropertyDescriptors(Map<String, PropertyDescriptor> propertyDescriptors) {
        this.propertyDescriptors = propertyDescriptors;
    }

    @ApiModelProperty("Whether or not this processor makes use of dynamic (user-set) properties")
    public Boolean getSupportsDynamicProperties() {
        return supportsDynamicProperties;
    }

    public void setSupportsDynamicProperties(Boolean supportsDynamicProperties) {
        this.supportsDynamicProperties = supportsDynamicProperties;
    }

    @ApiModelProperty("Any input requirements this processor has") // TODO, what is the format of this string?
    public String getInputRequirement() {
        return inputRequirement;
    }

    public void setInputRequirement(String inputRequirement) {
        this.inputRequirement = inputRequirement;
    }

    @ApiModelProperty("A list of scheduling strategies supported by this processor")
    public List<String> getSupportedSchedulingStrategies() {
        return (supportedSchedulingStrategies != null ? Collections.unmodifiableList(supportedSchedulingStrategies) : null);
    }

    public void setSupportedSchedulingStrategies(List<String> supportedSchedulingStrategies) {
        this.supportedSchedulingStrategies = supportedSchedulingStrategies;
    }

    @ApiModelProperty
    public String getDefaultSchedulingStrategy() {
        return defaultSchedulingStrategy;
    }

    public void setDefaultSchedulingStrategy(String defaultSchedulingStrategy) {
        this.defaultSchedulingStrategy = defaultSchedulingStrategy;
    }

    @ApiModelProperty
    public Map<String, Map<String, String>> getDefaultValuesBySchedulingStrategy() {
        return (defaultValuesBySchedulingStrategy != null ? Collections.unmodifiableMap(defaultValuesBySchedulingStrategy) : null);
    }

    public void setDefaultValuesBySchedulingStrategy(Map<String, Map<String, String>> defaultValuesBySchedulingStrategy) {
        this.defaultValuesBySchedulingStrategy = defaultValuesBySchedulingStrategy;
    }
}
