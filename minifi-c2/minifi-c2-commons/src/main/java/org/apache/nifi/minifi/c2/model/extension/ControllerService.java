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

import java.util.Map;

@ApiModel  // TODO Swagger docs description
public class ControllerService extends ExtensionComponent {

    private Map<String, PropertyDescriptor> propertyDescriptors;

    @ApiModelProperty("Descriptions of configuration properties applicable to this controller service")
    public Map<String, PropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }

    public void setPropertyDescriptors(Map<String, PropertyDescriptor> propertyDescriptors) {
        this.propertyDescriptors = propertyDescriptors;
    }

}
