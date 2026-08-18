package com.integritylog.web.dto;

import com.integritylog.domain.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        Long sequenceNumber,
        String eventType,
        String actorId,
        String resourceType,
        String resourceId,
        String payload,
        String contentHash,
        String previousHash,
        String recordHash,
        Instant createdAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getSequenceNumber(),
                event.getEventType(),
                event.getActorId(),
                event.getResourceType(),
                event.getResourceId(),
                event.getPayload(),
                event.getContentHash(),
                event.getPreviousHash(),
                event.getRecordHash(),
                event.getCreatedAt()
        );
    }
}
