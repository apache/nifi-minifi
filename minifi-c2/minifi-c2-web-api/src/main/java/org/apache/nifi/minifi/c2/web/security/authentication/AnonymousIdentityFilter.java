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
package org.apache.nifi.minifi.c2.web.security.authentication;

import org.apache.nifi.minifi.c2.core.security.authorization.user.NiFiUserDetails;
import org.apache.nifi.minifi.c2.core.security.authorization.user.StandardNiFiUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;

public class AnonymousIdentityFilter extends AnonymousAuthenticationFilter {

    private static final String ANONYMOUS_KEY = "anonymousNifiKey";

    public AnonymousIdentityFilter() {
        super(ANONYMOUS_KEY);
    }

    @Override
    protected Authentication createAuthentication(HttpServletRequest request) {
        return new AuthenticationSuccessToken(new NiFiUserDetails(StandardNiFiUser.ANONYMOUS));
    }

}
