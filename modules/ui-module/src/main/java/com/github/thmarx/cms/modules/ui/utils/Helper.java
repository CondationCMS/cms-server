package com.github.thmarx.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
