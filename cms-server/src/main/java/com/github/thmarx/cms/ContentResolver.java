package com.github.thmarx.cms;

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

import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.api.utils.PathUtil;
import com.google.common.base.Strings;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ContentResolver {

	private final Path contentBase;

	private final ContentRenderer contentRenderer;
	
	private final FileSystem fileSystem;
	
	public Optional<String> getContent(final RequestContext context) {
		String path;
		if (Strings.isNullOrEmpty(context.uri())) {
			path = "";
		} else {
			// remove leading slash
			path = context.uri().substring(1);
		}
		

		var contentPath = contentBase.resolve(path);
		Path contentFile = null;
		if (Files.exists(contentPath) && Files.isDirectory(contentPath)) {
			// use index.md
			var tempFile = contentPath.resolve("index.md");
			if (Files.exists(tempFile)) {
				contentFile = tempFile;
			}
		} else {
			var temp = contentBase.resolve(path + ".md");
			if (Files.exists(temp)) {
				contentFile = temp;
			} else {
				return Optional.empty();
			}
		}
		
		var uri = PathUtil.toRelativeFile(contentFile, contentBase);
		if (!fileSystem.isVisible(uri)) {
			return Optional.empty();
		}
		
		try {
			
			List<MetaData.MetaNode> sections = fileSystem.listSections(contentFile);
			
			Map<String, List<ContentRenderer.Section>> renderedSections = contentRenderer.renderSections(sections, context);
			
			var content = contentRenderer.render(contentFile, context, renderedSections);
			return Optional.of(content);
		} catch (Exception ex) {
			log.error(null, ex);
			return Optional.empty();
		}
	}
}
