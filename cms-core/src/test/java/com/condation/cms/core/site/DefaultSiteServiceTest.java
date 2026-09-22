package com.condation.cms.core.site;

/*-
 * #%L
 * CMS Core
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

import com.condation.cms.api.MultisiteProperties;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.site.Site;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultSiteServiceTest {

	@Test
	void resolvesSitesAndRelatedSitesFromMultisiteGroup() {
		var service = new DefaultSiteService();
		service.add(site("demo", "demo", Locale.US));
		service.add(site("demo-de", "demo", Locale.GERMANY));
		service.add(site("standalone", "", Locale.FRANCE));

		Assertions.assertThat(service.get("demo-de"))
				.isPresent()
				.get()
				.extracting(site -> site.locale().toLanguageTag())
				.isEqualTo("de-DE");
		Assertions.assertThat(service.sitesInGroup("demo").map(site -> site.id()).toList())
				.containsExactly("demo", "demo-de");
		Assertions.assertThat(service.relatedSites("demo").map(site -> site.id()).toList())
				.containsExactly("demo-de");
		Assertions.assertThat(service.relatedSites("standalone")).isEmpty();
		Assertions.assertThat(service.relatedSites("missing")).isEmpty();
	}

	private Site site(String id, String group, Locale locale) {
		SiteProperties properties = new StubSiteProperties(id, group, locale);
		var injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(SiteProperties.class).toInstance(properties);
			}
		});
		return new Site(injector);
	}

	private record StubSiteProperties(String id, String group, Locale locale) implements SiteProperties {

		@Override public List<String> hostnames() { return List.of("localhost"); }
		@Override public String markdownEngine() { return "system"; }
		@Override public String contextPath() { return "/"; }
		@Override public String baseUrl() { return "https://example.test"; }
		@Override public Object get(String field) { return null; }
		@Override public <T> T getOrDefault(String field, T defaultValue) { return defaultValue; }
		@Override public String theme() { return "demo"; }
		@Override public String queryIndexMode() { return "MEMORY"; }
		@Override public String language() { return locale.getLanguage(); }
		@Override public String defaultContentType() { return "text/markdown"; }
		@Override public List<String> contentPipeline() { return List.of(); }
		@Override public String cacheEngine() { return "default"; }
		@Override public String templateEngine() { return "system"; }
		@Override public List<String> activeModules() { return List.of(); }
		@Override public com.condation.cms.api.UIProperties ui() {
			return new com.condation.cms.api.UIProperties() {
				@Override public boolean force2fa() { return false; }
				@Override public boolean managerEnabled() { return true; }
			};
		}
		@Override public MultisiteProperties multisite() {
			return new MultisiteProperties(group, Map.of());
		}
	}
}
