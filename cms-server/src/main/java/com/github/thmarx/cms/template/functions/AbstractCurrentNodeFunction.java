package com.github.thmarx.cms.template.functions;

/*-
 * #%L
 * cms-server
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
