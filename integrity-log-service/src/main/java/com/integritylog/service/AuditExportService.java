package com.integritylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.AuditEvent;
import com.integritylog.domain.AuditEventOriginal;
import com.integritylog.repository.AuditEventOriginalRepository;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.repository.AuditEventSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditExportService {

    private final AuditEventRepository repository;
    private final AuditEventOriginalRepository originals;
    private final AttestationService attestationService;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public AuditExportService(AuditEventRepository repository,
                              AuditEventOriginalRepository originals,
                              AttestationService attestationService) {
        this.repository = repository;
        this.originals = originals;
        this.attestationService = attestationService;
    }

    public Map<String, Object> export(Optional<String> actorId, Optional<String> resourceId) {
        // build spec via existing AuditEventSpecifications by creating a simple query DTO
        var q = new com.integritylog.service.AuditEventQuery(actorId.orElse(null), resourceId.orElse(null), null, null, null, null);
        var spec = AuditEventSpecifications.matches(q);
        List<AuditEvent> events = repository.findAll(spec).stream()
                .sorted(Comparator.comparing(AuditEvent::getSequenceNumber, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<Map<String, Object>> evts = new ArrayList<>();
        List<Map<String, Object>> origs = new ArrayList<>();
        for (AuditEvent e : events) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("sequenceNumber", e.getSequenceNumber());
            m.put("eventType", e.getEventType());
            m.put("actorId", e.getActorId());
            m.put("resourceType", e.getResourceType());
            m.put("resourceId", e.getResourceId());
            m.put("payload", e.getPayload());
            m.put("contentHash", e.getContentHash());
            m.put("previousHash", e.getPreviousHash());
            m.put("recordHash", e.getRecordHash());
            m.put("archivedAt", e.getArchivedAt());
            m.put("redacted", e.isRedacted());
            m.put("redactionProofId", e.getRedactionProofId());
            evts.add(m);

            if (e.isRedacted() && e.getRedactionProofId() != null) {
                originals.findTopByEventIdOrderByCreatedAtDesc(e.getId()).ifPresent(o -> {
                    Map<String, Object> om = new LinkedHashMap<>();
                    om.put("id", o.getId());
                    om.put("eventId", o.getEventId());
                    om.put("originalContentHash", o.getOriginalContentHash());
                    om.put("keyId", o.getKeyId());
                    om.put("redactedBy", o.getRedactedBy());
                    om.put("createdAt", o.getCreatedAt());
                    om.put("encryptedPayload", Base64.getEncoder().encodeToString(o.getEncryptedPayload()));
                    origs.add(om);
                });
            }
        }

        Map<String, Object> bundle = new LinkedHashMap<>();
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("exportedAt", Instant.now());
        meta.put("count", events.size());
        bundle.put("metadata", meta);
        bundle.put("events", evts);
        bundle.put("originals", origs);

        try {
            byte[] payload = mapper.writeValueAsBytes(bundle);
            String sig = attestationService.sign(payload);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("bundle", bundle);
            out.put("attestation", Map.of("algorithm", "HMAC-SHA256", "signature", sig));
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
