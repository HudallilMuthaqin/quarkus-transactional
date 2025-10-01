package org.testing.transactional.controller;


import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testing.transactional.common.ApiResponse;
import org.testing.transactional.dto.CardDTO;
import org.testing.transactional.service.CardService;
import org.testing.transactional.service.TransactionalDemoService;

@Path("/api/cards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management", description = "Operations for managing users with comprehensive transaction examples")
public class CardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardController.class);

    @Inject
    CardService cardService;

    @Inject
    TransactionalDemoService transactionalDemoService;

    /**
     * Membuat card
     */
    @POST
    @Path("/create")
    public Response createCard(CardDTO dto) {
        CardDTO createdCard = cardService.createCard(dto);
        return Response.ok(createdCard).build();
    }
}
