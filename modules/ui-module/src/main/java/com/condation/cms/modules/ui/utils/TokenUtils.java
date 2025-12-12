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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenUtils {

	/**
	 * Erstelle ein neues JWT Token
	 * @param username Benutzername
	 * @param secret Geheimer Schlüssel für die Signatur
	 * @param payloadData Zusätzliche Claims
	 * @param idleTimeout Idle Timeout (Inaktivität)
	 * @param maxLifetime Absolutes Maximum
	 * @return JWT Token String
	 */
	public static String createToken(String username, String secret, 
			Map<String, Object> payloadData, Duration idleTimeout, Duration maxLifetime) throws Exception {
		
		Instant now = Instant.now();
		Instant expiresAt = now.plus(idleTimeout);
		Instant maxAt = now.plus(maxLifetime);
		
		// Erstelle Secret Key aus String
		SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		
		// Baue das JWT
		String token = Jwts.builder()
				// Standard Claims
				.subject(username)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				
				// Custom Claims
				.claim("username", username)
				.claim("expiresAt", expiresAt.getEpochSecond())
				.claim("maxLifetime", maxAt.getEpochSecond())
				.claim("tokenId", UUID.randomUUID().toString())
				
				// Zusätzliche Payload-Daten
				.claims(payloadData)
				
				// Signiere mit HMAC-SHA256
				.signWith(key)
				.compact();
		
		return token;
	}

	/**
	 * Erstelle Token mit Standard Payload
	 * @param username
	 * @param secret
	 * @return 
	 */
	public static String createToken(String username, String secret, 
			Duration idleTimeout, Duration maxLifetime) throws Exception {
		return createToken(username, secret, new HashMap<>(), idleTimeout, maxLifetime);
	}

	/**
	 * Validiere und dekodiere das Token
	 * @param token JWT Token String
	 * @param secret Geheimer Schlüssel
	 * @return Optional<Payload> mit Token-Daten oder leer wenn ungültig
	 */
	public static Optional<Payload> getPayload(String token, String secret) {
		try {
			// Erstelle Secret Key
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			
			// Parse und validiere Token
			Jws<Claims> jws = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token);
			
			Claims claims = jws.getBody();
			
			// Hole Custom Claims
			String username = claims.get("username", String.class);
			Long expiresAt = claims.get("expiresAt", Long.class);
			Long maxLifetime = claims.get("maxLifetime", Long.class);
			
			long now = Instant.now().getEpochSecond();
			
			// Prüfe Idle Timeout
			if (now >= expiresAt) {
				log.debug("Token idle timeout exceeded");
				return Optional.empty();
			}
			
			// Prüfe absolutes Maximum
			if (now >= maxLifetime) {
				log.debug("Token max lifetime exceeded");
				return Optional.empty();
			}
			
			// Extrahiere alle Claims als Map
			Map<String, Object> data = new HashMap<>(claims);
			
			return Optional.of(new Payload(
					username,
					claims.getIssuedAt().getTime() / 1000,
					expiresAt,
					maxLifetime,
					data
			));
			
		} catch (ExpiredJwtException e) {
			log.debug("Token has expired");
			return Optional.empty();
		} catch (JwtException e) {
			log.debug("Invalid JWT token: {}", e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			log.debug("Error parsing token", e);
			return Optional.empty();
		}
	}

	/**
	 * Payload Record für Token-Daten
	 */
	public record Payload(
			String username,
			long issuedAt,
			long expiresAt,
			long maxLifetime,
			Map<String, Object> data) {
		
		/**
		 * Prüfe ob Token noch gültig ist (Idle + Max Lifetime)
		 */
		public boolean isValid() {
			long now = Instant.now().getEpochSecond();
			return now < expiresAt && now < maxLifetime;
		}
		
		public boolean isAuthToken () {
			return data.getOrDefault("type", "none").equals("auth");
		}
	}
}