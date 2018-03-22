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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.nifi.minifi.c2.model.BuildInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A component provided by an extension bundle
 */
@ApiModel
public class ExtensionComponent extends DefinedType {

    private BuildInfo buildInfo;

    private List<DefinedType> providedApiImplementations;

    private String description;
    private Set<String> tags;

    private Boolean deprecated;
    private String deprecationReason;

    @ApiModelProperty("The build metadata for this component")
    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    @ApiModelProperty("If this type represents a provider for an interface, this lists the APIs it implements")
    public List<DefinedType> getProvidedApiImplementations() {
        return (providedApiImplementations != null ? Collections.unmodifiableList(providedApiImplementations) : null);

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
        return (tags != null ? Collections.unmodifiableSet(tags) : null);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ExtensionComponent that = (ExtensionComponent) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(buildInfo, that.buildInfo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(buildInfo)
                .toHashCode();
    }
}
