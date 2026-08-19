package com.integritylog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "client_access_audit")
public class ClientAccessAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "decision", nullable = false, length = 50)
    private String decision = "ALLOW";

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "JSONB", nullable = false)
    private String details = "{}";

    @Column(name = "accessed_at", nullable = false, updatable = false)
    private Instant accessedAt = Instant.now();

    protected ClientAccessAudit() {
    }

    public ClientAccessAudit(String actorId,
                            String resourceType,
                            String resourceId,
                            String action,
                            String decision,
                            String purpose,
                            String correlationId,
                            String ipAddress,
                            String details) {
        this.actorId = actorId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.action = action;
        this.decision = decision == null || decision.isBlank() ? "ALLOW" : decision;
        this.purpose = purpose;
        this.correlationId = correlationId;
        this.ipAddress = ipAddress;
        this.details = details == null || details.isBlank() ? "{}" : details;
        this.accessedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getActorId() {
        return actorId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getAction() {
        return action;
    }

    public String getDecision() {
        return decision;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }
}
