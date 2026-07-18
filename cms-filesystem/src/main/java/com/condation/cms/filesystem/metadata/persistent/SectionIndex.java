package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * CMS FileSystem
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

import com.condation.cms.api.utils.SectionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

final class SectionIndex {

	private static final String MAP_NAME = "sectionsByOwnerPath";
	private static final String KEY_SEPARATOR = "\u0000";

	private final MVMap<String, String> entries;

	SectionIndex(MVStore store) {
		entries = store.openMap(MAP_NAME);
	}

	void add(String sectionPath) {
		ownerPath(sectionPath).ifPresent(pagePath ->
				entries.put(keyPrefix(pagePath) + sectionPath, sectionPath));
	}

	List<String> findByPagePath(String pagePath) {
		var prefix = keyPrefix(pagePath);
		var cursor = entries.cursor(prefix);
		var sectionPaths = new ArrayList<String>();

		while (cursor.hasNext()) {
			var key = cursor.next();
			if (!key.startsWith(prefix)) {
				break;
			}
			sectionPaths.add(cursor.getValue());
		}
		return List.copyOf(sectionPaths);
	}

	void clear() {
		entries.clear();
	}

	private static Optional<String> ownerPath(String sectionPath) {
		var separatorIndex = sectionPath.lastIndexOf('/');
		var fileName = sectionPath.substring(separatorIndex + 1);
		if (!SectionUtil.isSectionEntry(fileName)) {
			return Optional.empty();
		}

		var ownerFileName = fileName.substring(0, fileName.indexOf('.')) + ".md";
		var folder = separatorIndex < 0 ? "" : sectionPath.substring(0, separatorIndex + 1);
		return Optional.of(folder + ownerFileName);
	}

	private static String keyPrefix(String pagePath) {
		return pagePath + KEY_SEPARATOR;
	}
}
