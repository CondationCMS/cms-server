package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TokenUtils {

	public static Optional<Payload> getPayload(String token, String secret) {
		try {
			String[] parts = token.split(":");
			if (parts.length != 2) {
				return Optional.empty();
			}

			String base64Payload = parts[0];
			String signature = parts[1];

			String expectedSig = hmacSha256(base64Payload, secret);
			if (!MessageDigest.isEqual(expectedSig.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
				return Optional.empty();
			}

			String json = new String(Base64.getUrlDecoder().decode(base64Payload), StandardCharsets.UTF_8);
			Payload payload = UIGsonProvider.INSTANCE.fromJson(json, Payload.class);

			long now = Instant.now().getEpochSecond();

			// BEIDE Grenzen prüfen!
			if (now >= payload.expiresAt()) {
				return Optional.empty();  // Idle timeout
			}
			if (now >= payload.maxLifetime()) {
				return Optional.empty();  // Absolute grenze
			}

			return Optional.of(payload);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static String createToken(String username, String SECRET, Map<String, Object> payloadData, Duration idleTimeout, Duration maxLifetime) throws Exception {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(idleTimeout);
		Instant maxAt = now.plus(maxLifetime);
		
		payloadData.put("uuid", UUID.randomUUID().toString());
		
		Payload payload = new Payload(username,
				now.getEpochSecond(),
				expiresAt.getEpochSecond(),
				maxAt.getEpochSecond(),
				payloadData);

		String json = UIGsonProvider.INSTANCE.toJson(payload);
		String base64Payload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		String signature = hmacSha256(base64Payload, SECRET);
		return base64Payload + ":" + signature;
	}

	public static String createToken(String username, String SECRET, Duration idleTimeout, Duration maxLifetime) throws Exception {
		return createToken(username, SECRET, new HashMap<>(), idleTimeout, maxLifetime);
	}

	private static String hmacSha256(String data, String key) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		mac.init(secretKeySpec);
		byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
	}

	public record Payload(
			String username,
			long issuedAt,
			long expiresAt,
			long maxLifetime,
			Map<String, Object> data) {

	}
}
