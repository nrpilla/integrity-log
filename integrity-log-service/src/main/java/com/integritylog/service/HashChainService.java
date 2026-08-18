package com.integritylog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.integritylog.domain.AuditEvent;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HashChainService {

    public static final String GENESIS_SEED = "INTEGRITYLOG_GENESIS_v1";

    public final ObjectMapper canonicalMapper =  new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public String genesisHash() {
        return sha256(GENESIS_SEED);
    }

    public String computeContentHash(String eventType, String actorId, String resourceType,
                                     String resourceId, String payload) {
        Map<String, String> content = new LinkedHashMap<>();
        content.put("actorId", actorId);
        content.put("eventType", eventType);
        content.put("payload", payload == null ? "" : payload);
        content.put("resourceId", resourceId);
        content.put("resourceType", resourceType);
        return sha256(toCanonicalJson(content));
    }

    public String computeRecordHash(String previousHash, String contentHash) {
        return sha256(previousHash + "|" + contentHash);
    }

    public boolean contentHashMatches(AuditEvent event) {
        String expected = computeContentHash(
                event.getEventType(),
                event.getActorId(),
                event.getResourceType(),
                event.getResourceId(),
                event.getPayload()
        );
        return expected.equals(event.getContentHash());
    }

    public boolean recordHashMatches(AuditEvent event) {
        String expected = computeRecordHash(event.getPreviousHash(), event.getContentHash());
        return expected.equals(event.getRecordHash());
    }

    private String toCanonicalJson(Map<String, String> content) {
        try {
            return canonicalMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit content for hashing", e);
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

