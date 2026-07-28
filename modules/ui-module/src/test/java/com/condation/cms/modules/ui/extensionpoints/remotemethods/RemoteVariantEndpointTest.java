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

import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.content.VariantResolver;
import com.condation.cms.modules.ui.extensionpoints.remotemethods.dto.VariantDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
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
	private VariantResolver variantResolver;

	private RemoteVariantEndpoint endpoint;

	@BeforeEach
	void setUp() {
		endpoint = new RemoteVariantEndpoint() {
			@Override
			protected VariantResolver getVariantResolver(DB db) {
				return variantResolver;
			}
		};
		endpoint.setContext(moduleContext);
		lenient().when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		lenient().when(db.getContent()).thenReturn(content);
	}

	@Test
	void getReturnsVariantsSortedById() throws RPCException {
		var node = node("about.md", "/about", Map.of("title", "About"));
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
		when(variantResolver.getVariants(node)).thenReturn(List.of(
				new VariantResolver.Variant("summer", summerNode),
				new VariantResolver.Variant("campaign", campaignNode)
		));

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoint.get(Map.of("uri", "about.md"));

		assertThat(result).containsEntry("uri", "about.md");
		assertThat((List<VariantDto>) result.get("variants"))
				.extracting(VariantDto::id)
				.containsExactly("campaign", "summer");
	}

	@Test
	void getReturnsEmptyListWhenNodeHasNoVariants() throws RPCException {
		var node = node("about.md", "/about", Map.of());
		when(content.byPath("about.md")).thenReturn(Optional.of(node));
		when(variantResolver.getVariants(node)).thenReturn(List.of());

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

	private ContentNode node(String uri, String url, Map<String, Object> data) {
		return new ContentNode(uri, url, "about.md", data);
	}
}
