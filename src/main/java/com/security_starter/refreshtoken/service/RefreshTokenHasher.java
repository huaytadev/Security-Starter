package com.security_starter.refreshtoken.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {
	public String hash(String token) {

		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("Refresh token cannot be null or blank");
		}

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(hash.length * 2);

			for (byte value : hash) {
				hex.append(String.format("%02x", value));
			}

			return hex.toString();

		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 algorithm is not available", ex);
		}
	}
}
