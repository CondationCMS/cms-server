package com.condation.cms.api.db.collection;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.regex.Pattern;

/** Canonical validation rules for collection item identifiers. */
public final class CollectionItemId {

	private static final Pattern VALID_ID = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_.-]*");

	private CollectionItemId() {
	}

	public static boolean isValid(String id) {
		return id != null && VALID_ID.matcher(id).matches();
	}

	public static String requireValid(String id) {
		if (!isValid(id)) {
			throw new IllegalArgumentException("invalid collection item id: " + id);
		}
		return id;
	}
}
