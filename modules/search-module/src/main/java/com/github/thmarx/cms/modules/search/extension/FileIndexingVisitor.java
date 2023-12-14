package com.github.thmarx.cms.modules.search.extension;

/*-
 * #%L
 * search-module
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
import com.github.thmarx.cms.api.CMSModuleContext;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.content.ContentResponse;
import com.github.thmarx.cms.api.utils.PathUtil;
import com.github.thmarx.cms.api.utils.SectionUtil;
import com.github.thmarx.cms.modules.search.IndexDocument;
import com.github.thmarx.cms.modules.search.SearchEngine;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class FileIndexingVisitor extends SimpleFileVisitor<Path> {

	private final Path contentBase;
	private final SearchEngine searchEngine;
	private final CMSModuleContext moduleContext;

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

		if (SectionUtil.isSection(file.getFileName().toString())) {
			return FileVisitResult.CONTINUE;
		}
		if (!file.getFileName().toString().endsWith(".md")) {
			return FileVisitResult.CONTINUE;
		}

		try {
			log.trace("indexing file {}", file.getFileName().toString());
			
			if (!shouldIndex(file)) {
				log.trace("indexing is disabled for this file");
				return FileVisitResult.CONTINUE;
			}
			
			var uri = PathUtil.toURI(file, contentBase);
			
			var content = getContent(file);

			if (content.isPresent() && Constants.ContentTypes.HTML.equals(content.get().contentType())) {
				final Document parsedContent = Jsoup.parse(content.get().content());
				
				if (noindex(parsedContent)) {
					return FileVisitResult.CONTINUE;
				}
				
				final Elements contentElements = parsedContent.select("#content");
				String text;
				if (contentElements != null && !contentElements.isEmpty()) {
					text = contentElements.text();
				} else {
					text = parsedContent.text();
				}
				String title = "";
				final Elements titleElements = parsedContent.select("head title");
				if (titleElements != null && !titleElements.isEmpty()) {
					title = titleElements.text();
				}
				
				IndexDocument document = new IndexDocument(uri, title, text);
				searchEngine.index(document);
			}

		} catch (Exception e) {
			log.error(null, e);
		}

		return FileVisitResult.CONTINUE;
	}

	private boolean shouldIndex (Path contentFile) {
		Optional<Map<String, Object>> meta = moduleContext.getDb().getContent().getMeta(PathUtil.toRelativeFile(contentFile, contentBase));
		
		return (Boolean)((Map<String, Object>) meta
				.orElse(Map.of())
				.getOrDefault("search", Map.of()))
				.getOrDefault("index", Boolean.TRUE);
	}
	
	private boolean noindex (final Document document) {
		Element meta = document.selectFirst("head meta[name='robots']");
		
		if (meta != null) {
			var content = meta.attr("content");
			if (!Strings.isNullOrEmpty(content)) {
				return content.toLowerCase().contains("noindex");
			}
		}
		
		return false;
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (dir.getFileName().toString().startsWith(".")) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		return FileVisitResult.CONTINUE;
	}

	private Optional<ContentResponse> getContent(Path path) throws IOException {
		var uri = "/" + PathUtil.toRelativeFile(path, contentBase);

		uri = uri.substring(0, uri.lastIndexOf("."));
		
		return moduleContext.getRenderContentFunction().apply(uri, Collections.emptyMap());
	}

}
