package com.integritylog.web.dto;

public record VerifyResponse(
        boolean valid,
        long eventCount,
        Long brokenAtSequence,
        String message
) {
    public static VerifyResponse valid(long eventCount) {
        return new VerifyResponse(true, eventCount, null, "Chain integrity verified");
    }

    public static VerifyResponse invalid(long eventCount, Long brokenAtSequence, String message) {
        return new VerifyResponse(false, eventCount, brokenAtSequence, message);
    }
}
