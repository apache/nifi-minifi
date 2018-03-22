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

import java.util.Map;

/**
 * Status of the aspects of the flow that are controllable by the C2 server, ie:
 *   - Processors that can be started/stopped and their current state
 *   - Queues that can be cleared and their current state
 */
@ApiModel
public class FlowStatus {

    private Map<String, ComponentStatus> components;
    private Map<String, FlowQueueStatus> queues;

    @ApiModelProperty("Status and for each component that is part of the flow (e.g., processors)")
    public Map<String, ComponentStatus> getComponents() {
        return components;
    }

    public void setComponents(Map<String, ComponentStatus> components) {
        this.components = components;
    }

    @ApiModelProperty("Status and metrics for each flow connection queue")
    public Map<String, FlowQueueStatus> getQueues() {
        return queues;
    }

    public void setQueues(Map<String, FlowQueueStatus> queues) {
        this.queues = queues;
    }

}
