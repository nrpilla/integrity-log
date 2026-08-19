package com.integritylog.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.AuditEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        Long sequenceNumber,
        String eventType,
        String actorId,
        String resourceType,
        String resourceId,
        Object payload,
        String contentHash,
        String previousHash,
        String recordHash,
        Instant createdAt
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getSequenceNumber(),
                event.getEventType(),
                event.getActorId(),
                event.getResourceType(),
                event.getResourceId(),
                parsePayload(event.getPayload()),
                event.getContentHash(),
                event.getPreviousHash(),
                event.getRecordHash(),
                event.getCreatedAt()
        );
    }

    private static Object parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return null;
        }
        try {
            Object payload = OBJECT_MAPPER.readValue(rawPayload, Object.class);
            return payload instanceof Map<?, ?> || payload instanceof java.util.List<?> ? payload : rawPayload;
        } catch (JsonProcessingException e) {
            return rawPayload;
        }
    }
}
