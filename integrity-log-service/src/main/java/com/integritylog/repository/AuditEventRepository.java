package com.integritylog.repository;

import com.integritylog.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    Optional<AuditEvent> findTopByOrderBySequenceNumberDesc();

    List<AuditEvent> findAllByOrderBySequenceNumberAsc();

    List<AuditEvent> findByResourceTypeAndResourceIdOrderBySequenceNumberAsc(
            String resourceType, String resourceId);
}

