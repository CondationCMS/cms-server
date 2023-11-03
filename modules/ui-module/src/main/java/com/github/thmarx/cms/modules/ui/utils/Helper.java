package com.github.thmarx.cms.modules.ui.utils;

import com.google.common.hash.Hashing;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 *
 * @author t.marx
 */
public abstract class Helper {
	/**
	 *
	 * @author marx
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	public static String hash(String value) {
		return Hashing.sha256()
				.hashString(value, StandardCharsets.UTF_8)
				.toString();

	}

	public static String randomString() {
		return new BigInteger(130, RANDOM).toString(32);
	}
}
