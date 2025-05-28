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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author thorstenmarx
 */
public class TokenUtils {

 	public static Optional<String> getUserName(String token, String SECRET) throws Exception {
		if (!validateToken(token, SECRET)) {
			return Optional.empty();
		}
		String[] parts = token.split(":");
		return Optional.of(parts[0]);
	}
	
	public static boolean validateToken(String token, String SECRET) throws Exception {
		String[] parts = token.split(":");
		if (parts.length != 3) {
			return false;
		}

		String payload = parts[0] + ":" + parts[1];
		String expectedSig = hmacSha256(payload, SECRET);
		if (!expectedSig.equals(parts[2])) {
			return false;
		}

		long timestamp = Long.parseLong(parts[1]);
		long now = Instant.now().getEpochSecond();
		return (now - timestamp) < 3600; // z. B. 1 Stunde gültig
	}

	public static String hmacSha256(String data, String key) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		mac.init(secretKeySpec);
		byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
	}

	public static String createToken(String username, String SECRET) throws Exception {
		long timestamp = Instant.now().getEpochSecond();
		String payload = username + ":" + timestamp;
		String signature = hmacSha256(payload, SECRET);
		return payload + ":" + signature;
	}
}
