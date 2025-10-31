package com.condation.cms.core.serivce.impl;

/*-
 * #%L
 * cms-core
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

import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.core.serivce.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author thmar
 */
public class SiteLinkService implements Service {
	
	private final Configuration configuration;
	
	public SiteLinkService (final Configuration configuration) {
		this.configuration = configuration;
	}
	
	public String managerDeepLink (String url) {
		var siteProperties = configuration.get(SiteConfiguration.class).siteProperties();
		url = HTTPUtil.modifyUrl(url, siteProperties);
		String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8);
		var deepLink = HTTPUtil.modifyUrl("/manager/index.html?page=%s".formatted(encodedUrl), siteProperties);
	
		var baseUrl = siteProperties.baseUrl();
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		
		return baseUrl + deepLink;
	}
	
	public String link (String url) {
		var siteProperties = configuration.get(SiteConfiguration.class).siteProperties();
		return HTTPUtil.modifyUrl(url, siteProperties);
	}
}
