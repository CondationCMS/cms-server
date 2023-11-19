package com.github.thmarx.cms.api.utils;

/*-
 * #%L
 * cms-server
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
import com.github.thmarx.cms.api.Constants;

/**
 *
 * @author t.marx
 */
public class SectionUtil {

	public static boolean isOrderedSection(final String name) {
		return Constants.SECTION_ORDERED_PATTERN.matcher(name).matches();
	}

	public static String getSectionName(final String name) {
		if (isOrderedSection(name)) {
			var matcher = Constants.SECTION_ORDERED_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		} else {
			var matcher = Constants.SECTION_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		}
	}

	public static int getSectionIndex(final String name) {
		if (isOrderedSection(name)) {
			var matcher = Constants.SECTION_ORDERED_PATTERN.matcher(name);
			matcher.matches();
			return Integer.parseInt(matcher.group("index"));
		} else {
			return Constants.DEFAULT_SECTION_ORDERED_INDEX;
		}
	}

	public static boolean isSection(final String name) {
		return Constants.SECTION_PATTERN.matcher(name).matches()
				|| Constants.SECTION_ORDERED_PATTERN.matcher(name).matches();
	}
}
