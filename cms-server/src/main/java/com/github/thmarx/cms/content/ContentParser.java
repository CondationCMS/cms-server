package com.github.thmarx.cms.content;

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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.Startup;
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
public class ContentParser {

	private final Cache<String, Content> contentCache;

	public ContentParser() {
		var builder = Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(1));
		if (Startup.DEV_MODE) {
			builder.maximumSize(0);
		}
		contentCache = builder.build();
	}

	public void clearCache() {
		contentCache.invalidateAll();
	}

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
        if (Strings.isNullOrEmpty(content.meta.trim())) {
            return Collections.emptyMap();
        }
		try {
			return new Yaml().load(content.meta.trim());
		} catch (Exception e) {
			log.error("error parsing yaml: " + content.meta, e);
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

	private record ContentRecord(String content, String meta) {}

	public record Content(String content, Map<String, Object> meta) {}
}
