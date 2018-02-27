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
package org.apache.nifi.minifi.c2.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.nifi.minifi.c2.model.AgentManifest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/agent-manifests")
@Api(value = "Agent Manifests", description = "Register and manage agent manifest definitions")
public class AgentManifestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Upload an agent manifest",
            response = AgentManifest.class)
    public Response createAgentManifest(
            @QueryParam("class")
            @ApiParam("Optionally, a class label to associate with the manifest being uploaded")
                    String className,
            @ApiParam
                    AgentManifest agentManifest) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get all agent manifests",
            response = AgentManifest.class,
            responseContainer = "List")
            // TODO, pagination
    public Response getAgentManifests(
            @QueryParam("class")
            @ApiParam("Optionally, filter the results to match a class label")
                    String className) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(
            value = "Get the agent manifest specified by the id",
            response = AgentManifest.class)
    public Response getAgentManifest(
            @PathParam("id")
            @ApiParam
                    String id) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(
            value = "Delete the agent manifest specified by id",
            response = AgentManifest.class)
    public Response deleteAgentManifest(
            @PathParam("id")
            @ApiParam
                    String id) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

}
