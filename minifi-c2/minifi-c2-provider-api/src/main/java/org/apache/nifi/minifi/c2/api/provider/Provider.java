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
package org.apache.nifi.minifi.c2.api.provider;

/**
 * Base interface for providers.
 */
public interface Provider {

    /**
     * Called prior to configuring the Provider in order to discover the prefix for the provider's properties.
     *
     * It is recommended that the prefix match the base package for the provider implementation
     * classes, in order to ensure uniqueness. For example, if your provider uses the package
     * com.mycompany.nifi-providers.example-provider, that package name can server as the
     *
     * If your properties prefix string collides with another configured provider at runtime,
     * the server will throw a runtime exception and fail to start.
     */
    // String getPropertiesPrefix();
    // ^^^ TODO, this is disabled as it was an experimental look at how to simplify extension provider configuration.
    // The idea is to combine their config into minifi-c2.properties and allow them to get their config my defining their prefix.
    // However I'm not sure it can work in all cases as it's not as powerful/flexible as JAXB.
    // For simple, standalone providers, such as persistence provider, it's probably sufficient,
    // but for more complex extensions, such as an Authorizers framework, it may be too limiting.

    /**
     * Called to configure the Provider.
     *
     * @param configurationContext the context containing configuration for the given provider
     * @throws ProviderCreationException if an error occurs while the provider is configured
     */
    void onConfigured(ProviderConfigurationContext configurationContext) throws ProviderCreationException;

}
