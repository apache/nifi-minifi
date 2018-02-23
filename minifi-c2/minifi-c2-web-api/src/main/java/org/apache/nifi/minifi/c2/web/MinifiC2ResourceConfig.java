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
package org.apache.nifi.minifi.c2.web;

import org.apache.nifi.minifi.c2.web.api.TestResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * This is the main Jersey configuration for the MiNiFi C2 REST API web application.
 */
@Configuration
public class MinifiC2ResourceConfig extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinifiC2ResourceConfig.class);

    public MinifiC2ResourceConfig(@Context ServletContext servletContext) {
        // register filters
        register(HttpMethodOverrideFilter.class);

        // register the exception mappers & jackson object mapper resolver
        packages("org.apache.nifi.minifi.c2.web.mapper");

        // register endpoints
        logger.info("Registering {}", TestResource.class.getName());
        register(TestResource.class);

        // include bean validation errors in response
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        // This is necessary for Kerberos auth via SPNEGO to work correctly when responding
        // "401 Unauthorized" with a "WWW-Authenticate: Negotiate" header value.
        // If this value needs to be changed, Kerberos authentication needs to move to filter chain
        // so it can directly set the HttpServletResponse instead of indirectly through a JAX-RS Response
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    }

}