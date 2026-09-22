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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NodeAlternatesTest {

	@AfterEach
	void clearServices() {
		ServiceRegistry.getInstance().clear();
	}

	@Test
	void exposesCurrentAndLinkedSitesWithAbsoluteUrls() {
		var current = descriptor("demo-site", Locale.US, "/");
		var german = descriptor("demo-de-site", Locale.GERMANY, "/de");
		var sites = mock(SiteService.class);
		when(sites.get("demo-site")).thenReturn(Optional.of(current));
		when(sites.get("demo-de-site")).thenReturn(Optional.of(german));

		registerLinkService("demo-site", "/about", "https://example.test/about");
		registerLinkService("demo-de-site", "/ueber", "https://example.test/de/ueber");

		var properties = mock(SiteProperties.class);
		when(properties.id()).thenReturn("demo-site");
		var node = new ContentNode(
				"about.md",
				"/about",
				"about.md",
				Map.of(Constants.MetaFields.ALTERNATES, Map.of("demo-de-site", "/ueber")));

		var entries = new NodeAlternates(node, properties, sites).entries();
		Assertions.assertThat(entries)
				.extracting(
						NodeAlternates.AlternateDto::site,
						NodeAlternates.AlternateDto::locale,
						NodeAlternates.AlternateDto::current,
						NodeAlternates.AlternateDto::url)
				.containsExactly(
						Assertions.tuple("demo-site", "en-US", true, "https://example.test/about"),
						Assertions.tuple("demo-de-site", "de-DE", false, "https://example.test/de/ueber"));
		Assertions.assertThat(entries)
				.extracting(NodeAlternates.AlternateDto::language, NodeAlternates.AlternateDto::country)
				.containsExactly(
						Assertions.tuple("en", "us"),
						Assertions.tuple("de", "de"));
	}

	private SiteDescriptor descriptor(String id, Locale locale, String contextPath) {
		return new SiteDescriptor(
				id, "demo", locale, "https://example.test", contextPath,
				List.of("example.test"), List.of(), true, Map.of());
	}

	private void registerLinkService(String siteId, String uri, String absoluteUrl) {
		var links = mock(SiteLinkService.class);
		when(links.absoluteLink(uri)).thenReturn(absoluteUrl);
		ServiceRegistry.getInstance().register(siteId, SiteLinkService.class, links);
	}
}
