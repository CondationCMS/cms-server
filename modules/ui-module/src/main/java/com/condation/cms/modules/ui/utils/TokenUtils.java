package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenUtils {

	/**
	 * Create a new JWT.
	 * @param username Username
	 * @param secret Secret key used for signing
	 * @param payloadData Additional claims
	 * @param idleTimeout Idle timeout
	 * @param maxLifetime Maximum lifetime
	 * @return JWT string
	 */
	public static String createToken(String username, String secret, 
			Map<String, Object> payloadData, Duration idleTimeout, Duration maxLifetime) throws Exception {
		
		Objects.requireNonNull(secret, "Secret must not be null");
		
		Instant now = Instant.now();
		Instant expiresAt = now.plus(idleTimeout);
		Instant maxAt = now.plus(maxLifetime);
		
		// Create the secret key from the string
		SecretKey key;
		try {
			key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		} catch (WeakKeyException exception) {
			throw new IllegalArgumentException(
					"Invalid configuration 'ui.secret': must contain at least 32 bytes (256 bits) for JWT HMAC signing.",
					exception);
		}
		
		// Build the JWT
		String token = Jwts.builder()
				// Standard claims
				.subject(username)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				
				// Custom claims
				.claim("username", username)
				.claim("expiresAt", expiresAt.getEpochSecond())
				.claim("maxLifetime", maxAt.getEpochSecond())
				.claim("tokenId", UUID.randomUUID().toString())
				
				// Additional payload data
				.claims(payloadData)
				
				// Sign with HMAC-SHA256
				.signWith(key)
				.compact();
		
		return token;
	}

	/**
	 * Create a token with the default payload.
	 * @param username Username
	 * @param secret Secret key used for signing
	 * @return JWT string
	 */
	public static String createToken(String username, String secret, 
			Duration idleTimeout, Duration maxLifetime) throws Exception {
		return createToken(username, secret, new HashMap<>(), idleTimeout, maxLifetime);
	}

	/**
	  * Validate and decode the token.
	  * @param token JWT string
	  * @param secret Secret key used for signing
	  * @return An optional payload containing the token data, or empty if the token is invalid
	 */
	public static Optional<Payload> getPayload(String token, String secret) {
		try {
			// Create the secret key
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			
			// Parse and validate the token
			Jws<Claims> jws = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token);
			
			Claims claims = jws.getBody();
			
			// Get custom claims
			String username = claims.get("username", String.class);
			Long expiresAt = claims.get("expiresAt", Long.class);
			Long maxLifetime = claims.get("maxLifetime", Long.class);
			
			long now = Instant.now().getEpochSecond();
			
			// Check the idle timeout
			if (now >= expiresAt) {
				log.debug("Token idle timeout exceeded");
				return Optional.empty();
			}
			
			// Check the maximum lifetime
			if (now >= maxLifetime) {
				log.debug("Token max lifetime exceeded");
				return Optional.empty();
			}
			
			// Extract all claims into a map
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
	 * Payload record containing token data.
	 */
	public record Payload(
			String username,
			long issuedAt,
			long expiresAt,
			long maxLifetime,
			Map<String, Object> data) {
		
		/**
		 * Check whether the token is still valid (idle timeout and maximum lifetime).
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
