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

package org.apache.nifi.minifi.c2.security.authorization;

import org.apache.nifi.minifi.c2.api.security.authorization.AuthorityGranter;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrincipalStringAuthorityGranter implements AuthorityGranter {
    private final Map<String, List<String>> grantedAuthorityMap;

    public PrincipalStringAuthorityGranter(Resource configYaml) throws IOException {
        try (InputStream inputStream = configYaml.getInputStream()) {
            Object yaml = new Yaml().load(inputStream);
            if (!(yaml instanceof Map)) {
                throw new IllegalArgumentException("Expected authority map of Principal -> Authority list");
            }
            grantedAuthorityMap = (Map<String, List<String>>) yaml;
        }
    }
    @Override
    public Collection<GrantedAuthority> grantAuthorities(Authentication authentication) {
        List<String> authorities = grantedAuthorityMap.get(authentication.getPrincipal().toString());
        if (authorities == null) {
            return null;
        }
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
