package com.integritylog.service;

import com.integritylog.domain.AuditEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashChainServiceTest {

    private final HashChainService hashChainService = new HashChainService();

    @Test
    void genesisHashIsStableAndEventHashesMatch() {
        String genesisHash = hashChainService.genesisHash();

        assertThat(genesisHash).isNotBlank();
        assertThat(hashChainService.genesisHash()).isEqualTo(genesisHash);

        String contentHash = hashChainService.computeContentHash(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"service-test\"}"
        );
        String recordHash = hashChainService.computeRecordHash(genesisHash, contentHash);

        AuditEvent event = new AuditEvent(
                "CONSENT_GRANTED",
                "user-1",
                "consent",
                "c-100",
                "{\"source\":\"service-test\"}",
                contentHash,
                genesisHash,
                recordHash
        );

        assertThat(hashChainService.contentHashMatches(event)).isTrue();
        assertThat(hashChainService.recordHashMatches(event)).isTrue();
    }
}
