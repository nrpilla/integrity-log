package com.integritylog.repository;

import com.integritylog.domain.ClientAccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ClientAccessAuditRepository extends JpaRepository<ClientAccessAudit, UUID>,
        JpaSpecificationExecutor<ClientAccessAudit> {
}
