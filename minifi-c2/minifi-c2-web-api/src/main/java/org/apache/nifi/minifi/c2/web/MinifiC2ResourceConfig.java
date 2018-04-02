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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.apache.nifi.minifi.c2.properties.C2Properties;
import org.apache.nifi.minifi.c2.web.api.AgentClassResource;
import org.apache.nifi.minifi.c2.web.api.AgentManifestResource;
import org.apache.nifi.minifi.c2.web.api.C2ProtocolResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * This is the main Jersey configuration for the MiNiFi C2 REST API web application.
 */
@Configuration
public class MinifiC2ResourceConfig extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinifiC2ResourceConfig.class);

    @Value("application.version")
    String restApiVersion;

    public MinifiC2ResourceConfig(@Context ServletContext servletContext, @Autowired C2Properties properties) {
        // register filters
        register(HttpMethodOverrideFilter.class);

        // register the exception mappers & jackson object mapper resolver
        packages("org.apache.nifi.minifi.c2.web.mapper");

        // register endpoints
        Class[] resources = {
                AgentClassResource.class,
                AgentManifestResource.class,
                C2ProtocolResource.class
        };
        for (Class resourceClass : resources) {
            logger.info("Registering {}", resourceClass.getName());
            register(resourceClass);
        }

        // include bean validation errors in response
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        // This is necessary for Kerberos auth via SPNEGO to work correctly when responding
        // "401 Unauthorized" with a "WWW-Authenticate: Negotiate" header value.
        // If this value needs to be changed, Kerberos authentication needs to move to filter chain
        // so it can directly set the HttpServletResponse instead of indirectly through a JAX-RS Response
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);

        // configure jersey to ignore resource paths for actuator and swagger-ui
        property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/(actuator|swagger/).*");

        // configure swagger and register swagger endpoints
        configureSwagger(properties);
    }

    private void configureSwagger(C2Properties properties) {
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);

        BeanConfig swaggerConfig = new BeanConfig();
        swaggerConfig.setConfigId("minifi-c2-swagger-config");
        swaggerConfig.setResourcePackage("org.apache.nifi.minifi.c2");  // the base pkgs to scan for swagger annotated resources. TODO add actuator to swagger docs?
        swaggerConfig.setScan(true);
        swaggerConfig.setTitle("MiNiFi C2 Server");
        swaggerConfig.setDescription("A command and control server for MiNiFi agents");
        swaggerConfig.setContact("dev@nifi.apache.org");
        swaggerConfig.setLicense("Apache Software License 2.0");
        swaggerConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0");

        String[] schemes = properties.isTlsEnabled() ? new String[]{"https"} : new String[]{"http"};
        swaggerConfig.setSchemes(schemes);

        String host = properties.getHost() != null ? properties.getHost() : "localhost";
        if (properties.getPort() != null) {
            host += String.format(":%d", properties.getPort());
        }
        swaggerConfig.setHost(host);
        swaggerConfig.setBasePath("/minifi-c2-api");

        // SwaggerConfigLocator.getInstance().putConfig(SwaggerContextService.CONFIG_ID_DEFAULT, swaggerConfig);
    }

}