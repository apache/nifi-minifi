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
package org.apache.nifi.minifi.c2.core.provider.persistence;

import org.apache.nifi.minifi.c2.api.provider.device.DevicePersistenceProvider;
import org.apache.nifi.minifi.c2.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple, in-memory "persistence" provider in order to test the service layer.
 *
 * This is not designed for real use outside of development. For example:
 *   - it only keeps an in-memory record of saved entities, there is no real persistence
 *   - it does not support transactions
 *   - it does not clone objects on save/retrieval, so any modifications made after interacting with this service
 *     also modify the "persisted" copies.
 *
 * TODO, deep copy objects on save/get so that they cannot be modified outside this class without modifying the persisted copy.
 */
public class VolatileDevicePersistenceProvider implements DevicePersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(VolatileDevicePersistenceProvider.class);

    private Map<String, Device> devices = new ConcurrentHashMap<>();

    @Override
    public long getCount() {
        return devices.size();
    }

    @Override
    public Device save(Device device) {
        if (device == null || device.getIdentifier() == null) {
            throw new IllegalArgumentException("Device must be not null and have an id in order to be saved.");
        }
        devices.put(device.getIdentifier(), device);
        return device;
    }

    @Override
    public Iterable<Device> getAll() {
        return new ArrayList<>(devices.values());
    }

    @Override
    public boolean existsById(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device id cannot be null");
        }
        return devices.containsKey(deviceId);
    }

    @Override
    public Optional<Device> getById(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device id cannot be null");
        }
        return Optional.ofNullable(devices.get(deviceId));
    }

    @Override
    public void deleteById(String deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device id cannot be null");
        }
        devices.remove(deviceId);
    }

    @Override
    public void delete(Device device) {
        if (device == null || device.getIdentifier() == null) {
            throw new IllegalArgumentException("Device must be not null and have an id");
        }
        deleteById(device.getIdentifier());
    }

    @Override
    public void deleteAll() {
        devices.clear();
    }

}
