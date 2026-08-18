package com.integritylog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuditEventRequest(
        @NotBlank @Size(max = 100) String eventType,
        @NotBlank @Size(max = 255) String actorId,
        @NotBlank @Size(max = 100) String resourceType,
        @NotBlank @Size(max = 255) String resourceId,
        String payload
) {
}
