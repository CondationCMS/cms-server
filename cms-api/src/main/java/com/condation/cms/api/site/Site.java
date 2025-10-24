package com.condation.cms.api.site;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.SiteProperties;
import com.google.inject.Injector;
import java.util.List;

/**
 *
 * @author thmar
 */
public record Site(Injector injector) {

	public String id() {
		return injector.getInstance(SiteProperties.class).id();
	}

	public List<String> modules() {
		return injector.getInstance(SiteProperties.class).activeModules();
	}

	public String baseurl() {
		return (String) injector.getInstance(SiteProperties.class).get("baseurl");
	}

	public boolean manager() {
		return injector.getInstance(SiteProperties.class).ui().managerEnabled();
	}

	public String realUrl() {
		var baseUrl = baseurl();
		var contextPath = injector.getInstance(SiteProperties.class).contextPath();

		// Normalize baseUrl: remove trailing slashes
		String normalizedBase = baseUrl.replaceAll("/+$", "");

		// Normalize contextPath: ensure it starts with a slash (except if it's just "/")
		String normalizedContext = contextPath.equals("/") ? "" : contextPath.replaceAll("^/+", "");

		// Combine
		if (normalizedContext.isEmpty()) {
			return normalizedBase + "/";
		} else {
			return normalizedBase + "/" + normalizedContext + "/";
		}
	}

}
