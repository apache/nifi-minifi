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
package org.apache.nifi.minifi.c2.web.api;

class HttpStatusMessages {

    /* 4xx messages */
    static final String MESSAGE_400 = "MiNiFi C2 server was unable to complete the request because it was invalid. The request should not be retried without modification.";
    static final String MESSAGE_401 = "Client could not be authenticated.";
    static final String MESSAGE_403 = "Client is not authorized to make this request.";
    static final String MESSAGE_404 = "The specified resource could not be found.";
    static final String MESSAGE_409 = "MiNiFi C2 server was unable to complete the request because it assumes a server state that is not valid.";

    /* 5xx messages */
    static final String MESSAGE_500 = "MiNiFi C2 server was unable to complete the request because an unexpected error occurred.";
}
