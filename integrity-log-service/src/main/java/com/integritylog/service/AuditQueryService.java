package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.repository.AuditEventSpecifications;
import com.integritylog.web.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditQueryService {

    private final AuditEventRepository repository;

    public AuditQueryService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public AuditEvent getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit event not found: " + id));
    }

    public Page<AuditEvent> query(AuditEventQuery query, Pageable pageable) {
        return repository.findAll(AuditEventSpecifications.matches(query), pageable);
    }
}
