/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.nifi.minifi.commons.schema.v1;

import org.apache.nifi.minifi.commons.schema.ProcessorSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.ID_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.PROCESSORS_KEY;

public class ProcessorSchemaV1 extends ProcessorSchema {
    public ProcessorSchemaV1(Map map) {
        super(map);
    }

    @Override
    public String getId(Map map, String wrapperName) {
        return getOptionalKeyAsType(map, ID_KEY, String.class, wrapperName, "");
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public List<String> getValidationIssues() {
        List<String> validationIssues = new ArrayList<>(super.getValidationIssues());
        if (StringUtil.isNullOrEmpty(getId())) {
            validationIssues.add(getIssueText(ID_KEY, PROCESSORS_KEY, IT_WAS_NOT_FOUND_AND_IT_IS_REQUIRED));
        }
        return Collections.unmodifiableList(validationIssues);
    }
}
