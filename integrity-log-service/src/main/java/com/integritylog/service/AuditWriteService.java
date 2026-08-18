package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditWriteService {

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

        String payload = request.payload();
        String contentHash = hashChainService.computeContentHash(
                request.eventType(),
                request.actorId(),
                request.resourceType(),
                request.resourceId(),
                payload
        );
        String recordHash = hashChainService.computeRecordHash(previousHash, contentHash);

        AuditEvent event = new AuditEvent(
                request.eventType(),
                request.actorId(),
                request.resourceType(),
                request.resourceId(),
                payload,
                contentHash,
                previousHash,
                recordHash
        );

        return repository.save(event);
    }
}

