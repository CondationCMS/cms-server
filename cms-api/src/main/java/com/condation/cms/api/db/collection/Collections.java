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

import java.util.Set;

/**
 * Site-scoped access to collections.
 */
public interface Collections {

	Collection collection(String name);

	Set<String> names();

	/** Returns whether a collection is stored by this site and may be modified here. */
	default boolean isLocal(String collection) {
		return true;
	}

	/** Re-indexes one item after a synchronous manager write, or removes a deleted item from the index. */
	void refresh(String collection, String id);
}
