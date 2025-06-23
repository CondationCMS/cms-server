package com.condation.cms.modules.system.api.services;

/*-
 * #%L
 * cms-system-modules
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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.modules.system.api.helpers.NodeHelper;
import com.condation.cms.modules.system.api.helpers.WhitelistFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
public class ContentService {

	private final DB db;
	private final Set<String> metaWhiteList;
	
	public Optional<ApiContentNode> resolve (String uri, Request request) {
		var resolved = resolveContentNode(uri);
		if (resolved.isEmpty()) {
			return Optional.empty();
		}
		
		final ContentNode node = resolved.get();
		
		return Optional.of(new ApiContentNode(
				uri, 
				NodeHelper.getLinks(node, request), 
				WhitelistFilter.applyWhitelist(node.data(), metaWhiteList)
		));
	}
	
	private Optional<ContentNode> resolveContentNode(String uri) {
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		var contentPath = contentBase.resolve(uri);
		ReadOnlyFile contentFile = null;
		if (contentPath.exists() && contentPath.isDirectory()) {
			// use index.md
			var tempFile = contentPath.resolve("index.md");
			if (tempFile.exists()) {
				contentFile = tempFile;
			} else {
				return Optional.empty();
			}
		} else {
			var temp = contentBase.resolve(uri + ".md");
			if (temp.exists()) {
				contentFile = temp;
			} else {
				return Optional.empty();
			}
		}
		
		var filePath = PathUtil.toRelativeFile(contentFile, contentBase);
		if (!db.getContent().isVisible(filePath)) {
			return Optional.empty();
		}
		
		final ContentNode contentNode = db.getContent().byUri(filePath).get();

		return Optional.ofNullable(contentNode);
	}
}
