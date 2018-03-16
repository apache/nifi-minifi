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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ApiModel
public class SystemInfo {

    private String machineArch;
    // TODO timezone / UTC offset? Keeping in mind it could change whereas other fields such as "machineArch" should not

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long physicalMem;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer vCores;

    @ApiModelProperty("Machine architecture of the device, e.g., ARM, x86")
    public String getMachineArch() {
        return machineArch;
    }

    public void setMachineArch(String machineArch) {
        this.machineArch = machineArch;
    }

    @ApiModelProperty(value = "Size of physical memory of the device in bytes", allowableValues = "range[0, 9223372036854775807]")
    public long getPhysicalMem() {
        return physicalMem;
    }

    public void setPhysicalMem(long physicalMem) {
        this.physicalMem = physicalMem;
    }

    @ApiModelProperty(
            value ="Number of virtual cores on the device",
            name = "vCores",
            allowableValues = "range[0, 2147483647]")
    public Integer getvCores() {
        return vCores;
    }

    public void setvCores(Integer vCores) {
        this.vCores = vCores;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "machineArch='" + machineArch + '\'' +
                ", physicalMem=" + physicalMem +
                ", vCores=" + vCores +
                '}';
    }
}
