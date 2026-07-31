package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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
import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.variants.Variant;
import com.condation.cms.content.ConfigurableVariantSelector;
import com.condation.cms.content.VariantResolver;
import com.condation.cms.content.VariantSelectorConfigurationRepository;
import com.condation.cms.modules.ui.extensionpoints.remotemethods.dto.VariantDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteVariantEndpointTest {

	@Mock
	private SiteModuleContext moduleContext;

	@Mock
	private DB db;

	@Mock
	private Content content;

	@Mock
	private DBFileSystem fileSystem;

	@Mock
	private EventBus eventBus;

	@Mock
	private VariantResolver variantResolver;

	@Mock
	private ConfigurableVariantSelector configurableVariantSelector;

	@Mock
	private VariantSelectorConfigurationRepository selectorConfigurationRepository;

	private RemoteVariantEndpoint endpoint;

	@TempDir
	private Path tempDir;

	@BeforeEach
	void setUp() {
		endpoint = new RemoteVariantEndpoint() {
			@Override
			protected VariantResolver getVariantResolver(DB db) {
				return variantResolver;
			}

			@Override
			protected ConfigurableVariantSelector getConfigurableVariantSelector() {
				return configurableVariantSelector;
			}

			@Override
			protected VariantSelectorConfigurationRepository getVariantSelectorConfigurationRepository() {
				return selectorConfigurationRepository;
			}
		};
		endpoint.setContext(moduleContext);
		lenient().when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		lenient().when(moduleContext.get(EventBusFeature.class)).thenReturn(new EventBusFeature(eventBus));
		lenient().when(db.getContent()).thenReturn(content);
		lenient().when(db.getFileSystem()).thenReturn(fileSystem);
		lenient().when(fileSystem.resolve(Constants.Folders.CONTENT)).thenReturn(tempDir);
	}

	@Test
	void getReturnsVariantsSortedById() throws RPCException {
		var node = node("about.md", "/about", Map.of(
				"title", "About",
				"template", "page.html"
		));
		var summerNode = node(
				".variants/about/summer/about.md",
				"/.variants/about/summer/about",
				Map.of("title", "Summer")
		);
		var campaignNode = node(
				".variants/about/campaign/about.md",
				"/.variants/about/campaign/about",
				Map.of("title", "Campaign")
		);
		when(content.byPath("about.md")).thenReturn(Optional.of(node));
		var variants = List.of(
				new Variant("summer", summerNode),
				new Variant("campaign", campaignNode)
		);
		when(variantResolver.resolveContext(node)).thenReturn(
				new VariantResolver.VariantContext(node, Optional.empty(), variants)
		);

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoint.get(Map.of("uri", "about.md"));

		assertThat(result).containsEntry("uri", "about.md");
		assertThat((List<VariantDto>) result.get("variants"))
				.extracting(VariantDto::id)
				.containsExactly("campaign", "summer");
		assertThat((List<VariantDto>) result.get("variants"))
				.extracting(VariantDto::url)
				.containsExactly(
						"/about?preview=manager&variant=campaign",
						"/about?preview=manager&variant=summer"
				);
		assertThat(result).containsEntry("activeVariantId", null);
		assertThat((Map<String, Object>) result.get("canonical"))
				.containsEntry("template", "page.html");
	}

	@Test
	void getFromVariantReturnsCanonicalAndActiveVariant() throws RPCException {
		var canonical = node("about.md", "/about", Map.of("title", "About"));
		var summer = node(
				".variants/about/summer/about.md",
				"/.variants/about/summer/about",
				Map.of("title", "Summer")
		);
		var variants = List.of(new Variant("summer", summer));
		when(content.byPath(summer.path())).thenReturn(Optional.of(summer));
		when(variantResolver.resolveContext(summer)).thenReturn(
				new VariantResolver.VariantContext(canonical, Optional.of("summer"), variants)
		);

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoint.get(Map.of("uri", summer.path()));

		assertThat(result)
				.containsEntry("uri", "about.md")
				.containsEntry("activeVariantId", "summer");
		assertThat((Map<String, Object>) result.get("canonical"))
				.containsEntry("url", "/about?preview=manager");
	}

	@Test
	void getReturnsEmptyListWhenNodeHasNoVariants() throws RPCException {
		var node = node("about.md", "/about", Map.of());
		when(content.byPath("about.md")).thenReturn(Optional.of(node));
		when(variantResolver.resolveContext(node)).thenReturn(
				new VariantResolver.VariantContext(node, Optional.empty(), List.of())
		);

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoint.get(Map.of("uri", "about.md"));

		assertThat(result).containsEntry("variants", List.of());
	}

	@Test
	void getRejectsBlankUri() {
		assertThatThrownBy(() -> endpoint.get(Map.of()))
				.isInstanceOf(RPCException.class)
				.satisfies(exception ->
						assertThat(((RPCException) exception).getCode()).isEqualTo(400)
				);
	}

	@Test
	void getReturnsNotFoundForUnknownNode() {
		when(content.byPath("missing.md")).thenReturn(Optional.empty());
		when(content.byUrl("missing.md")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> endpoint.get(Map.of("uri", "missing.md")))
				.isInstanceOf(RPCException.class)
				.satisfies(exception ->
						assertThat(((RPCException) exception).getCode()).isEqualTo(404)
				);
	}

	@Test
	void getSelectorsReturnsConfigurationAndAvailableStrategies() throws RPCException {
		var node = node("about.md", "/about", Map.of());
		when(content.byPath("about.md")).thenReturn(Optional.of(node));
		when(selectorConfigurationRepository.getSelectorId(node)).thenReturn("audience");
		when(configurableVariantSelector.availableSelectors()).thenReturn(Map.of(
				"date-range",
				new ConfigurableVariantSelector.SelectorDescriptor("date-range", "Date range"),
				"audience",
				new ConfigurableVariantSelector.SelectorDescriptor("audience", "Audience")
		));

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoint.getSelectors(Map.of("uri", "about.md"));

		assertThat(result).containsEntry("selector", "audience");
		assertThat((List<ConfigurableVariantSelector.SelectorDescriptor>) result.get("selectors"))
				.extracting(ConfigurableVariantSelector.SelectorDescriptor::id)
				.containsExactly("audience", "date-range");
	}

	@Test
	void setSelectorPersistsConfigurationForPage() throws Exception {
		var node = node("about.md", "/about", Map.of());
		when(content.byPath("about.md")).thenReturn(Optional.of(node));
		when(configurableVariantSelector.hasSelector("audience")).thenReturn(true);

		var result = endpoint.setSelector(Map.of("uri", "about.md", "selector", "audience"));

		assertThat(result).isEqualTo(Map.of("selector", "audience"));
		verify(selectorConfigurationRepository).setSelectorId(node, "audience");
	}

	@Test
	void deleteRemovesVariantAndSectionsButKeepsPageConfiguration() throws Exception {
		var canonical = node("about.md", "/about", Map.of("title", "About"));
		var summer = node(
				".variants/about/summer/about.md",
				"/.variants/about/summer/about",
				Map.of("title", "Summer")
		);
		var variantFolder = tempDir.resolve(".variants/about/summer");
		Files.createDirectories(variantFolder);
		Files.writeString(variantFolder.resolve("about.md"), "variant");
		Files.writeString(variantFolder.resolve("about.main.hero.md"), "section");
		var configuration = tempDir.resolve(".variants/about/variants.yaml");
		Files.writeString(configuration, "selector: date-range");
		when(content.byPath(canonical.path())).thenReturn(Optional.of(canonical));
		when(variantResolver.resolveContext(canonical)).thenReturn(
				new VariantResolver.VariantContext(
						canonical,
						Optional.empty(),
						List.of(new Variant("summer", summer))
				)
		);

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoint.delete(Map.of(
				"uri", canonical.path(),
				"id", "summer"
		));

		assertThat(result)
				.containsEntry("id", "summer")
				.containsEntry("url", "/about?preview=manager");
		assertThat(variantFolder).doesNotExist();
		assertThat(configuration).exists();
		verify(fileSystem).flushContentChanges();
	}

	@Test
	void deleteRejectsUnknownVariant() {
		var canonical = node("about.md", "/about", Map.of());
		when(content.byPath(canonical.path())).thenReturn(Optional.of(canonical));
		when(variantResolver.resolveContext(canonical)).thenReturn(
				new VariantResolver.VariantContext(canonical, Optional.empty(), List.of())
		);

		assertThatThrownBy(() -> endpoint.delete(Map.of(
				"uri", canonical.path(),
				"id", "missing"
		)))
				.isInstanceOf(RPCException.class)
				.satisfies(exception ->
						assertThat(((RPCException) exception).getCode()).isEqualTo(404)
				);
	}

	private ContentNode node(String uri, String url, Map<String, Object> data) {
		return new ContentNode(uri, url, "about.md", data);
	}
}
