package com.integritylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditWriteServiceTest {

    @Autowired
    private AuditWriteService writeService;

    @Autowired
    private AuditEventRepository repository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void appendCreatesAHashLinkedSequence() {
        var first = writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"one\"}")
        ));

        var second = writeService.append(new CreateAuditEventRequest(
                "CONSENT_REVOKED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"two\"}")
        ));

        var saved = repository.findAllByOrderBySequenceNumberAsc();

        assertThat(saved).hasSize(2);
        assertThat(first.getPreviousHash()).isEqualTo(new HashChainService().genesisHash());
        assertThat(second.getPreviousHash()).isEqualTo(first.getRecordHash());
        assertThat(saved.get(1).getPreviousHash()).isEqualTo(saved.get(0).getRecordHash());

        assertThat(saved.get(0).getContentHash()).isNotBlank();
        assertThat(saved.get(0).getRecordHash()).isNotBlank();
        assertThat(saved.get(0).getSequenceNumber()).isNotNull();
        assertThat(saved.get(0).getCreatedAt()).isNotNull();
    }

    private java.util.Map<String, Object> payload(String json) {
        try {
            return objectMapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid payload JSON", e);
        }
    }
}
