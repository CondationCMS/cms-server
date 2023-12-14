package com.github.thmarx.cms.content;

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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.api.ServerContext;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ContentParser implements com.github.thmarx.cms.api.content.ContentParser{

	private final Cache<String, Content> contentCache;

	public ContentParser() {
		var builder = Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(1));
		if (ServerContext.IS_DEV) {
			builder.maximumSize(0);
		}
		contentCache = builder.build();
	}

	public void clearCache() {
		contentCache.invalidateAll();
	}

	@Override
	public Content parse(final Path contentFile) throws IOException {
		final String filename = contentFile.toAbsolutePath().toString();
		var cached = contentCache.getIfPresent(filename);
		if (cached != null) {
			return cached;
		}
		var object = _parse(contentFile);
		contentCache.put(filename, object);
		return object;
	}

	private Content _parse(final Path contentFile) throws IOException {
		ContentRecord readContent = readContent(contentFile);

		return new Content(readContent.content(), _parseMeta(readContent));
	}
    
    private Map<String, Object> _parseMeta (ContentRecord content) {
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

	public Map<String, Object> parseMeta(final Path contentFile) throws IOException {
		ContentRecord readContent = readContent(contentFile);

		return _parseMeta(readContent);
	}

	private ContentRecord readContent(final Path contentFile) throws IOException {
		var fileContent = Files.readAllLines(contentFile, StandardCharsets.UTF_8);

		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder metaBuilder = new StringBuilder();

		AtomicBoolean inFrontMatter = new AtomicBoolean(true);
		AtomicInteger counter = new AtomicInteger(0);
		fileContent.stream().forEach((line) -> {
			if (line.trim().equals("---")) {
				counter.incrementAndGet();
				if (counter.get() == 2) {
					inFrontMatter.set(false);
				}
				return;
			}			
			if (inFrontMatter.get()) {
				metaBuilder.append(line).append("\r\n");
			} else {
				contentBuilder.append(line).append("\r\n");
			}
		});
		
		return new ContentRecord(contentBuilder.toString(), metaBuilder.toString());
	}
}
