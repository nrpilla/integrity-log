package com.integritylog.web;

import com.integritylog.domain.AuditEvent;
import com.integritylog.service.AuditQueryService;
import com.integritylog.service.AuditVerifyService;
import com.integritylog.service.AuditWriteService;
import com.integritylog.web.dto.AuditEventResponse;
import com.integritylog.web.dto.CreateAuditEventRequest;
import com.integritylog.web.dto.VerifyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
public class AuditEventController {

    private final AuditWriteService writeService;
    private final AuditQueryService queryService;
    private final AuditVerifyService verifyService;

    public AuditEventController(AuditWriteService writeService,
                                AuditQueryService queryService,
                                AuditVerifyService verifyService) {
        this.writeService = writeService;
        this.queryService = queryService;
        this.verifyService = verifyService;
    }

    @PostMapping("/events")
    public ResponseEntity<AuditEventResponse> append(@Valid @RequestBody CreateAuditEventRequest request) {
        AuditEvent saved = writeService.append(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuditEventResponse.from(saved));
    }

    @GetMapping("/events")
    public List<AuditEventResponse> query(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId) {
        return queryService.query(resourceType, resourceId).stream()
                .map(AuditEventResponse::from)
                .toList();
    }

    @GetMapping("/events/{id}")
    public AuditEventResponse getById(@PathVariable UUID id) {
        return AuditEventResponse.from(queryService.getById(id));
    }

    @GetMapping("/verify")
    public VerifyResponse verify() {
        return verifyService.verifyChain();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex) {
        return ex.getMessage();
    }
}
