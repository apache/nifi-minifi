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
package org.apache.nifi.minifi.c2.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@ApiModel("Uniform Resource Identifier for NiFi Versioned Flows saved to a NiFi Registry")
public class FlowUri {

    private String rawUriString;  // This is only used if the FlowUri(String) constructor is used. This field is never exposed other than indirectly by calling toString();

    private String registryUrl;
    private String bucketId;
    private String flowId;

    public FlowUri() {
    }

    public FlowUri(String flowUriString) {
        this.rawUriString = flowUriString;
        // TODO, parse out url, bucketId, flowId
        throw new UnsupportedOperationException("The FlowUri(String) constructor is not yet implemented");
    }

    public FlowUri(String registryUrl, String bucketId, String flowId) {
        this.registryUrl = registryUrl;
        this.bucketId = bucketId;
        this.flowId = flowId;
    }

    @ApiModelProperty(value = "The URL of the NiFi Registry storing the flow.",
            notes = "For example, 'https://registry.myorganization.org'")
    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @ApiModelProperty("The identifier of the bucket at the NiFi Registry that contains the flow")
    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    @ApiModelProperty("The identifier of the flow in the NiFi Registry bucket")
    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    @Override
    public String toString() {
        if (rawUriString != null) {
            return rawUriString;
        } else {
            return buildUriStringFromParts();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FlowUri flowUri = (FlowUri) o;

        return new EqualsBuilder()
                .append(registryUrl, flowUri.registryUrl)
                .append(bucketId, flowUri.bucketId)
                .append(flowId, flowUri.flowId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(registryUrl)
                .append(bucketId)
                .append(flowId)
                .toHashCode();
    }

    private String buildUriStringFromParts() {

        if (registryUrl == null && bucketId == null && flowId == null) {
            return "[empty_flow_uri]";
        }

        StringBuilder uriString = new StringBuilder();
        if (registryUrl != null) {

            if (registryUrl.endsWith("nifi-registry-api/")) {
                // Do nothing, this is the form we want
            } else if (registryUrl.endsWith("nifi-registry-api")) {
                uriString.append("/");
            } else if (registryUrl.endsWith("/")) {
                uriString.append("nifi-registry-api/");
            } else {
                uriString.append("/nifi-registry-api/");
            }
        } else {
            uriString.append("/");
        }

        if (bucketId != null) {
            uriString.append("buckets/");
            uriString.append(bucketId);
        }

        if (flowId != null) {
            uriString.append("flows/");
            uriString.append(flowId);
        }

        return uriString.toString();
    }
}
