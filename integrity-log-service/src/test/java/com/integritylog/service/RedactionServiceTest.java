package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import com.integritylog.domain.AuditEventOriginal;
import com.integritylog.repository.AuditEventOriginalRepository;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedactionServiceTest {

    @Autowired
    private AuditWriteService writeService;

    @Autowired
    private RedactionService redactionService;

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

    @Test
    void redactStoresEncryptedOriginalAndMarksEventAsRedacted() {
        AuditEvent event = writeService.append(new CreateAuditEventRequest(
                "USER.UPDATE",
                "alice",
                "user",
                "u1",
                Map.of("name", "Alice", "ssn", "123-45-6789")
        ));

        AuditEvent redacted = redactionService.redact(event.getId(), List.of("ssn"), "admin@example.com");

        assertThat(redacted.isRedacted()).isTrue();
        assertThat(redacted.getPayload()).contains("<REDACTED:ssn>");
        assertThat(redacted.getRedactionProofId()).isNotNull();

        AuditEventOriginal original = originals.findTopByEventIdOrderByCreatedAtDesc(event.getId()).orElseThrow();
        assertThat(original.getRedactedBy()).isEqualTo("admin@example.com");
        assertThat(original.getOriginalContentHash()).isEqualTo(event.getContentHash());
        assertThat(original.getEncryptedPayload()).isNotEmpty();

        String decrypted = new String(cryptoService.decrypt(original.getEncryptedPayload()), StandardCharsets.UTF_8);
        assertThat(decrypted).contains("123-45-6789");
    }
}
