package org.testing.transactional.common;

public enum ResponseCode {

    SUCCESS("200", "Success"),
    CREATED("201", "Created"),
    ACCEPTED("202", "Accepted"),
    NO_CONTENT("204", "No Content"),

    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not Found"),
    METHOD_NOT_ALLOWED("405", "Method Not Allowed"),
    CONFLICT("409", "Conflict"),
    VALIDATION_ERROR("422", "Validation Error"),

    INTERNAL_SERVER_ERROR("500", "Internal Server Error"),
    SERVICE_UNAVAILABLE("503", "Service Unavailable"),

    // Custom business logic codes
    TRANSACTION_FAILED("T001", "Transaction Failed"),
    CONCURRENCY_CONFLICT("T002", "Concurrency Conflict"),
    DATABASE_ERROR("T003", "Database Error"),

    ERROR("E001", "General Error");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + " - " + message;
    }
}

