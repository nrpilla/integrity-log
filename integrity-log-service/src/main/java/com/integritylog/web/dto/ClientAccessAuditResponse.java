package com.integritylog.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.ClientAccessAudit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ClientAccessAuditResponse(
        UUID id,
        String actorId,
        String resourceType,
        String resourceId,
        String action,
        String decision,
        String purpose,
        String correlationId,
        String ipAddress,
        Map<String, Object> details,
        Instant accessedAt
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ClientAccessAuditResponse from(ClientAccessAudit audit) {
        return new ClientAccessAuditResponse(
                audit.getId(),
                audit.getActorId(),
                audit.getResourceType(),
                audit.getResourceId(),
                audit.getAction(),
                audit.getDecision(),
                audit.getPurpose(),
                audit.getCorrelationId(),
                audit.getIpAddress(),
                parseDetails(audit.getDetails()),
                audit.getAccessedAt()
        );
    }

    private static Map<String, Object> parseDetails(String rawDetails) {
        if (rawDetails == null || rawDetails.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(rawDetails, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of("raw", rawDetails);
        }
    }
}
