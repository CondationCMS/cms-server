package com.github.thmarx.cms.filesystem.datafilter;

/*-
 * #%L
 * cms-filesystem
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

import java.util.Random;

public class TestHelper {
	private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static Random rnd = new Random();
	private static String[] strings = new String[100];
	static {
		for (int i = 0; i < strings.length; i++) {
			strings[i] = randomString(10);
		}
	}

	public static String randomString() {
		
		return strings[rnd.nextInt(10)];
	}
	
	private static String randomString(int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(AB.substring(rnd.nextInt(AB.length())));
		}
		return sb.toString();
	}

	public static int randomInt(int max) {
		return rnd.nextInt(max + 1);
	}
}
