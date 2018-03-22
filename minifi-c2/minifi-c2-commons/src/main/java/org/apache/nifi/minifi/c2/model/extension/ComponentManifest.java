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
public class ComponentManifest {

    private List<DefinedType> apis;
    private List<ControllerService> controllerServices;
    private List<Processor> processors;
    private List<ReportingTask> reportingTasks;

    @ApiModelProperty("Public interfaces defined in this bundle")
    public List<DefinedType> getApis() {
        return (apis != null ? Collections.unmodifiableList(apis) : null);
    }

    public void setApis(List<DefinedType> apis) {
        this.apis = apis;
    }

    @ApiModelProperty("Controller Services provided in this bundle")
    public List<ControllerService> getControllerServices() {
        return (controllerServices != null ? Collections.unmodifiableList(controllerServices) : null);
    }

    public void setControllerServices(List<ControllerService> controllerServices) {
        this.controllerServices = controllerServices;
    }

    @ApiModelProperty("Processors provided in this bundle")
    public List<Processor> getProcessors() {
        return (processors != null ? Collections.unmodifiableList(processors) : null);
    }

    public void setProcessors(List<Processor> processors) {
        this.processors = processors;
    }

    @ApiModelProperty("Reporting Tasks provided in this bundle")
    public List<ReportingTask> getReportingTasks() {
        return (reportingTasks != null ? Collections.unmodifiableList(reportingTasks) : null);
    }

    public void setReportingTasks(List<ReportingTask> reportingTasks) {
        this.reportingTasks = reportingTasks;
    }
}
