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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for generating and performing {@link IdentityMapping}s
 */
public class IdentityMappingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityMappingUtil.class);
    private static final Pattern backReferencePattern = Pattern.compile("\\$(\\d+)");

    /**
     * Given a properties object, generate a list of {@link IdentityMapping}s.
     *
     * The properties should be in the format:
     *
     * <pre>
     * identityMappingPatternPrefix.uniq-key-1=regexPatternToMatch1
     * identityMappingValuePrefix.uniq-key-1=identityValue1
     * identityMappingPatternPrefix.uniq-key-2=regexPatternToMatch2
     * identityMappingValuePrefix.uniq-key-2=identityValue2
     * ...
     * </pre>
     *
     * @param properties the properties file containing the mappings
     * @param identityMappingPatternPrefix the prefix of properties containing identity mapping regex patterns
     * @param identityMappingValuePrefix the prefix of properties containing identity mapping identity values
     * @return A list of {@link IdentityMapping}s extracted from the properties
     */
    public static List<IdentityMapping> getIdentityMappings(
            final Properties properties,
            final String identityMappingPatternPrefix,
            final String identityMappingValuePrefix) {
        final List<IdentityMapping> mappings = new ArrayList<>();

        // go through each property key
        for (String propertyName : properties.stringPropertyNames()) {
            if (StringUtils.startsWith(propertyName, identityMappingPatternPrefix)) {
                final String key = StringUtils.substringAfter(propertyName, identityMappingPatternPrefix);
                final String identityPattern = properties.getProperty(propertyName);

                if (StringUtils.isBlank(identityPattern)) {
                    LOGGER.warn("Identity Mapping property {} was found, but was empty", new Object[]{propertyName});
                    continue;
                }

                final String identityValueProperty = identityMappingValuePrefix + key;
                final String identityValue = properties.getProperty(identityValueProperty);

                if (StringUtils.isBlank(identityValue)) {
                    LOGGER.warn("Identity Mapping property {} was found, but corresponding value {} was not found",
                            new Object[]{propertyName, identityValueProperty});
                    continue;
                }

                final IdentityMapping identityMapping = new IdentityMapping(key, Pattern.compile(identityPattern), identityValue);
                mappings.add(identityMapping);

                LOGGER.debug("Found Identity Mapping with key = {}, pattern = {}, value = {}",
                        key, identityPattern, identityValue);
            }
        }

        // sort the list by the key so users can control the ordering in properties
        mappings.sort(Comparator.comparing(IdentityMapping::getKey));

        return mappings;
    }

    /**
     * Checks the given identity against each provided mapping and performs the mapping using the first one that matches.
     * If none match then the identity is returned as is.
     *
     * @param identity the identity to map
     * @param mappings the mappings
     * @return the mapped identity, or the same identity if no mappings matched
     */
    public static String mapIdentity(final String identity, List<IdentityMapping> mappings) {
        for (IdentityMapping mapping : mappings) {
            Matcher m = mapping.getPattern().matcher(identity);
            if (m.matches()) {
                final String pattern = mapping.getPattern().pattern();
                final String replacementValue = escapeLiteralBackReferences(mapping.getReplacementValue(), m.groupCount());
                return identity.replaceAll(pattern, replacementValue);
            }
        }

        return identity;
    }

    // If we find a back reference that is not valid, then we will treat it as a literal string. For example, if we have 3 capturing
    // groups and the Replacement Value has the value is "I owe $8 to him", then we want to treat the $8 as a literal "$8", rather
    // than attempting to use it as a back reference.
    private static String escapeLiteralBackReferences(final String unescaped, final int numCapturingGroups) {
        if (numCapturingGroups == 0) {
            return unescaped;
        }

        String value = unescaped;
        final Matcher backRefMatcher = backReferencePattern.matcher(value);
        while (backRefMatcher.find()) {
            final String backRefNum = backRefMatcher.group(1);
            if (backRefNum.startsWith("0")) {
                continue;
            }
            final int originalBackRefIndex = Integer.parseInt(backRefNum);
            int backRefIndex = originalBackRefIndex;

            // if we have a replacement value like $123, and we have less than 123 capturing groups, then
            // we want to truncate the 3 and use capturing group 12; if we have less than 12 capturing groups,
            // then we want to truncate the 2 and use capturing group 1; if we don't have a capturing group then
            // we want to truncate the 1 and get 0.
            while (backRefIndex > numCapturingGroups && backRefIndex >= 10) {
                backRefIndex /= 10;
            }

            if (backRefIndex > numCapturingGroups) {
                final StringBuilder sb = new StringBuilder(value.length() + 1);
                final int groupStart = backRefMatcher.start(1);

                sb.append(value.substring(0, groupStart - 1));
                sb.append("\\");
                sb.append(value.substring(groupStart - 1));
                value = sb.toString();
            }
        }

        return value;
    }

}
