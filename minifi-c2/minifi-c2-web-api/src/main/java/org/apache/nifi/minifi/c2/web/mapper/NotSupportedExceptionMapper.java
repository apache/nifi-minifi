/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.minifi.c2.web.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class NotSupportedExceptionMapper implements ExceptionMapper<NotSupportedException> {

    private static final Logger logger = LoggerFactory.getLogger(NotSupportedExceptionMapper.class);

    @Override
    public Response toResponse(NotSupportedException exception) {
        logger.info("{}. Returning {} response.", exception, Response.Status.NOT_IMPLEMENTED);
        logger.debug("", exception);

        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(exception.getMessage())
                .type("text/plain")
                .build();
    }

}
