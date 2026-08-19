package com.integritylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventOriginalRepository;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import com.integritylog.web.dto.ViolationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditVerifyServiceTest {

    @Autowired
    private AuditWriteService writeService;

    @Autowired
    private AuditVerifyService verifyService;

    @Autowired
    private AuditEventRepository repository;

    @Autowired
    private AuditEventOriginalRepository originals;

    @Autowired
    private CryptoService cryptoService;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        originals.deleteAll();
        repository.deleteAll();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void verifyChainReturnsValidForLinkedEvents() {
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"valid-1\"}")
        ));
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_REVOKED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"valid-2\"}")
        ));

        var response = verifyService.verifyChain();

        assertThat(response.valid()).isTrue();
        assertThat(response.eventCount()).isEqualTo(2);
        assertThat(response.brokenAtSequence()).isNull();
        assertThat(response.violationType()).isNull();
    }

    @Test
    void verifyChainAcceptsLegitimateRedactionWithProof() {
        AuditEvent event = writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"redacted-1\",\"ssn\":\"123-45-6789\"}")
        ));

        RedactionService redactionService = new RedactionService(repository, originals, cryptoService, new HashChainService());
        redactionService.redact(event.getId(), java.util.List.of("ssn"), "admin@example.com");

        var response = verifyService.verifyChain();

        assertThat(response.valid()).isTrue();
        assertThat(response.eventCount()).isEqualTo(1);
    }

    @Test
    void verifyChainDetectsTamperedRecordHash() {
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"tamper-1\"}")
        ));

        AuditEvent event = repository.findAllByOrderBySequenceNumberAsc().getFirst();
        ReflectionTestUtils.setField(event, "recordHash", "invalid-hash");
        repository.saveAndFlush(event);

        var response = verifyService.verifyChain();

        assertThat(response.valid()).isFalse();
        assertThat(response.brokenAtSequence()).isEqualTo(event.getSequenceNumber());
        assertThat(response.violationType()).isEqualTo(ViolationType.RECORD_HASH);
        assertThat(response.message()).contains("Record hash mismatch");
    }

    private java.util.Map<String, Object> payload(String json) {
        try {
            return objectMapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid payload JSON", e);
        }
    }
}
