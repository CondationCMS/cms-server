package com.condation.cms.auth.utils;

/*-
 * #%L
 * cms-auth
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import java.security.SecureRandom;

/**
 *
 * @author t.marx
 */
public class SecurityUtil {

	private static final SecureRandom RANDOM = new SecureRandom();
	
	public static String hash (final String value) {
		return Hashing.sha256()
				.hashString(value, Charsets.UTF_8)
				.toString();
	}
}
