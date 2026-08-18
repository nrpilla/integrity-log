package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.ResourceNotFoundException;
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

    public List<AuditEvent> query(String resourceType, String resourceId) {
        if (resourceType != null && resourceId != null) {
            return repository.findByResourceTypeAndResourceIdOrderBySequenceNumberAsc(
                    resourceType, resourceId);
        }
        return repository.findAllByOrderBySequenceNumberAsc();
    }
}
