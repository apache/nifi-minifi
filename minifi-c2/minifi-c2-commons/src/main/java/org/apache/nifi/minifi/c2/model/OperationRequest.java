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
import javax.validation.constraints.NotNull;

@ApiModel
public class OperationRequest {

    @NotNull
    private C2Operation operation;

    @NotBlank
    private String targetAgentIdentifier;

    private OperationState state = OperationState.NEW;
    private String operatorIdentity;
    private String operatorName;

    @ApiModelProperty("The C2 operation to perform")
    public C2Operation getOperation() {
        return operation;
    }

    public void setOperation(C2Operation operation) {
        this.operation = operation;
    }

    @ApiModelProperty("The identifier of the agent to which the operation applies")
    public String getTargetAgentIdentifier() {
        return targetAgentIdentifier;
    }

    public void setTargetAgentIdentifier(String targetAgentIdentifier) {
        this.targetAgentIdentifier = targetAgentIdentifier;
    }

    @ApiModelProperty(
            value = "The current state of the operation",
            readOnly = true)
    public OperationState getState() {
        return state;
    }

    public void setState(OperationState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "The verified identity of the C2 server client that created the operation",
            readOnly = true,
            notes = "This field is set by the server when an operation request is submitted to identify the origin. " +
                    "When the C2 instance is secured, this is the client principal identity (e.g., certificate DN). " +
                    "When the C2 instances is unsecured, this will be 'anonymous' as client identity can not be authenticated.")
    public String getOperatorIdentity() {
        return operatorIdentity;
    }

    public void setOperatorIdentity(String operatorIdentity) {
        this.operatorIdentity = operatorIdentity;
    }

    @ApiModelProperty(value = "The client-specified name of the user that created the operation",
            readOnly = true,
            notes = "A convenience field that the client/user can set to identify operation requests they submitted. " +
                    "This field cannot be trusted as the client can specify whatever they want (i.e., impersonate another user), " +
                    "but can be used when the operatorIdentity is ambiguous, such as when running an unsecured C2 server or " +
                    "when a client certificate is shared by multiple clients. (Note, sharing client certs is not a recommended best practice.)")
    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
