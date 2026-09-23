package com.condation.cms.api.site;

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

import com.condation.cms.api.SiteProperties;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Public, runtime-independent description of a configured site.
 */
public record SiteDescriptor(
		String id,
		String group,
		Locale locale,
		String baseUrl,
		String contextPath,
		List<String> hostnames,
		List<String> modules,
		boolean manager,
		Map<String, Object> attributes) {

	public SiteDescriptor {
		group = group == null ? "" : group;
		hostnames = hostnames == null ? List.of() : List.copyOf(hostnames);
		modules = modules == null ? List.of() : List.copyOf(modules);
		attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
	}

	public static SiteDescriptor from(SiteProperties properties) {
		var multisite = properties.multisite();
		return new SiteDescriptor(
				properties.id(),
				multisite.group(),
				properties.locale(),
				properties.baseUrl(),
				properties.contextPath(),
				properties.hostnames(),
				properties.activeModules(),
				properties.ui().managerEnabled(),
				multisite.attributes());
	}

	public boolean grouped() {
		return !group.isBlank();
	}

	public String realUrl() {
		String normalizedBase = withoutTrailingSlashes(baseUrl);
		String normalizedContext = contextPath.equals("/") ? "" : withoutLeadingSlashes(contextPath);

		if (normalizedContext.isEmpty()) {
			return normalizedBase + "/";
		}
		return normalizedBase + "/" + normalizedContext + "/";
	}

	private static String withoutTrailingSlashes(String value) {
		int end = value.length();
		while (end > 0 && value.charAt(end - 1) == '/') {
			end--;
		}
		return value.substring(0, end);
	}

	private static String withoutLeadingSlashes(String value) {
		int start = 0;
		while (start < value.length() && value.charAt(start) == '/') {
			start++;
		}
		return value.substring(start);
	}
}
