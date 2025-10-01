package org.testing.transactional.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("data")
    private T data;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("query")
    private String query;

    @JsonProperty("totalElements")
    private Long totalElements;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("currentPage")
    private Integer currentPage;

    @JsonProperty("pageSize")
    private Integer pageSize;

    @JsonProperty("errors")
    private List<String> errors;

    // Private constructor to enforce builder pattern
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.SUCCESS.getCode())
                .data(data)
                .message("Operation completed successfully")
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.SUCCESS.getCode())
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message, String query) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.SUCCESS.getCode())
                .data(data)
                .message(message)
                .query(query)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.ERROR.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.ERROR.getCode())
                .message(message)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.NOT_FOUND.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.BAD_REQUEST.getCode())
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .responseCode(ResponseCode.UNAUTHORIZED.getCode())
                .message(message)
                .build();
    }

    // Builder class
    public static class Builder<T> {
        private final ApiResponse<T> response;

        private Builder() {
            response = new ApiResponse<>();
        }

        public Builder<T> responseCode(String responseCode) {
            response.responseCode = responseCode;
            return this;
        }

        public Builder<T> data(T data) {
            response.data = data;
            return this;
        }

        public Builder<T> message(String message) {
            response.message = message;
            return this;
        }

        public Builder<T> query(String query) {
            response.query = query;
            return this;
        }

        public Builder<T> totalElements(Long totalElements) {
            response.totalElements = totalElements;
            return this;
        }

        public Builder<T> totalPages(Integer totalPages) {
            response.totalPages = totalPages;
            return this;
        }

        public Builder<T> currentPage(Integer currentPage) {
            response.currentPage = currentPage;
            return this;
        }

        public Builder<T> pageSize(Integer pageSize) {
            response.pageSize = pageSize;
            return this;
        }

        public Builder<T> errors(List<String> errors) {
            response.errors = errors;
            return this;
        }

        public ApiResponse<T> build() {
            return response;
        }
    }

    // Getters
    public String getResponseCode() {
        return responseCode;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getQuery() {
        return query;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public List<String> getErrors() {
        return errors;
    }
}
