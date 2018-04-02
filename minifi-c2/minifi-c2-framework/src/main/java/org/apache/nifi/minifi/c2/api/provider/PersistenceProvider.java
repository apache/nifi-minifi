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
package org.apache.nifi.minifi.c2.api.provider;

import java.util.Optional;

/**
 * A generic interface for an object persistence provider.
 *
 * This interface design is derived from org.springframework.data.repository.CrudRepository (ALv2).
 *
 * NOTE: Although this interface is intended to be an extension point, it is not yet considered stable and thus may
 * change across releases until the the C2 Server APIs mature.
 */
public interface PersistenceProvider<T, ID> {

    /**
     * Returns the number of saved entities.
     *
     * @return the number of saved entities
     */
    long getCount();

    /**
     * Saves a given entity. Use the returned instance as the save operation might have side-effects.
     *
     * @param t must not be null
     * @return the saved agentClass
     * @throws IllegalArgumentException if agentClass is null
     */
    T save(T t);

    /**
     * Retrieves all saved entities.
     *
     * TODO: Modify this interface to support pagination and sorting
     *
     * @return a List of all saved entities, or an empty List if there are no saved entities
     */
    Iterable<T> getAll();

    /**
     * Checks existence an entity by id.
     *
     * @param id must not be null
     * @return true if an entity with the given id exists; otherwise, false
     * @throws IllegalArgumentException if name is null
     */
    boolean existsById(ID id);

    /**
     * Retrieves an entity by id.
     *
     * @param id must not be null
     * @return the entity with the specified id (or empty optional)
     * @throws IllegalArgumentException if name is null
     */
    Optional<T> getById(ID id);


    /**
     * Delete an entity by id.
     *
     * @param id must not be null
     * @throws IllegalArgumentException if name is null
     */
    void deleteById(ID id);

    /**
     * Delete an entity.
     *
     * @param t must not be null
     * @throws IllegalArgumentException if name is null
     */
    void delete(T t);

    /**
     * Delete all entities.
     */
    void deleteAll();

}
