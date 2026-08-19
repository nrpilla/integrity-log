package com.integritylog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.ClientAccessAudit;
import com.integritylog.repository.ClientAccessAuditRepository;
import com.integritylog.repository.ClientAccessAuditSpecifications;
import com.integritylog.web.dto.ClientAccessAuditRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ClientAccessAuditService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ClientAccessAuditRepository repository;

    public ClientAccessAuditService(ClientAccessAuditRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClientAccessAudit recordAccess(ClientAccessAuditRequest request) {
        String details = serializeDetails(request.details());
        ClientAccessAudit audit = new ClientAccessAudit(
                request.actorId(),
                request.resourceType(),
                request.resourceId(),
                request.action(),
                request.decision(),
                request.purpose(),
                request.correlationId(),
                request.ipAddress(),
                details
        );
        return repository.saveAndFlush(audit);
    }

    public List<ClientAccessAudit> query(String actorId,
                                        String resourceType,
                                        String resourceId,
                                        String action,
                                        Instant from,
                                        Instant to) {
        return repository.findAll(ClientAccessAuditSpecifications.matches(actorId, resourceType, resourceId, action, from, to));
    }

    private String serializeDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize access audit details", e);
        }
    }
}
