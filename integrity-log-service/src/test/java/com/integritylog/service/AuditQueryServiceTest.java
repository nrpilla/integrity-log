package com.integritylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.repository.AuditEventRepository;
import com.integritylog.web.dto.CreateAuditEventRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditQueryServiceTest {

    @Autowired
    private AuditWriteService writeService;

    @Autowired
    private AuditQueryService queryService;

    @Autowired
    private AuditEventRepository repository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void queryMatchesExactFilterCriteria() {
        var first = writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"query-a\"}")
        ));
        writeService.append(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                payload("{\"source\":\"query-b\"}")
        ));
        writeService.append(new CreateAuditEventRequest(
                "USER_LOGIN",
                "user-2",
                "user",
                "u-77",
                payload("{\"source\":\"other\"}")
        ));

        var result = queryService.query(
                new AuditEventQuery("user-1", "consent", "c-100", "CONSENT_GRANTED", null, null),
                PageRequest.of(0, 10, Sort.by("sequenceNumber").ascending())
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(event -> event.getActorId().equals("user-1"));
        assertThat(result.getContent()).allMatch(event -> event.getResourceType().equals("consent"));
        assertThat(result.getContent()).allMatch(event -> event.getResourceId().equals("c-100"));
    }

    private java.util.Map<String, Object> payload(String json) {
        try {
            return objectMapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid payload JSON", e);
        }
    }
}
