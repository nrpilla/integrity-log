package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditArchiveService {

    private final AuditEventRepository repository;

    public AuditArchiveService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean archive(UUID id) {
        AuditEvent event = repository.findById(id).orElseThrow(() -> new com.integritylog.web.ResourceNotFoundException("AuditEvent not found: " + id));
        if (event.getArchivedAt() != null) {
            return false; // already archived
        }
        event.setArchivedAt(Instant.now());
        repository.saveAndFlush(event);
        return true; // newly archived
    }
}
