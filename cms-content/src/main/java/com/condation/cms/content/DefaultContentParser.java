package com.condation.cms.content;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class DefaultContentParser implements ContentParser {

	public DefaultContentParser() {
	}

	@Override
	public void clearCache() {

	}

	@Override
	public Content parse(final ReadOnlyFile contentFile) throws IOException {
		ContentRecord readContent = readContent(contentFile);

		return new Content(readContent.content(), _parseMeta(readContent));
	}

	private Map<String, Object> _parseMeta(ContentRecord content) {
		if (Strings.isNullOrEmpty(content.meta().trim())) {
			return Collections.emptyMap();
		}
		try {
			return new Yaml().load(content.meta().trim());
		} catch (Exception e) {
			log.error("error parsing yaml: " + content.meta(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Object> parseMeta(final ReadOnlyFile contentFile) throws IOException {
		ContentRecord readContent = readContent(contentFile);

		return _parseMeta(readContent);
	}

	private ContentRecord readContent(final ReadOnlyFile contentFile) throws IOException {
		var fileContent = contentFile.getAllLines();

		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder metaBuilder = new StringBuilder();

		boolean inFrontMatter = false;
		boolean frontMatterClosed = false;

		for (String line : fileContent) {
			if (line.trim().equals("---") && !frontMatterClosed) {
				if (!inFrontMatter) {
					inFrontMatter = true; // Start Frontmatter
					continue;
				} else if (!frontMatterClosed) {
					frontMatterClosed = true; // Ende Frontmatter
					inFrontMatter = false;
					continue;
				}
			}

			if (inFrontMatter) {
				metaBuilder.append(line).append("\r\n");
			} else {
				contentBuilder.append(line).append("\r\n");
			}
		}

		return new ContentRecord(contentBuilder.toString(), metaBuilder.toString());
	}
}
