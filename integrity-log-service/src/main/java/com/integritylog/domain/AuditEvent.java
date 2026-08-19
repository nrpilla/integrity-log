package com.integritylog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sequence_number", insertable = false, updatable = false)
    private Long sequenceNumber;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "payload_digest", length = 64)
    private String payloadDigest;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(name = "record_hash", nullable = false, length = 64)
    private String recordHash;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "redacted", nullable = false)
    private boolean redacted = false;

    @Column(name = "redaction_proof_id")
    private java.util.UUID redactionProofId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected AuditEvent() {
    }

    public AuditEvent(String eventType, String actorId, String resourceType, String resourceId,
                      String payload, String contentHash, String previousHash, String recordHash) {
        this.eventType = eventType;
        this.actorId = actorId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.payload = payload;
        this.contentHash = contentHash;
        this.previousHash = previousHash;
        this.recordHash = recordHash;
    }

    public UUID getId() { return id; }
    public Long getSequenceNumber() { return sequenceNumber; }
    public String getEventType() { return eventType; }
    public String getActorId() { return actorId; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getPayload() { return payload; }
    public String getPayloadDigest() { return payloadDigest; }
    public String getContentHash() { return contentHash; }
    public String getPreviousHash() { return previousHash; }
    public String getRecordHash() { return recordHash; }
    public Instant getCreatedAt() { return createdAt; }

    public Instant getArchivedAt() { return archivedAt; }
    public boolean isRedacted() { return redacted; }
    public java.util.UUID getRedactionProofId() { return redactionProofId; }

    public void setPayload(String payload) { this.payload = payload; }
    public void setRedacted(boolean redacted) { this.redacted = redacted; }
    public void setRedactionProofId(java.util.UUID proofId) { this.redactionProofId = proofId; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }
}
