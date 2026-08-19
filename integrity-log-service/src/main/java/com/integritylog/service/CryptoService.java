package com.integritylog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

@Service
public class CryptoService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LEN = 128;
    private final SecretKeySpec keySpec;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(@Value("${integritylog.redaction.key:change-me-in-production}") String key) {
        // Derive 16-byte key from provided secret
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes());
            byte[] k = Arrays.copyOf(digest, 16);
            this.keySpec = new SecretKeySpec(k, AES);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] encrypt(byte[] plain) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] ct = cipher.doFinal(plain);
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] decrypt(byte[] encrypted) {
        try {
            byte[] iv = Arrays.copyOfRange(encrypted, 0, 12);
            byte[] ct = Arrays.copyOfRange(encrypted, 12, encrypted.length);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            return cipher.doFinal(ct);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String keyId() {
        // return hex fingerprint of the derived key
        return HexFormat.of().formatHex(keySpec.getEncoded());
    }
}
