/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.minifi.c2.api.provider.operations;

import org.apache.nifi.minifi.c2.api.provider.Provider;
import org.apache.nifi.minifi.c2.model.OperationRequest;

import java.util.List;
import java.util.Optional;

/**
 * Interface for a persistence provider for C2Operations.
 *
 * Note: This is an unstable interface that is expected to change.
 */
public interface OperationPersistenceProvider extends Provider {

    long getOperationCount();

    OperationRequest saveOperation(OperationRequest operationRequest);

    List<OperationRequest> getOperations();

    List<OperationRequest> getOperationsByAgent(String agentId);

    Optional<OperationRequest> getOperation(String operationId);

    void deleteOperation(String operationId);

}
