package com.condation.cms.api.utils;

/*-
 * #%L
 * CMS Api
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

import com.condation.cms.api.Constants;

/**
 *
 * @author t.marx
 */
public class SectionUtil {

	public static boolean isNamedSectionEntry(final String name) {
		return Constants.SECTION_ENTRY_NAMED_PATTERN.matcher(name).matches();
	}

	public static String getSectionName(final String name) {
		if (isNamedSectionEntry(name)) {
			var matcher = Constants.SECTION_ENTRY_NAMED_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		} else {
			var matcher = Constants.SECTION_ENTRY_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		}
	}

	public static boolean isSectionEntry(final String name) {
		return Constants.SECTION_ENTRY_PATTERN.matcher(name).matches()
				|| Constants.SECTION_ENTRY_NAMED_PATTERN.matcher(name).matches();
	}
}
