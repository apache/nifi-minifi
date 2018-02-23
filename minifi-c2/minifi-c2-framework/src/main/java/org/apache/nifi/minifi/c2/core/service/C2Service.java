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
package org.apache.nifi.minifi.c2.core.service;

import org.apache.nifi.minifi.c2.core.persistence.C2Repository;
import org.apache.nifi.minifi.c2.model.TestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class C2Service {

    C2Repository c2Repository;

    @Autowired
    public C2Service(C2Repository c2Repository) {
        this.c2Repository = c2Repository;
    }

    public TestObject createTestObject(TestObject testObject) {
        return c2Repository.createTestObject(testObject);
    }

    public List<TestObject> getTestObjects() {
        List<TestObject> objects = new ArrayList<>();
        c2Repository.getTestObjects().forEachRemaining(objects::add);
        return objects;
    }

    public TestObject getTestObjectById(String identifier) {
        return c2Repository.getTestObjectById(identifier);
    }

    public TestObject updateTestObject(TestObject testObject) {
        return c2Repository.updateTestObject(testObject);
    }

    public TestObject deleteTestObject(String identifier) {
        return c2Repository.deleteTestObject(identifier);
    }

}
