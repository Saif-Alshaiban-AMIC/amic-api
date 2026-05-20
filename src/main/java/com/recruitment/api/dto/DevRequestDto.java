package com.recruitment.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DevRequestDto(
    Long id,
    String requesterName,
    String requesterEmail,
    String department,
    String appName,
    String appType,
    String description,
    String priority,
    LocalDate targetDate,
    String status,
    String notes,
    LocalDateTime createdAt
) {}
