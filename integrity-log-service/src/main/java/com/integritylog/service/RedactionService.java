package com.integritylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.integritylog.domain.AuditEvent;
import com.integritylog.domain.AuditEventOriginal;
import com.integritylog.repository.AuditEventOriginalRepository;
import com.integritylog.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RedactionService {

    private final AuditEventRepository repository;
    private final AuditEventOriginalRepository originals;
    private final CryptoService cryptoService;
    private final HashChainService hashChainService;
    private final ObjectMapper mapper = new ObjectMapper();

    public RedactionService(AuditEventRepository repository,
                            AuditEventOriginalRepository originals,
                            CryptoService cryptoService,
                            HashChainService hashChainService) {
        this.repository = repository;
        this.originals = originals;
        this.cryptoService = cryptoService;
        this.hashChainService = hashChainService;
    }

    @Transactional
    public AuditEvent redact(UUID id, List<String> fields, String redactedBy) {
        AuditEvent event = repository.findById(id).orElseThrow();
        String currentPayload = event.getPayload() == null ? "{}" : event.getPayload();
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(currentPayload);

            // compute and store original content hash
            String originalContentHash = hashChainService.computeContentHash(
                    event.getEventType(),
                    event.getActorId(),
                    event.getResourceType(),
                    event.getResourceId(),
                    currentPayload
            );

            // save original encrypted
            byte[] enc = cryptoService.encrypt(currentPayload.getBytes(StandardCharsets.UTF_8));
            String keyId = cryptoService.keyId();
            AuditEventOriginal original = new AuditEventOriginal(id, enc, originalContentHash, keyId, redactedBy);
            AuditEventOriginal saved = originals.save(original);

            // redact fields in payload
            for (String f : fields) {
                if (node.has(f)) {
                    node.put(f, "<REDACTED:" + f + ">");
                }
            }
            String redactedJson = mapper.writeValueAsString(node);

            // apply changes: update payload, mark redacted, link proof
            event.setPayload(redactedJson);
            event.setRedacted(true);
            event.setRedactionProofId(saved.getId());

            repository.saveAndFlush(event);
            return repository.findById(id).orElse(event);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
