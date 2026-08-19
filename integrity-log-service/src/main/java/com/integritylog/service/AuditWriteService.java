package com.integritylog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuditWriteService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AuditEventRepository repository;
    private final HashChainService hashChainService;

    public AuditWriteService(AuditEventRepository repository, HashChainService hashChainService) {
        this.repository = repository;
        this.hashChainService = hashChainService;
    }

    @Transactional
    public AuditEvent append(CreateAuditEventRequest request) {
        String previousHash = repository.findTopByOrderBySequenceNumberDesc()
                .map(AuditEvent::getRecordHash)
                .orElse(hashChainService.genesisHash());

        String payloadJson = normalizePayload(request.payload());
        String contentHash = hashChainService.computeContentHash(
                request.eventType(),
                request.actorId(),
                request.resourceType(),
                request.resourceId(),
                payloadJson
        );
        String recordHash = hashChainService.computeRecordHash(previousHash, contentHash);

        AuditEvent event = new AuditEvent(
                request.eventType(),
                request.actorId(),
                request.resourceType(),
                request.resourceId(),
                payloadJson,
                contentHash,
                previousHash,
                recordHash
        );

        AuditEvent saved = repository.saveAndFlush(event);
        return repository.findById(saved.getId()).orElse(saved);
    }

    private String normalizePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payload", e);
        }
    }
}

