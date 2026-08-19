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
@Table(name = "audit_event_originals")
public class AuditEventOriginal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "encrypted_payload", nullable = false)
    private byte[] encryptedPayload;

    @Column(name = "original_content_hash", length = 64)
    private String originalContentHash;

    @Column(name = "key_id")
    private String keyId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "redacted_by")
    private String redactedBy;

    protected AuditEventOriginal() {}

    public AuditEventOriginal(UUID eventId, byte[] encryptedPayload, String originalContentHash, String keyId, String redactedBy) {
        this.eventId = eventId;
        this.encryptedPayload = encryptedPayload;
        this.originalContentHash = originalContentHash;
        this.keyId = keyId;
        this.redactedBy = redactedBy;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public byte[] getEncryptedPayload() { return encryptedPayload; }
    public String getOriginalContentHash() { return originalContentHash; }
    public String getKeyId() { return keyId; }
    public Instant getCreatedAt() { return createdAt; }
    public String getRedactedBy() { return redactedBy; }
}
