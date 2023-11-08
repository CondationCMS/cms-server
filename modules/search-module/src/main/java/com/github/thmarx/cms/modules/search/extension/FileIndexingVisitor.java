package com.github.thmarx.cms.modules.search.extension;

/*-
 * #%L
 * search-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.github.thmarx.cms.api.CMSModuleContext;
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
			var uri = PathUtil.toURI(file, contentBase);
			var content = getContent(file);

			if (content.isPresent()) {
				final Document parsedContent = Jsoup.parse(content.get());
				
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

	private Optional<String> getContent(Path path) throws IOException {
		var uri = "/" + PathUtil.toRelativeFile(path, contentBase);

		uri = uri.substring(0, uri.lastIndexOf("."));
		
		return moduleContext.getRenderContentFunction().apply(uri, Collections.emptyMap());
	}

}
