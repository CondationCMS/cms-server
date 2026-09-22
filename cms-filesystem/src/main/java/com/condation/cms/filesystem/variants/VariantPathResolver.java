package com.condation.cms.filesystem.variants;

/*-
 * #%L
 * CMS FileSystem
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
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves the filesystem location encoded in a variant content path.
 */
public final class VariantPathResolver {

	private static final String VARIANTS_DIRECTORY = ".variants";

	private VariantPathResolver() {
	}

	public static Optional<VariantLocation> resolve(String path) {
		Objects.requireNonNull(path, "path");

		var pathParts = normalize(path).split(Constants.PATH_SEPARATOR);
		for (int index = 0; index < pathParts.length; index++) {
			if (!VARIANTS_DIRECTORY.equals(pathParts[index])) {
				continue;
			}
			if (index + 3 != pathParts.length - 1) {
				return Optional.empty();
			}

			var pageFolder = pathParts[index + 1];
			var variantId = pathParts[index + 2];
			var fileName = pathParts[index + 3];
			if (!pageFolder.equals(removeMarkdown(fileName))) {
				return Optional.empty();
			}

			var canonicalPath = index == 0
					? fileName
					: String.join(Constants.PATH_SEPARATOR, java.util.Arrays.copyOf(pathParts, index))
							+ Constants.PATH_SEPARATOR + fileName;
			return Optional.of(new VariantLocation(canonicalPath, variantId));
		}
		return Optional.empty();
	}

	private static String normalize(String path) {
		var normalized = path.replace('\\', '/');
		while (normalized.startsWith(Constants.PATH_SEPARATOR)) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}

	private static String removeMarkdown(String filename) {
		return filename.endsWith(".md")
				? filename.substring(0, filename.length() - 3)
				: filename;
	}

	public record VariantLocation(String canonicalPath, String variantId) {
	}
}
