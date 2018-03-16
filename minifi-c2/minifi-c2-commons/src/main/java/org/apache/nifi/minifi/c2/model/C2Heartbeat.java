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

/**
 * TODO add Builder interface
 */
@ApiModel
public class C2Heartbeat {

    // Internal, not part of REST API
    private String identifier;
    private long timestamp;

    private DeviceInfo deviceInfo;
    private AgentInfo agentInfo;
    private FlowInfo flowInfo;

    @ApiModelProperty(hidden = true)
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @ApiModelProperty(hidden = true)
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @ApiModelProperty("Metadata for the device")
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @ApiModelProperty("Metadata for the agent installed on the device")
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }

    @ApiModelProperty("Metadata for the flow currently deployed to the agent")
    public FlowInfo getFlowInfo() {
        return flowInfo;
    }

    public void setFlowInfo(FlowInfo flowInfo) {
        this.flowInfo = flowInfo;
    }

    @Override
    public String toString() {
        return "C2Heartbeat{" +
                "identifier='" + identifier + '\'' +
                ", timestamp=" + timestamp +
                ", agent=" + (agentInfo != null ? agentInfo.getIdentifier() : "null") +
                ", device=" + (deviceInfo != null ? deviceInfo.getIdentifier() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        C2Heartbeat that = (C2Heartbeat) o;

        return new EqualsBuilder()
                .append(identifier, that.identifier)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(identifier)
                .toHashCode();
    }
}
