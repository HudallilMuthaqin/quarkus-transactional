package org.testing.transactional.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testing.transactional.dto.UserTransactionDTO;
import org.testing.transactional.service.UserService;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "Operations for managing users with comprehensive transaction examples")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Inject
    UserService userService;

    @GET
    @Path("/{userId}")
    public Response getUserTransaction(@PathParam("userId") Long userId) {
        UserTransactionDTO dto = userService.getUserTransaction(userId);
        return Response.ok(dto).build();
    }

}
