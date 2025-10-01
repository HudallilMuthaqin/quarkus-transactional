package org.testing.transactional.common;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PaginationRequest {

    @NotNull(message = "Page number cannot be null")
    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    private Integer page = 0;

    @NotNull(message = "Page size cannot be null")
    @Min(value = 1, message = "Page size must be greater than 0")
    private Integer size = 20;

    private String sortBy;
    private String sortDirection = "ASC";

    // Default constructor
    public PaginationRequest() {}

    // Constructor with parameters
    public PaginationRequest(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public PaginationRequest(Integer page, Integer size, String sortBy, String sortDirection) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    // Getters and setters
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // Helper methods
    public int getOffset() {
        return page * size;
    }

    public boolean hasSorting() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }

    public boolean isAscending() {
        return "ASC".equalsIgnoreCase(sortDirection);
    }

    @Override
    public String toString() {
        return String.format("PaginationRequest{page=%d, size=%d, sortBy='%s', sortDirection='%s'}",
                page, size, sortBy, sortDirection);
    }
}