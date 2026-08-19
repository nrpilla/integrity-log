package com.integritylog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class AttestationService {

    private final SecretKeySpec keySpec;

    public AttestationService(@Value("${integritylog.attestation.key:change-me-in-production}") String key) {
        this.keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
    }

    public String sign(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] sig = mac.doFinal(data);
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
