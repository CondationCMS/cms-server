package com.condation.cms.content.utils;

/*-
 * #%L
 * CMS Content
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.github.slugify.Slugify;

/** Shared slug rules for generated paths and collection routes. */
public final class SlugUtil {

	private static final Slugify SLUGIFIER = Slugify.builder()
			.customReplacement("ä", "ae")
			.customReplacement("Ä", "ae")
			.customReplacement("ü", "ue")
			.customReplacement("Ü", "ue")
			.customReplacement("ö", "oe")
			.customReplacement("Ö", "oe")
			.customReplacement("ß", "ss")
			.lowerCase(true)
			.build();

	private SlugUtil() {
	}

	public static String slugify(String input) {
		return SLUGIFIER.slugify(input);
	}
}
