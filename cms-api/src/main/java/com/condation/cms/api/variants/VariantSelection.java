package com.condation.cms.api.variants;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Optional;

/**
 * Describes the result and origin of a variant selection.
 */
public record VariantSelection(Optional<Variant> variant, Source source) {

	public static VariantSelection canonical() {
		return new VariantSelection(Optional.empty(), Source.CANONICAL);
	}

	public static VariantSelection preview(Variant variant) {
		return new VariantSelection(Optional.of(variant), Source.PREVIEW);
	}

	public static VariantSelection automatic(Variant variant) {
		return new VariantSelection(Optional.of(variant), Source.AUTOMATIC);
	}

	public enum Source {
		CANONICAL,
		PREVIEW,
		AUTOMATIC
	}
}
