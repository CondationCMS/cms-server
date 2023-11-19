package com.github.thmarx.cms.filesystem.datafilter;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
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
