package org.testing.transactional.dto;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String fullName;
    private String department;
    private Boolean active;
}