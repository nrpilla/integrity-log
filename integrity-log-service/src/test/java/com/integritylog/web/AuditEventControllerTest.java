package com.integritylog.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integritylog.domain.AuditEvent;
import com.integritylog.domain.ClientAccessAudit;
import com.integritylog.service.AuditEventQuery;
import com.integritylog.service.AuditQueryService;
import com.integritylog.service.AuditVerifyService;
import com.integritylog.service.AuditWriteService;
import com.integritylog.service.ClientAccessAuditService;
import com.integritylog.web.dto.ClientAccessAuditRequest;
import com.integritylog.web.dto.CreateAuditEventRequest;
import com.integritylog.web.dto.VerifyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditEventControllerTest {

    private final AuditWriteService writeService = mock(AuditWriteService.class);
    private final AuditQueryService queryService = mock(AuditQueryService.class);
    private final AuditVerifyService verifyService = mock(AuditVerifyService.class);
    private final ClientAccessAuditService clientAccessAuditService = mock(ClientAccessAuditService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuditEventController(writeService, queryService, verifyService, clientAccessAuditService))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void appendCreatesAuditEventResponse() throws Exception {
        AuditEvent event = new AuditEvent(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"controller-test\"}",
                "content-hash",
                "previous-hash",
                "record-hash"
        );
        when(writeService.append(any())).thenReturn(event);

        String requestBody = objectMapper.writeValueAsString(new CreateAuditEventRequest(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                objectMapper.readValue("{\"source\":\"controller-test\"}", java.util.Map.class)
        ));

        mockMvc.perform(post("/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("CONSENT_GRANTED"))
                .andExpect(jsonPath("$.resourceId").value("c-100"));
    }

    @Test
    void queryReturnsPagedAuditEvents() throws Exception {
        AuditEvent event = new AuditEvent(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"query-test\"}",
                "content-hash",
                "previous-hash",
                "record-hash"
        );
        when(queryService.query(any(AuditEventQuery.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), PageRequest.of(0, 10), 1L));

        mockMvc.perform(get("/audit/events")
                        .param("actorId", "user-1")
                        .param("resourceType", "consent")
                        .param("resourceId", "c-100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].actorId").value("user-1"));
    }

    @Test
    void verifyReturnsIntegrityStatus() throws Exception {
        when(verifyService.verifyChain()).thenReturn(VerifyResponse.valid(2));

        mockMvc.perform(get("/audit/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.eventCount").value(2));
    }

    @Test
    void recordAccessCreatesComplianceAuditEntry() throws Exception {
        ClientAccessAudit entry = new ClientAccessAudit(
                "user-1",
                "client-account",
                "acct-100",
                "READ",
                "ALLOW",
                "Regulator review",
                "corr-123",
                "127.0.0.1",
                "{\"purpose\":\"regulator-review\"}"
        );
        when(clientAccessAuditService.recordAccess(any(ClientAccessAuditRequest.class))).thenReturn(entry);

        String requestBody = objectMapper.writeValueAsString(new ClientAccessAuditRequest(
                "user-1",
                "client-account",
                "acct-100",
                "READ",
                "ALLOW",
                "Regulator review",
                "corr-123",
                "127.0.0.1",
                java.util.Map.of("purpose", "regulator-review")
        ));

        mockMvc.perform(post("/audit/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.actorId").value("user-1"))
                .andExpect(jsonPath("$.resourceId").value("acct-100"));
    }

    @Test
    void queryAccessReturnsComplianceAuditEntries() throws Exception {
        ClientAccessAudit entry = new ClientAccessAudit(
                "user-1",
                "client-account",
                "acct-100",
                "READ",
                "ALLOW",
                "Regulator review",
                "corr-123",
                "127.0.0.1",
                "{\"screen\":\"account-summary\"}"
        );
        when(clientAccessAuditService.query(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/audit/access")
                        .param("actorId", "user-1")
                        .param("resourceType", "client-account")
                        .param("resourceId", "acct-100")
                        .param("action", "READ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actorId").value("user-1"))
                .andExpect(jsonPath("$[0].resourceId").value("acct-100"));
    }
}
