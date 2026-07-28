package com.condation.cms.content;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.filesystem.FileSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@RequiredArgsConstructor
public class VariantResolver {
	
	private final DB db;

	public Optional<Variant> loadVariant (ContentNode node, String variantId) {
		return getVariants(node).stream().filter(variant -> variant.id().equals(variantId)).findFirst();
	}
	
	public List<Variant> getVariants (ContentNode node) {
		
		var folder = db.getFileSystem().contentBase().resolve(node.path()).getParent();
		var variantsFolder = folder.resolve(".variants/" + removeMarkdown(node.name()));
		
		if (variantsFolder.exists()) {
			try {
				List<Variant> variants = new ArrayList<>();
				variantsFolder.children().stream().filter(ReadOnlyFile::isDirectory).forEach(variant -> {
					var id = variant.getFileName();
					var contentFile = variant.resolve(node.name());
					var url = PathUtil.toURL(contentFile, db.getFileSystem().contentBase());
					var variantNode = db.getContent().byUrl(url);
					if (variantNode.isPresent()) {
						variants.add(new Variant(id, variantNode.get()));
					}
				});
				
				return variants;
			} catch (IOException ex) {
				log.error("error loading variants", ex);
			}
		}
		
		return Collections.emptyList();
	}
	
	private String removeMarkdown (String filename) {
		if (filename.endsWith(".md")) {
			return filename.substring(0, filename.lastIndexOf(".md"));
		}
		return filename;
	}
	
	public record Variant (String id, ContentNode node) {}
}
