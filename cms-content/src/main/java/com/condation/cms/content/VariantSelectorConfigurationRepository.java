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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Loads and stores the selector configured for a canonical page.
 */
@Slf4j
@RequiredArgsConstructor
public class VariantSelectorConfigurationRepository {

	public static final String DEFAULT_SELECTOR_ID = "date-range";
	public static final String CONFIG_FILE_NAME = "variants.yaml";
	private static final String SELECTOR_PROPERTY = "selector";

	private final DB db;
	private final VariantResolver variantResolver;

	public String getSelectorId(ContentNode node) {
		var configurationFile = configurationFile(node);
		if (!Files.exists(configurationFile)) {
			return DEFAULT_SELECTOR_ID;
		}

		try {
			var yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
			var data = yaml.load(Files.readString(configurationFile));
			if (data instanceof Map<?, ?> map
					&& map.get(SELECTOR_PROPERTY) instanceof String selector
					&& !selector.isBlank()) {
				return selector.trim();
			}
		} catch (Exception exception) {
			log.error("Could not read variant selector configuration '{}'", configurationFile, exception);
		}
		return DEFAULT_SELECTOR_ID;
	}

	public void setSelectorId(ContentNode node, String selectorId) throws IOException {
		if (selectorId == null || selectorId.isBlank()) {
			throw new IllegalArgumentException("selectorId must not be blank");
		}
		var configurationFile = configurationFile(node);
		Files.createDirectories(configurationFile.getParent());
		YamlHeaderUpdater.saveMetaData(
				configurationFile,
				Map.of(SELECTOR_PROPERTY, selectorId.trim())
		);
	}

	public Path configurationFile(ContentNode node) {
		var canonical = variantResolver.resolveContext(node).canonical();
		var contentBase = db.getFileSystem().resolve(Constants.Folders.CONTENT);
		var canonicalFile = contentBase.resolve(canonical.path());
		var fileName = canonicalFile.getFileName().toString();
		var pageName = fileName.endsWith(".md")
				? fileName.substring(0, fileName.length() - 3)
				: fileName;
		return canonicalFile.getParent()
				.resolve(".variants")
				.resolve(pageName)
				.resolve(CONFIG_FILE_NAME);
	}
}
