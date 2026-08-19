package com.integritylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.AuditEvent;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.ResourceNotFoundException;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuditArchiveServiceTest {

    @Autowired
    private AuditWriteService writeService;

    @Autowired
    private AuditArchiveService archiveService;

    @Autowired
    private AuditEventRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void archiveMarksEventAsArchivedAndRejectsDuplicateArchive() {
        AuditEvent event = writeService.append(new CreateAuditEventRequest(
                "USER.UPDATE",
                "alice",
                "user",
                "u1",
                payload(Map.of("name", "Alice"))
        ));

        assertThat(archiveService.archive(event.getId())).isTrue();
        AuditEvent saved = repository.findById(event.getId()).orElseThrow();
        assertThat(saved.getArchivedAt()).isNotNull();

        assertThat(archiveService.archive(event.getId())).isFalse();
    }

    @Test
    void archiveThrowsWhenEventDoesNotExist() {
        assertThatThrownBy(() -> archiveService.archive(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("AuditEvent not found");
    }

    private Map<String, Object> payload(Map<String, Object> values) {
        return values;
    }
}
