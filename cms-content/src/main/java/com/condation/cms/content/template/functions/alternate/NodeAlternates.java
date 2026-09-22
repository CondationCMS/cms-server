package com.condation.cms.content.template.functions.alternate;

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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.site.SiteDescriptor;
import com.condation.cms.api.site.SiteService;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteLinkService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Template projection of the current content node and its cross-site alternates. */
public class NodeAlternates {

	private final ContentNode node;
	private final SiteProperties siteProperties;
	private final SiteService siteService;

	public NodeAlternates(ContentNode node, SiteProperties siteProperties, SiteService siteService) {
		this.node = node;
		this.siteProperties = siteProperties;
		this.siteService = siteService;
	}

	public List<AlternateDto> entries() {
		if (node == null) {
			return List.of();
		}

		List<AlternateDto> result = new ArrayList<>();
		var current = siteService.get(siteProperties.id()).orElseGet(() -> SiteDescriptor.from(siteProperties));
		absoluteLink(current.id(), node.url()).ifPresent(url -> result.add(toDto(current, true, url)));

		var value = node.data().get(Constants.MetaFields.ALTERNATES);
		if (!(value instanceof Map<?, ?> alternates)) {
			return List.copyOf(result);
		}

		alternates.forEach((siteId, uri) -> siteService.get(String.valueOf(siteId))
				.flatMap(site -> absoluteLink(site.id(), String.valueOf(uri))
						.map(url -> toDto(site, false, url)))
				.ifPresent(result::add));
		return List.copyOf(result);
	}

	private java.util.Optional<String> absoluteLink(String siteId, String uri) {
		return ServiceRegistry.getInstance().get(siteId, SiteLinkService.class)
				.map(service -> service.absoluteLink(uri));
	}

	private AlternateDto toDto(SiteDescriptor site, boolean current, String url) {
		return new AlternateDto(site.id(), site.locale().toLanguageTag(), current, url);
	}

	public record AlternateDto(String site, String locale, boolean current, String url) {

		public String language() {
			return Locale.forLanguageTag(locale).getLanguage();
		}

		public String country() {
			return Locale.forLanguageTag(locale).getCountry().toLowerCase(Locale.ROOT);
		}
	}
}
