package com.integritylog.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ClientAccessAuditRequest(
        @NotBlank String actorId,
        @NotBlank String resourceType,
        @NotBlank String resourceId,
        @NotBlank String action,
        String decision,
        String purpose,
        String correlationId,
        String ipAddress,
        Map<String, Object> details
) {
    public ClientAccessAuditRequest {
        if (decision == null || decision.isBlank()) {
            decision = "ALLOW";
        }
        if (details == null) {
            details = Map.of();
        }
    }
}
