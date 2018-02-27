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

import java.util.List;
import java.util.Set;

/**
 * A component provided by an extension bundle
 */
@ApiModel
public class ExtensionComponent extends DefinedType {

    // TODO, does arch/binary/compiler metadata need to be added here?

    private List<DefinedType> providedApiImplementations;

    private String description;
    private Set<String> tags;

    private Boolean deprecated;
    private String deprecationReason;

    @ApiModelProperty("If this type represents a provider for an interface, this lists the APIs it implements")
    public List<DefinedType> getProvidedApiImplementations() {
        return providedApiImplementations;
    }

    public void setProvidedApiImplementations(List<DefinedType> providedApiImplementations) {
        this.providedApiImplementations = providedApiImplementations;
    }

    @ApiModelProperty("A description of the component")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty("The tags associated with this type")
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @ApiModelProperty("Whether or not the component has been deprecated")
    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    @ApiModelProperty("If this component has been deprecated, this optional field can be used to provide an explanation")
    public String getDeprecationReason() {
        return deprecationReason;
    }

    public void setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
    }


}
