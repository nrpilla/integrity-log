package com.integritylog.repository;

import com.integritylog.domain.AuditEventOriginal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuditEventOriginalRepository extends JpaRepository<AuditEventOriginal, UUID> {
    Optional<AuditEventOriginal> findTopByEventIdOrderByCreatedAtDesc(UUID eventId);
}
