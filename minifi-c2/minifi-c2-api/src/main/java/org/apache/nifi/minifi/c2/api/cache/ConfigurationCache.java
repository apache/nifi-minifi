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

package org.apache.nifi.minifi.c2.api.cache;

import org.apache.nifi.minifi.c2.api.InvalidParameterException;

import java.util.List;
import java.util.Map;

/**
 * Cache for storing configurations so they don't have to be pulled from the provider more often than necessary
 */
public interface ConfigurationCache {
    /**
     * Returns the information on a given cache entry
     *
     * @param parameters the parameters that identify the entry
     * @return information on the entry
     * @throws InvalidParameterException if there are illegal/invalid parameters
     */
    ConfigurationCacheFileInfo getCacheFileInfo(String contentType, Map<String, List<String>> parameters) throws InvalidParameterException;
}
