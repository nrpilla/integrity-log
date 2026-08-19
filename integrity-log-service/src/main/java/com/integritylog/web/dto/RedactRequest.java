package com.integritylog.web.dto;

import java.util.List;

public record RedactRequest(
        List<String> fields,
        String redactedBy
) {
}
