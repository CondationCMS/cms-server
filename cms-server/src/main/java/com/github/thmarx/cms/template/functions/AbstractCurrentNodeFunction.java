package com.github.thmarx.cms.template.functions;

/*-
 * #%L
 * cms-server
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

import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCurrentNodeFunction {

	protected final FileSystem fileSystem;
	protected final Path currentNode;
	protected final ContentParser contentParser;
	protected final MarkdownRenderer markdownRenderer;

	protected String getUrl(Path node) {
		StringBuilder sb = new StringBuilder();

		while (node != null && !node.equals(fileSystem.resolve("content/"))) {

			var filename = node.getFileName().toString();
			if (!filename.equals("index.md")) {
				if (filename.endsWith(".md")) {
					filename = filename.substring(0, filename.length() - 3);
				}
				sb.insert(0, filename);
				sb.insert(0, "/");
			}
			node = node.getParent();
		}

		var url = sb.toString();

		return "".equals(url) ? "/" : url;
	}

	protected Optional<ContentParser.Content> parse(Path node) {
		try {
			//Path rel = contentBase.relativize(node);
			if (Files.isDirectory(node)) {
				node = node.resolve("index.md");
			}
			var md = contentParser.parse(node);

			return Optional.of(md);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return Optional.empty();
	}
}
