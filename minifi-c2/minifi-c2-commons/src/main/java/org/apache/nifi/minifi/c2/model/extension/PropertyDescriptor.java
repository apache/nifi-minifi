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

@ApiModel
public class PropertyDescriptor {

    private String name;
    private String displayName;
    private String description;
    private List<PropertyAllowableValue> allowableValues;
    private String defaultValue;
    private Boolean required;
    private Boolean sensitive;
    private Boolean supportsEl;
    private DefinedType typeProvidedByValue;

    @ApiModelProperty(value = "The name of the property key", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty("The display name of the property key, if different from the name")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @ApiModelProperty("The description of what the property does")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty("A list of the allowable values for the property")
    public List<PropertyAllowableValue> getAllowableValues() {
        return (allowableValues != null ? Collections.unmodifiableList(allowableValues) : null);
    }

    public void setAllowableValues(List<PropertyAllowableValue> allowableValues) {
        this.allowableValues = allowableValues;
    }

    @ApiModelProperty("The default value if a user-set value is not specified")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @ApiModelProperty("Whether or not  the property is required for the component")
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    @ApiModelProperty("Whether or not  the value of the property is considered sensitive (e.g., passwords and keys)")
    public Boolean getSensitive() {
        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {
        this.sensitive = sensitive;
    }

    @ApiModelProperty("Whether or not the property supports using NiFi Expression Language in the value")
    public Boolean getSupportsEl() {
        return supportsEl;
    }

    public void setSupportsEl(Boolean supportsEl) {
        this.supportsEl = supportsEl;
    }

    @ApiModelProperty
    public DefinedType getTypeProvidedByValue() {
        return typeProvidedByValue;
    }

    public void setTypeProvidedByValue(DefinedType typeProvidedByValue) {
        this.typeProvidedByValue = typeProvidedByValue;
    }


}
