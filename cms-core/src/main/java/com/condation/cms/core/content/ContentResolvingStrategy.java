package com.condation.cms.core.content;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import java.util.Optional;

/**
 *
 * @author thmar
 */
public class ContentResolvingStrategy {
	
	public static String uriToPath (String uri) {
		if (uri == null) {
			return "";
		} else if (uri.equals("/")) {
			return "";
		} else if (uri.startsWith("/")) {
			return uri.substring(1);
		}
		return uri;
	}
	
	public static Optional<ReadOnlyFile> resolve (String uri, DB db) {
		var path = uriToPath(uri);
		
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		var contentPath = contentBase.resolve(path);
		ReadOnlyFile contentFile = null;
		if (contentPath.exists() && contentPath.isDirectory()) {
			// use index.md
			var tempFile = contentPath.resolve("index.md");
			if (tempFile.exists()) {
				contentFile = tempFile;
			}
		} else {
			var temp = contentBase.resolve(path + ".md");
			if (temp.exists()) {
				contentFile = temp;
			}
		}
		
		return Optional.ofNullable(contentFile);
	}
}
