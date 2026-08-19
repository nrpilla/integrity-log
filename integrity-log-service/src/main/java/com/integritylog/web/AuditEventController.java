package com.integritylog.web;

import com.integritylog.domain.AuditEvent;
import com.integritylog.service.AuditEventQuery;
import com.integritylog.service.AuditQueryService;
import com.integritylog.service.AuditVerifyService;
import com.integritylog.service.AuditWriteService;
import com.integritylog.web.dto.AuditEventResponse;
import com.integritylog.web.dto.CreateAuditEventRequest;
import com.integritylog.web.dto.PagedAuditEventResponse;
import com.integritylog.web.dto.VerifyResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.Instant;
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
    public PagedAuditEventResponse query(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        AuditEventQuery query = new AuditEventQuery(actorId, resourceType, resourceId, eventType, from, to);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("sequenceNumber").ascending());
        return PagedAuditEventResponse.from(queryService.query(query, pageable));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
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
