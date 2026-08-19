package com.integritylog.service;

import com.integritylog.domain.ClientAccessAudit;
import com.integritylog.repository.ClientAccessAuditRepository;
import com.integritylog.web.dto.ClientAccessAuditRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClientAccessAuditServiceTest {

    @Autowired
    private ClientAccessAuditService service;

    @Autowired
    private ClientAccessAuditRepository repository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void recordAndQueryAccessEventsByCriteria() {
        service.recordAccess(new ClientAccessAuditRequest(
                "user-1",
                "client-account",
                "acct-100",
                "READ",
                "ALLOW",
                "Review",
                "corr-123",
                "127.0.0.1",
                Map.of("screen", "account-summary")
        ));

        service.recordAccess(new ClientAccessAuditRequest(
                "user-1",
                "client-account",
                "acct-100",
                "WRITE",
                "DENY",
                "Policy",
                "corr-456",
                "127.0.0.2",
                Map.of("reason", "insufficient_permission")
        ));

        List<ClientAccessAudit> byActor = service.query("user-1", null, null, null, null, null);
        List<ClientAccessAudit> byResource = service.query(null, "client-account", "acct-100", null, null, null);
        List<ClientAccessAudit> byActionAndTime = service.query(null, null, null, "READ",
                Instant.parse("2020-01-01T00:00:00Z"), Instant.now().plusSeconds(3600));

        assertThat(byActor).hasSize(2);
        assertThat(byResource).hasSize(2);
        assertThat(byActionAndTime).hasSize(1);
        assertThat(byActionAndTime.getFirst().getDecision()).isEqualTo("ALLOW");
    }
}
