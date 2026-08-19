package com.integritylog.web.dto;

import com.integritylog.domain.AuditEvent;
import org.springframework.data.domain.Page;
import java.util.List;
public record PagedAuditEventResponse(
        List<AuditEventResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static PagedAuditEventResponse from(Page<AuditEvent> page) {
        return new PagedAuditEventResponse(
                page.getContent().stream().map(AuditEventResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
