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
package org.apache.nifi.minifi.c2.core.persistence;

import org.apache.nifi.minifi.c2.model.TestObject;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Repository
public class VolatileC2Repository implements C2Repository {

    private Map<String, TestObject> testObjectMap;

    public VolatileC2Repository() {
        this.testObjectMap = new HashMap<>();
    }

    @Override
    public TestObject createTestObject(final TestObject testObject) {

        final String id = UUID.randomUUID().toString();
        TestObject createdTestObject = new TestObject();
        createdTestObject.setIdentifier(id);
        createdTestObject.setName(testObject.getName());
        testObjectMap.put(id, createdTestObject);
        return createdTestObject;

    }

    @Override
    public Iterator<TestObject> getTestObjects() {
        return testObjectMap.values().iterator();
    }

    @Override
    public TestObject getTestObjectById(String identifier) {
        return testObjectMap.get(identifier);
    }

    @Override
    public TestObject updateTestObject(TestObject testObject) {
        TestObject updatedTestObject = testObjectMap.get(testObject.getIdentifier());
        updatedTestObject.setName(testObject.getName());
        return updatedTestObject;
    }

    @Override
    public TestObject deleteTestObject(String identifier) {
        return testObjectMap.remove(identifier);
    }
}
