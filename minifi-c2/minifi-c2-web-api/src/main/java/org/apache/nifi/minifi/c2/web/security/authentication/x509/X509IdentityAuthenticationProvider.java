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
package org.apache.nifi.minifi.c2.web.security.authentication.x509;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.minifi.c2.core.security.authorization.user.NiFiUser;
import org.apache.nifi.minifi.c2.core.security.authorization.user.NiFiUserDetails;
import org.apache.nifi.minifi.c2.core.security.authorization.user.StandardNiFiUser;
import org.apache.nifi.minifi.c2.util.IdentityMapping;
import org.apache.nifi.minifi.c2.web.security.authentication.AuthenticationRequestToken;
import org.apache.nifi.minifi.c2.web.security.authentication.AuthenticationSuccessToken;
import org.apache.nifi.minifi.c2.web.security.authentication.IdentityAuthenticationProvider;
import org.apache.nifi.registry.security.authentication.AuthenticationRequest;
import org.apache.nifi.registry.security.authentication.AuthenticationResponse;
import org.apache.nifi.registry.security.authentication.IdentityProvider;
import org.apache.nifi.registry.security.authorization.Authorizer;
import org.apache.nifi.registry.security.util.ProxiedEntitiesUtils;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class X509IdentityAuthenticationProvider extends IdentityAuthenticationProvider {

//    private static final Authorizable PROXY_AUTHORIZABLE = new Authorizable() {
//        @Override
//        public Authorizable getParentAuthorizable() {
//            return null;
//        }
//
//        @Override
//        public Resource getResource() {
//            return ResourceFactory.getProxyResource();
//        }
//    };

    public X509IdentityAuthenticationProvider(Authorizer authorizer, IdentityProvider identityProvider, List<IdentityMapping> identityMappings) {
        super(authorizer, identityProvider, identityMappings);
    }

    @Override
    protected AuthenticationSuccessToken buildAuthenticatedToken(
            AuthenticationRequestToken requestToken,
            AuthenticationResponse response) {

        AuthenticationRequest authenticationRequest = requestToken.getAuthenticationRequest();

        String proxiedEntitiesChain = authenticationRequest.getDetails() != null
                ? (String)authenticationRequest.getDetails()
                : null;

        if (StringUtils.isBlank(proxiedEntitiesChain)) {
            return super.buildAuthenticatedToken(requestToken, response);
        }

        // build the entire proxy chain if applicable - <end-user><proxy1><proxy2>
        final List<String> proxyChain = ProxiedEntitiesUtils.tokenizeProxiedEntitiesChain(proxiedEntitiesChain);
        proxyChain.add(response.getIdentity());

        // add the chain as appropriate to each proxy
        NiFiUser proxy = null;
        for (final ListIterator<String> chainIter = proxyChain.listIterator(proxyChain.size()); chainIter.hasPrevious(); ) {
            String identity = chainIter.previous();

            // determine if the user is anonymous
            final boolean isAnonymous = StringUtils.isBlank(identity);
            if (isAnonymous) {
                identity = StandardNiFiUser.ANONYMOUS_IDENTITY;
            } else {
                identity = mapIdentity(identity);
            }

            final Set<String> groups = getUserGroups(identity);

            // Only set the client address for client making the request because we don't know the clientAddress of the proxied entities
            String clientAddress = (proxy == null) ? requestToken.getClientAddress() : null;
            proxy = createUser(identity, groups, proxy, clientAddress, isAnonymous);

            // TODO, if we add an authorization framework, add a check here that the current user is authorized to act as a proxy
//            if (chainIter.hasPrevious()) {
//                try {
//                    PROXY_AUTHORIZABLE.authorize(authorizer, RequestAction.WRITE, proxy);
//                } catch (final AccessDeniedException e) {
//                    throw new UntrustedProxyException(String.format("Untrusted proxy [%s].", identity));
//                }
//            }
        }

        return new AuthenticationSuccessToken(new NiFiUserDetails(proxy));

    }

    /**
     * Returns a regular user populated with the provided values, or if the user should be anonymous, a well-formed instance of the anonymous user with the provided values.
     *
     * @param identity      the user's identity
     * @param chain         the proxied entities
     * @param clientAddress the requesting IP address
     * @param isAnonymous   if true, an anonymous user will be returned (identity will be ignored)
     * @return the populated user
     */
    private static NiFiUser createUser(String identity, Set<String> groups, NiFiUser chain, String clientAddress, boolean isAnonymous) {
        if (isAnonymous) {
            return StandardNiFiUser.populateAnonymousUser(chain, clientAddress);
        } else {
            return new StandardNiFiUser.Builder().identity(identity).groups(groups).chain(chain).clientAddress(clientAddress).build();
        }
    }



}
