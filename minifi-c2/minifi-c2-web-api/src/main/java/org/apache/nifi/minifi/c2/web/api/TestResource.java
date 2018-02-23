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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.apache.nifi.minifi.c2.core.service.C2Service;
import org.apache.nifi.minifi.c2.model.TestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/test")
@Api(
        value = "test",
        description = "An example/test resource.",
        authorizations = { @Authorization("Authorization") }
)
public class TestResource {

    C2Service c2Service;

    @Autowired
    public TestResource(final C2Service c2Service) {
        this.c2Service = c2Service;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a test object",
            response = TestObject.class
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400) })
    public Response createObject(
            @ApiParam(value = "The object to create", required = true)
            final TestObject testObject) {

        final TestObject createdTestObject = c2Service.createTestObject(testObject);
        return Response.status(Response.Status.CREATED).entity(createdTestObject).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets all test objects",
            response = TestObject.class,
            responseContainer = "List"
    )
    public Response getObjects() {
        final List<TestObject> objects = c2Service.getTestObjects();
        return Response.status(Response.Status.OK).entity(objects).build();
    }

    @GET
    @Path("{id}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a test object",
            response = TestObject.class
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404) })
    public Response getObject(
            @PathParam("id")
            @ApiParam("The test object identifier")
            final String id) {

        final TestObject testObject = c2Service.getTestObjectById(id);
        if (testObject == null) {
            throw new NotFoundException(String.format("A test object with identifier %s was not found.", id));
        }
        return Response.status(Response.Status.OK).entity(testObject).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a test object",
            response = TestObject.class
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404) })
    public Response updateObject(
            @PathParam("id")
            @ApiParam("The test object identifier")
            final String id,
            @ApiParam(value = "The updated test object", required = true)
            final TestObject object) {
        final TestObject updatedObject = c2Service.updateTestObject(object);
        return Response.status(Response.Status.OK).entity(updatedObject).build();
    }

    @DELETE
    @Path("{id}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a test object",
            response = TestObject.class
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404) })
    public Response deleteObject(
            @PathParam("id")
            @ApiParam(value = "The identifier of the object to delete", required = true)
            final String id) {
        final TestObject deleteTestObject = c2Service.deleteTestObject(id);
        if (deleteTestObject == null) {
            throw new NotFoundException(String.format("A test object with identifier %s was not found.", id));
        }
        return Response.status(Response.Status.OK).entity(deleteTestObject).build();
    }

}
