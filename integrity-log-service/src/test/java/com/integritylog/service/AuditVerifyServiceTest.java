package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
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

    @BeforeEach
    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void verifyChainReturnsValidForLinkedEvents() {
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"valid-1\"}"
        ));
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_REVOKED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"valid-2\"}"
        ));

        var response = verifyService.verifyChain();

        assertThat(response.valid()).isTrue();
        assertThat(response.eventCount()).isEqualTo(2);
        assertThat(response.brokenAtSequence()).isNull();
        assertThat(response.violationType()).isNull();
    }

    @Test
    void verifyChainDetectsTamperedRecordHash() {
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"tamper-1\"}"
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
}
