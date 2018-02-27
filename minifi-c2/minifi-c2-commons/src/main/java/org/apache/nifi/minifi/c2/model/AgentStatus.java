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
 * Status of the aspects of the agent, including any agent components that are controllable by the C2 server, ie:
 *   - Repositories that can be cleared and their current state
 */
@ApiModel
public class AgentStatus {

    private long uptime;
    private Map<String, AgentRepositoryStatus> repositories;

    @ApiModelProperty("The number of milliseconds since the agent started.")
    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    @ApiModelProperty("Status and metrics for each repository")
    public Map<String, AgentRepositoryStatus> getRepositories() {
        return repositories;
    }

    public void setRepositories(Map<String, AgentRepositoryStatus> repositories) {
        this.repositories = repositories;
    }

}
