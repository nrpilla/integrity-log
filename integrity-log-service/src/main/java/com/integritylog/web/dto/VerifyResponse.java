package com.integritylog.web.dto;

public record VerifyResponse(
        boolean valid,
        long eventCount,
        Long brokenAtSequence,
        ViolationType violationType,
        String message
) {
    public static VerifyResponse valid(long eventCount) {
        return new VerifyResponse(true, eventCount, null, null,"Chain integrity verified");
    }

    public static VerifyResponse invalid(long eventCount, Long brokenAtSequence, ViolationType violationType, String message) {
        return new VerifyResponse(false, eventCount, brokenAtSequence, violationType, message);
    }
}
