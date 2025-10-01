package org.testing.quearkus.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testing.transactional.common.ApiResponse;
import org.testing.transactional.common.ResponseCode;
import org.testing.transactional.exeption.BusinessException;
import org.testing.transactional.exeption.ResourceNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global exception handler that ensures consistent error responses across the application.
 * Maps various types of exceptions to standardized API responses.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Throwable exception) {
        // 🔒 Log lengkap untuk investigasi internal, aman untuk production
        LOGGER.error("Unhandled exception caught", exception);

        if (exception instanceof BusinessException be) {
            return handleBusinessException(be);
        }
        if (exception instanceof ResourceNotFoundException rnfe) {
            return handleResourceNotFoundException(rnfe);
        }
        if (exception instanceof ConstraintViolationException cve) {
            return handleValidationException(cve);
        }
        if (exception instanceof OptimisticLockException ole) {
            return handleOptimisticLockException(ole);
        }
        if (exception instanceof RollbackException re) {
            return handleTransactionException(re);
        }
        if (exception instanceof SQLException se) {
            return handleSQLException(se);
        }
        if (exception instanceof IllegalArgumentException iae) {
            return handleIllegalArgumentException(iae);
        }
        if (exception instanceof RuntimeException re) {
            return handleRuntimeException(re);
        }

        return handleGenericException(exception);
    }

    private Response handleBusinessException(BusinessException ex) {
        Response.Status status = switch (ex.getErrorCode()) {
            case "VALIDATION_ERROR" -> Response.Status.BAD_REQUEST;
            case "NOT_FOUND"       -> Response.Status.NOT_FOUND;
            case "UNAUTHORIZED"    -> Response.Status.UNAUTHORIZED;
            case "FORBIDDEN"       -> Response.Status.FORBIDDEN;
            case "CONFLICT"        -> Response.Status.CONFLICT;
            default                -> Response.Status.BAD_REQUEST;
        };

        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ex.getErrorCode())
                .message(ex.getMessage())
                .build();

        return Response.status(status).entity(response).build();
    }

    private Response handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.NOT_FOUND.getCode())
                .message(ex.getMessage() != null ? ex.getMessage() : "Resource not found")
                .build();
        return Response.status(Response.Status.NOT_FOUND).entity(response).build();
    }

    private Response handleValidationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        // Ambil semua error agar client tahu field mana saja yang invalid
        List<String> errors = violations.stream()
                .map(v -> String.format("%s %s",
                        v.getPropertyPath().toString(),
                        v.getMessage()))
                .collect(Collectors.toList());

        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.VALIDATION_ERROR.getCode())
                .message("Validation failed")
                .errors(errors) // ✅ kirim list field yang gagal validasi
                .build();

        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private Response handleOptimisticLockException(OptimisticLockException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.CONCURRENCY_CONFLICT.getCode())
                .message("Data has been modified by another transaction. Please refresh and try again.")
                .build();
        return Response.status(Response.Status.CONFLICT).entity(response).build();
    }

    private Response handleTransactionException(RollbackException ex) {
        Throwable rootCause = getRootCause(ex);
        String message = "Transaction failed and rolled back";
        if (rootCause != null && rootCause != ex) {
            message += ": " + rootCause.getMessage();
        }

        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.TRANSACTION_FAILED.getCode())
                .message(message)
                .build();

        return Response.status(Response.Status.CONFLICT).entity(response).build();
    }

    private Response handleSQLException(SQLException ex) {
        String message = "Database error occurred";
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        String code = ResponseCode.DATABASE_ERROR.getCode();

        switch (ex.getErrorCode()) {
            case 1017 -> { message = "Database authentication failed"; status = Response.Status.UNAUTHORIZED; }
            case 1031 -> { message = "Insufficient database privileges"; status = Response.Status.FORBIDDEN; }
            case 1033 -> { message = "Database is unavailable"; status = Response.Status.SERVICE_UNAVAILABLE; }
            case 12505, 12514 -> { message = "Database connection issue"; status = Response.Status.SERVICE_UNAVAILABLE; }
            default -> message = "Database error: " + sanitizeDbMessage(ex.getMessage());
        }

        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(code)
                .message(message)
                .build();

        return Response.status(status).entity(response).build();
    }

    private Response handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.BAD_REQUEST.getCode())
                .message("Invalid request: " + ex.getMessage())
                .build();
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private Response handleRuntimeException(RuntimeException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.INTERNAL_SERVER_ERROR.getCode())
                .message("Unexpected error occurred. Please try again later.")
                .build();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }

    private Response handleGenericException(Throwable ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .responseCode(ResponseCode.INTERNAL_SERVER_ERROR.getCode())
                .message("Internal server error. Please contact support.")
                .build();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }

    // ==== Utility ====
    private Throwable getRootCause(Throwable ex) {
        Throwable cause = ex.getCause();
        while (cause != null && cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String sanitizeDbMessage(String msg) {
        // Jangan expose detail DB yang sensitif
        return (msg != null && msg.length() > 200) ? msg.substring(0, 200) + "..." : msg;
    }
}
