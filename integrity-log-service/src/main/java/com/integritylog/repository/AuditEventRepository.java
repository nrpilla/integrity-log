package com.integritylog.repository;

import com.integritylog.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID>,
        JpaSpecificationExecutor<AuditEvent> {
    Optional<AuditEvent> findTopByOrderBySequenceNumberDesc();
    List<AuditEvent> findAllByOrderBySequenceNumberAsc();
}

