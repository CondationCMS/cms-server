package com.condation.cms.api.configuration.configs;

/*-
 * #%L
 * CMS Api
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Configuration of one named collection.
 */
public record CollectionDefinition(String name, String site, CollectionDetailConfiguration detail) {

	private static final Pattern VALID_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_-]*");

	public CollectionDefinition {
		Objects.requireNonNull(name, "collection name must not be null");
		if (!VALID_NAME_PATTERN.matcher(name).matches()) {
			throw new IllegalArgumentException("invalid collection name: " + name);
		}
		if (site != null) {
			site = site.trim();
			if (site.isEmpty()) {
				throw new IllegalArgumentException("collection site must not be blank");
			}
		}
	}

	public CollectionDefinition(String name, CollectionDetailConfiguration detail) {
		this(name, null, detail);
	}

	public Optional<CollectionDetailConfiguration> detailPage() {
		return Optional.ofNullable(detail);
	}

	/** Site whose collection data should be used, or empty for a local collection. */
	public Optional<String> sourceSite() {
		return Optional.ofNullable(site);
	}
}
