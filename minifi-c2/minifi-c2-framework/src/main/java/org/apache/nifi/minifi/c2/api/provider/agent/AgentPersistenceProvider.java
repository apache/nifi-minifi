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
package org.apache.nifi.minifi.c2.api.provider.agent;

import org.apache.nifi.minifi.c2.api.provider.Provider;
import org.apache.nifi.minifi.c2.model.Agent;
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.apache.nifi.minifi.c2.model.AgentManifest;

import java.util.List;
import java.util.Optional;

/**
 * NOTE: Although this interface is intended to be an extension point, it is not yet considered stable and thus may
 * change across releases until the the C2 Server APIs mature.
 *
 * TODO, we may want to consider creating a separate entity model rather than reusing the REST API object model.
 * Currently, this design assumes the Provider implementation will do that translation.
 * This requires adding a dependency on minifi-c2-commons here for the data model.
 */
public interface AgentPersistenceProvider extends Provider {

    /**
     * Returns the number of persisted AgentClasses.
     *
     * @return the number of AgentClasses
     */
    long getAgentClassCount();

    /**
     * Saves a given AgentClass. Use the returned instance as the save operation might have side-effects.
     *
     * @param agentClass must not be null
     * @return the saved agentClass
     * @throws IllegalArgumentException if agentClass is null
     */
    AgentClass saveAgentClass(AgentClass agentClass);

    /**
     * Retrieves all saved AgentClasses.
     *
     * TODO: Change this interface to support pagination and sorting
     *
     * @return a List of all saved AgentClasses, or an empty List if there are no saved AgentClasses
     */
    List<AgentClass> getAgentClasses();

    /**
     * Check the existence of an AgentClass with a given name.
     *
     * @param name must not be null
     * @return true if a match is found, otherwise false.
     * @throws IllegalArgumentException if name is null
     */
    boolean agentClassExists(String name);

    /**
     * Retrieves an AgentClass by name.
     *
     * @param name must not be null
     * @return the AgentClass with the specified name (or empty optional)
     * @throws IllegalArgumentException if name is null
     */
    Optional<AgentClass> getAgentClass(String name);

    /**
     * Delete an AgentClass by name.
     *
     * @param name must not be null
     * @throws IllegalArgumentException if name is null
     */
    void deleteAgentClass(String name);


    long getAgentManifestCount();
    AgentManifest saveAgentManifest(AgentManifest agentManifest);
    List<AgentManifest> getAgentManifests();
    List<AgentManifest> getAgentManifestsByClass(String agentClassName);
    Optional<AgentManifest> getAgentManifest(String agentManifestId);
    void deleteAgentManifest(String agentManifestId);


    long getAgentCount();
    Agent saveAgent(Agent agent);
    List<Agent> getAgents();
    List<Agent> getAgentsByClassName(String agentClassName);
    Optional<Agent> getAgent(String agentId);
    void deleteAgent(String agentId);


}
