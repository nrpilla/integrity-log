package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventOriginalRepository;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditExportServiceTest {

    @Autowired
    private AuditWriteService writeService;

    @Autowired
    private RedactionService redactionService;

    @Autowired
    private AuditExportService exportService;

    @Autowired
    private AuditEventRepository repository;

    @Autowired
    private AuditEventOriginalRepository originals;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        originals.deleteAll();
        repository.deleteAll();
    }

    @Test
    void exportIncludesBundleAndAttestationForRedactedRecords() {
        AuditEvent event = writeService.append(new CreateAuditEventRequest(
                "USER.UPDATE",
                "alice",
                "user",
                "u1",
                Map.of("name", "Alice", "ssn", "123-45-6789")
        ));

        redactionService.redact(event.getId(), java.util.List.of("ssn"), "admin@example.com");

        var exported = exportService.export(Optional.of("alice"), Optional.of("u1"));

        assertThat(exported).containsKey("bundle");
        assertThat(exported).containsKey("attestation");

        Map<String, Object> bundle = (Map<String, Object>) exported.get("bundle");
        assertThat(bundle).containsKey("metadata");
        assertThat(bundle).containsKey("events");
        assertThat(bundle).containsKey("originals");
        assertThat((java.util.List<?>) bundle.get("events")).isNotEmpty();
        assertThat((java.util.List<?>) bundle.get("originals")).isNotEmpty();

        Map<String, Object> attestation = (Map<String, Object>) exported.get("attestation");
        assertThat(attestation.get("algorithm")).isEqualTo("HMAC-SHA256");
        assertThat(attestation.get("signature").toString()).isNotBlank();
    }
}
