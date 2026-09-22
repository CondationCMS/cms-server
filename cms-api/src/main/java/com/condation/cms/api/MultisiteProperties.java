package com.condation.cms.api;

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

import java.util.Map;

/**
 * Configuration that associates a site with a multisite group.
 *
 * @param group the group identifier, or an empty string for an independent site
 * @param attributes optional dimensions such as market or brand
 */
public record MultisiteProperties(String group, Map<String, Object> attributes) {

	public MultisiteProperties {
		group = group == null ? "" : group;
		attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static MultisiteProperties empty() {
		return new MultisiteProperties("", Map.of());
	}

	public boolean grouped() {
		return !group.isBlank();
	}
}
