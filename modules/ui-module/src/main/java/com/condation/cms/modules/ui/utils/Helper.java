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
