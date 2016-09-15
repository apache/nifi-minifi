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

package org.apache.nifi.minifi.commons.schema.common;

import java.util.Map;

import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.ID_KEY;
import static org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys.NAME_KEY;

public abstract class BaseSchemaWithIdAndName extends BaseSchema {
    private String id;
    private String name;

    public BaseSchemaWithIdAndName(Map map, String wrapperName) {
        id = getId(map, wrapperName);
        name = getName(map, wrapperName);
    }

    protected String getName(Map map, String wrapperName) {
        return getRequiredKeyAsType(map, NAME_KEY, String.class, wrapperName);
    }

    protected String getId(Map map, String wrapperName) {
        return getRequiredKeyAsType(map, ID_KEY, String.class, wrapperName);
    }

    protected void setId(String id) {
        this.id = id;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = mapSupplier.get();
        map.put(NAME_KEY, name);
        map.put(ID_KEY, id);
        return map;
    }
}
