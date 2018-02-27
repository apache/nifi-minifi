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

import javax.validation.constraints.NotBlank;

@ApiModel
public class FlowInfo {

    @NotBlank
    private String flowId;
    private FlowUri versionedFlowSnapshotURI;
    private FlowStatus status;

    @ApiModelProperty(value = "A unique identifier of the flow currently deployed on the agent", required = true)
    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    @ApiModelProperty("The URI to the Versioned Flow Snapshot, when the flow corresponds to a Versioned Flow in a NiFi Registry.")
    public FlowUri getVersionedFlowSnapshotURI() {
        return versionedFlowSnapshotURI;
    }

    public void setVersionedFlowSnapshotURI(FlowUri versionedFlowSnapshotURI) {
        this.versionedFlowSnapshotURI = versionedFlowSnapshotURI;
    }

    @ApiModelProperty("Current flow status and metrics")
    public FlowStatus getStatus() {
        return status;
    }

    public void setStatus(FlowStatus status) {
        this.status = status;
    }
}
