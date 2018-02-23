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
package org.apache.nifi.minifi.c2.web.security;

import org.apache.nifi.minifi.c2.properties.C2Properties;
import org.apache.nifi.minifi.c2.util.IdentityMapping;
import org.apache.nifi.minifi.c2.util.IdentityMappingUtil;
import org.apache.nifi.minifi.c2.web.security.authentication.AnonymousIdentityFilter;
import org.apache.nifi.minifi.c2.web.security.authentication.IdentityAuthenticationProvider;
import org.apache.nifi.minifi.c2.web.security.authentication.IdentityFilter;
import org.apache.nifi.minifi.c2.web.security.authentication.exception.UntrustedProxyException;
import org.apache.nifi.minifi.c2.web.security.authentication.x509.X509IdentityAuthenticationProvider;
import org.apache.nifi.minifi.c2.web.security.authentication.x509.X509IdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * NiFi Registry Web Api Spring security
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MinifiC2SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MinifiC2SecurityConfig.class);

    @Autowired
    private C2Properties properties;

    private AnonymousIdentityFilter anonymousAuthenticationFilter = new AnonymousIdentityFilter();

    @Autowired
    private X509IdentityProvider x509IdentityProvider;
    private IdentityFilter x509AuthenticationFilter;
    private IdentityAuthenticationProvider x509AuthenticationProvider;

    public MinifiC2SecurityConfig() {
        super(true); // disable defaults
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        // allow any client to access the endpoint for logging in to generate an access token
        webSecurity.ignoring().antMatchers( "/access/token/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .rememberMe().disable()
                .authorizeRequests()
                    .anyRequest().fullyAuthenticated()
                    .and()
                .exceptionHandling()
                    .authenticationEntryPoint(http401AuthenticationEntryPoint())
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // x509
        http.addFilterBefore(x509AuthenticationFilter(), AnonymousAuthenticationFilter.class);

        if (!properties.isTlsEnabled()) {
            // If we are running unsecured add an
            // anonymous authentication filter that will populate the
            // authenticated, anonymous user if no other user identity
            // is detected earlier in the Spring filter chain.
            http.anonymous().authenticationFilter(anonymousAuthenticationFilter);
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(x509AuthenticationProvider());
    }

    private IdentityFilter x509AuthenticationFilter() throws Exception {
        if (x509AuthenticationFilter == null) {
            x509AuthenticationFilter = new IdentityFilter(x509IdentityProvider);
        }
        return x509AuthenticationFilter;
    }

    private IdentityAuthenticationProvider x509AuthenticationProvider() {
        if (x509AuthenticationProvider == null) {
            List<IdentityMapping> identityMappings = Collections.unmodifiableList(
                    IdentityMappingUtil.getIdentityMappings(
                            properties,
                            C2Properties.SECURITY_IDENTITY_MAPPING_PATTERN_PREFIX,
                            C2Properties.SECURITY_IDENTITY_MAPPING_PATTERN_PREFIX));
            x509AuthenticationProvider = new X509IdentityAuthenticationProvider(null, x509IdentityProvider, identityMappings);
        }
        return x509AuthenticationProvider;
    }


    private AuthenticationEntryPoint http401AuthenticationEntryPoint() {
        // This gets used for both secured and unsecured configurations. It will be called by Spring Security if a request makes it through the filter chain without being authenticated.
        // For unsecured, this should never be reached because the custom AnonymousAuthenticationFilter should always populate a fully-authenticated anonymous user
        // For secured, this will cause attempt to access any API endpoint (except those explicitly ignored) without providing credentials to return a 401 Unauthorized challenge
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException authenticationException)
                    throws IOException, ServletException {

                final int status;

                // See X509IdentityAuthenticationProvider.buildAuthenticatedToken(...)
                if (authenticationException instanceof UntrustedProxyException) {
                    // return a 403 response
                    status = HttpServletResponse.SC_FORBIDDEN;
                    logger.info("Identity in proxy chain not trusted to act as a proxy: {} Returning 403 response.", authenticationException.toString());

                } else {
                    // return a 401 response
                    status = HttpServletResponse.SC_UNAUTHORIZED;
                    logger.info("Client could not be authenticated due to: {} Returning 401 response.", authenticationException.toString());
                }

                logger.debug("", authenticationException);

                if (!response.isCommitted()) {
                    response.setStatus(status);
                    response.setContentType("text/plain");
                    response.getWriter().println(String.format("%s Contact the system administrator.", authenticationException.getLocalizedMessage()));
                }
            }
        };
    }

}
