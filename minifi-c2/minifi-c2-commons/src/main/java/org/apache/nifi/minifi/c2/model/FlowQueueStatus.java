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
public class FlowQueueStatus {

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long size;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long sizeMax;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long dataSize;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long dataSizeMax;

    @ApiModelProperty(value = "The number of flow files in the queue", allowableValues = "range[0, 9223372036854775807]")
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @ApiModelProperty(value = "The maximum number of flow files that the queue is configured to hold", allowableValues = "range[0, 9223372036854775807]")
    public Long getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(Long sizeMax) {
        this.sizeMax = sizeMax;
    }

    @ApiModelProperty(value = "The size (in Bytes) of all flow files in the queue", allowableValues = "range[0, 9223372036854775807]")
    public Long getDataSize() {
        return dataSize;
    }

    public void setDataSize(Long dataSize) {
        this.dataSize = dataSize;
    }

    @ApiModelProperty(value = "The maximum size (in Bytes) that the queue is configured to hold", allowableValues = "range[0, 9223372036854775807]")
    public Long getDataSizeMax() {
        return dataSizeMax;
    }

    public void setDataSizeMax(Long dataSizeMax) {
        this.dataSizeMax = dataSizeMax;
    }

}
