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

package org.apache.nifi.minifi.bootstrap.configuration.mocks;

import org.apache.nifi.minifi.bootstrap.ConfigurationFileHolder;

import java.nio.ByteBuffer;

public class MockConfigurationFileHolder implements ConfigurationFileHolder {

    private volatile ByteBuffer configFile;

    public MockConfigurationFileHolder(ByteBuffer configFile) {
        this.configFile = configFile;
    }

    @Override
    public ByteBuffer getConfigFile() {
        return configFile;
    }

    public void setConfigFile(ByteBuffer configFile) {
        this.configFile = configFile;
    }

}
