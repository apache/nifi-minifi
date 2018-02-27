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
import org.apache.nifi.minifi.c2.model.AgentClass;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/agent-classes")
@Api(value = "Agent Classes", description = "Register and manage agent class definitions")
public class AgentClassResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Register a MiNiFi agent class with this C2 server",
            notes = "This can also be done with a heartbeat, which will register a MiNiFi agent class the first time it is seen in a heartbeat.",
            response = AgentClass.class
    )
    public Response createAgentClass(
            @ApiParam(value = "The class to create", required = true)
                AgentClass agentClass) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get all MiNiFi agent classes that are registered with this C2 server",
            response = AgentClass.class,
            responseContainer = "List"
    )
    public Response getAgentClasses() {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get a MiNiFi agent class that is registered with this C2 server",
            response = AgentClass.class
    )
    public Response getAgentClass(
            @PathParam("name")
            @ApiParam("The name of the class to retrieve")
                String name) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create or update a MiNiFi agent class",
            notes = "This can also be done with a heartbeat, which will register a MiNiFi agent the first time it heartbeats.",
            response = AgentClass.class
    )
    @Path("/{name}")
    public Response upsertAgentClass(
            @PathParam("name")
            @ApiParam(value = "The name of the class")
                String name,
            @ApiParam(value = "The metadata of the class to associate with the given name. If not specified, a class with a name and no other attributes will be set.")
                AgentClass agentClass) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update the metadata of a MiNiFi agent class",
            notes = "This can also be done with a heartbeat, which will register a MiNiFi agent the first time it heartbeats.",
            response = AgentClass.class
    )
    @Path("/{name}")
    public Response updateAgentClass(
            @PathParam("name")
            @ApiParam(value = "The name of the class to update")
                String name,
            @ApiParam(value = "A partial metadata object to update.", required = true)
                AgentClass agentClass) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }

    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Delete a MiNiFi agent class",
            response = AgentClass.class
    )
    public Response deleteAgentClass(
            @PathParam("name")
            @ApiParam("The name of the class to delete")
                String name) {
        throw new NotSupportedException("This method is not yet implemented for this resource.");
    }


}
