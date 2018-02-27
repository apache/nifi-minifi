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
package org.apache.nifi.minifi.c2.util;

import java.util.regex.Pattern;

/**
 * Holder to pass around the key, pattern, and replacement from an identity mapping
 */
public class IdentityMapping {

    private final String key;
    private final Pattern pattern;
    private final String replacementValue;

    public IdentityMapping(String key, Pattern pattern, String replacementValue) {
        this.key = key;
        this.pattern = pattern;
        this.replacementValue = replacementValue;
    }

    public String getKey() {
        return key;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacementValue() {
        return replacementValue;
    }

}
