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
package org.apache.nifi.minifi.c2.core.service.flow.mapping;

import org.apache.nifi.minifi.c2.model.FlowUri;

import java.util.Optional;

/**
 * Provides mappings from an agent class to the URI of a versioned flow.
 */
public interface FlowMapper {

    /**
     * Gets the flow mapping information for the given agent class.
     *
     * @param agentClassName the name of an agent class
     * @return the flow mapping information for the given agent class
     * @throws FlowMapperException if an error occurs getting the mapping
     */
    Optional<FlowUri> getFlowMapping(String agentClassName) throws FlowMapperException;

}
