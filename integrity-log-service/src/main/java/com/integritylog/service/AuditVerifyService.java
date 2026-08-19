package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.repository.AuditEventOriginalRepository;
import com.integritylog.web.dto.VerifyResponse;
import com.integritylog.web.dto.ViolationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditVerifyService {

    private final AuditEventRepository repository;
    private final HashChainService hashChainService;
    private final AuditEventOriginalRepository originals;
    private final CryptoService cryptoService;

    public AuditVerifyService(AuditEventRepository repository, HashChainService hashChainService,
                              AuditEventOriginalRepository originals, CryptoService cryptoService) {
        this.repository = repository;
        this.hashChainService = hashChainService;
        this.originals = originals;
        this.cryptoService = cryptoService;
    }

    public VerifyResponse verifyChain() {
        List<AuditEvent> events = repository.findAllByOrderBySequenceNumberAsc();
        String expectedPreviousHash = hashChainService.genesisHash();
        for (AuditEvent event : events) {
            if (!expectedPreviousHash.equals(event.getPreviousHash())) {
                return VerifyResponse.invalid(
                        events.size(),
                        event.getSequenceNumber(),
                        ViolationType.PREVIOUS_HASH,
                        "Previous hash mismatch at sequence " + event.getSequenceNumber()
                );
            }

            if (!hashChainService.contentHashMatches(event)) {
                // content hash mismatch - check if legitimately redacted with proof
                var proof = originals.findTopByEventIdOrderByCreatedAtDesc(event.getId());
                if (proof.isPresent()) {
                    try {
                        byte[] decrypted = cryptoService.decrypt(proof.get().getEncryptedPayload());
                        String originalPayload = new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
                        String originalContentHash = hashChainService.computeContentHash(
                                event.getEventType(),
                                event.getActorId(),
                                event.getResourceType(),
                                event.getResourceId(),
                                originalPayload
                        );
                        if (!originalContentHash.equals(event.getContentHash())) {
                            return VerifyResponse.invalid(
                                    events.size(),
                                    event.getSequenceNumber(),
                                    ViolationType.CONTENT_HASH,
                                    "Content hash mismatch at sequence " + event.getSequenceNumber()
                            );
                        }
                        // else: legitimately redacted - continue
                    } catch (Exception ex) {
                        return VerifyResponse.invalid(
                                events.size(),
                                event.getSequenceNumber(),
                                ViolationType.CONTENT_HASH,
                                "Content hash mismatch and failed to validate redaction proof at sequence " + event.getSequenceNumber()
                        );
                    }
                } else {
                    return VerifyResponse.invalid(
                            events.size(),
                            event.getSequenceNumber(),
                            ViolationType.CONTENT_HASH,
                            "Content hash mismatch at sequence " + event.getSequenceNumber()
                    );
                }
            }

            if (!hashChainService.recordHashMatches(event)) {
                return VerifyResponse.invalid(
                        events.size(),
                        event.getSequenceNumber(),
                        ViolationType.RECORD_HASH,
                        "Record hash mismatch at sequence " + event.getSequenceNumber()
                );
            }
            expectedPreviousHash = event.getRecordHash();
        }
        return VerifyResponse.valid(events.size());
    }
}
