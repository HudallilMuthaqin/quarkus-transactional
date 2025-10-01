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
import org.testing.transactional.dto.TransactionDTO;
import org.testing.transactional.service.TransactionService;

@Path("/api/transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transaction Management", description = "Operations for managing transaction with comprehensive transaction examples")
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    @Inject
    TransactionService transactionService;

    /**
     * Membuat transaction topup
     */
    @POST
    @Path("/topup")
    public Response createTopUp(TransactionDTO request) {
        LOGGER.info("API /topup called for cardNo: {}", request.getCardNo());
        TransactionDTO result = transactionService.crateTopup(request);
        ApiResponse<TransactionDTO> response = ApiResponse.success(
                result,
                "TOPUP created in new transaction successfully",
                "INSERT INTO TRANSACTION AND CARD (REQUIRES_NEW)"
        );
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Membuat transaction topup
     */
    @POST
    @Path("/purchase")
    public Response createPurchase(TransactionDTO request) {
        LOGGER.info("API /purchase called for cardNo: {}", request.getCardNo());
        TransactionDTO result = transactionService.cratePurchase(request);
        ApiResponse<TransactionDTO> response = ApiResponse.success(
                result,
                "PURCHASE created in new transaction successfully",
                "INSERT INTO TRANSACTION AND CARD (REQUIRES_NEW)"
        );
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Direct TopUp – langsung menambah saldo dan membuat transaksi SUCCESS
     */
    @POST
    @Path("/direct-topup")
    public Response createDirectTopUp(TransactionDTO request) {
        LOGGER.info("API Request: DirectTopUp with Card Number : {}", request.getCardNo());
        TransactionDTO result = transactionService.crateDirectTopup(request);
        ApiResponse<TransactionDTO> response = ApiResponse.success(
                result,
                "DIRECT TOPUP created in transaction successfully",
                "INSERT TABLE TRANSACTION AND CARD (REQUIRES_NEW)"
        );
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * Endpoint untuk meng-update saldo kartu berdasarkan
     * seluruh transaksi TOPUP yang masih PENDING
     * (status diubah menjadi SUCCESS).
     */
    @POST
    @Path("/update-balance")
    public Response updateBalance(TransactionDTO dto) {
        LOGGER.info("Request Update Balance for CardNo: {}", dto.getCardNo());
        TransactionDTO result = transactionService.crateUpdateBalance(dto);
        ApiResponse<TransactionDTO> response = ApiResponse.success(
                result,
                "Update Balance created in transaction successfully",
                "INSERT TABLE TRANSACTION AND CARD (REQUIRES_NEW)"
        );
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
