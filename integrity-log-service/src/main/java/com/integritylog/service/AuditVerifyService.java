package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.VerifyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditVerifyService {

    private final AuditEventRepository repository;
    private final HashChainService hashChainService;

    public AuditVerifyService(AuditEventRepository repository, HashChainService hashChainService) {
        this.repository = repository;
        this.hashChainService = hashChainService;
    }

    public VerifyResponse verifyChain() {
        List<AuditEvent> events = repository.findAllByOrderBySequenceNumberAsc();
        String expectedPreviousHash = hashChainService.genesisHash();

        for (AuditEvent event : events) {
            if (!expectedPreviousHash.equals(event.getPreviousHash())) {
                return VerifyResponse.invalid(
                        events.size(),
                        event.getSequenceNumber(),
                        "Previous hash mismatch at sequence " + event.getSequenceNumber()
                );
            }
            if (!hashChainService.contentHashMatches(event)) {
                return VerifyResponse.invalid(
                        events.size(),
                        event.getSequenceNumber(),
                        "Content hash mismatch at sequence " + event.getSequenceNumber()
                );
            }
            if (!hashChainService.recordHashMatches(event)) {
                return VerifyResponse.invalid(
                        events.size(),
                        event.getSequenceNumber(),
                        "Record hash mismatch at sequence " + event.getSequenceNumber()
                );
            }
            expectedPreviousHash = event.getRecordHash();
        }

        return VerifyResponse.valid(events.size());
    }
}
