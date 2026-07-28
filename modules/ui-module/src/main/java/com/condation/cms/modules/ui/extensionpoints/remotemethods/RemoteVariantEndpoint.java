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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.content.VariantResolver;
import com.condation.cms.modules.ui.extensionpoints.remotemethods.dto.VariantDto;
import com.condation.modules.api.annotation.Extension;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remote methods for loading the variants of a content node.
 *
 * @author thorstenmarx
 */
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteVariantEndpoint extends AbstractRemoteMethodeExtension {

	@RemoteMethod(name = "variants.get", permissions = {Permissions.CONTENT_EDIT})
	public Object get(Map<String, Object> parameters) throws RPCException {
		var uri = (String) parameters.getOrDefault("uri", "");
		if (uri.isBlank()) {
			throw new RPCException(400, "uri must not be blank");
		}

		var db = getDB(parameters);
		var contentNode = findContentNode(db, uri);
		var variants = getVariantResolver(db)
				.getVariants(contentNode)
				.stream()
				.sorted(Comparator.comparing(VariantResolver.Variant::id))
				.map(variant -> new VariantDto(
						variant.id(),
						variant.node().uri(),
						variant.node().url(),
						variant.node().data()
				))
				.toList();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("uri", contentNode.uri());
		result.put("variants", variants);
		return result;
	}

	private ContentNode findContentNode(DB db, String uri) throws RPCException {
		return db.getContent()
				.byPath(uri)
				.or(() -> db.getContent().byUrl(uri))
				.orElseThrow(() -> new RPCException(
						404,
						"content node for uri %s not found".formatted(uri)
				));
	}

	protected VariantResolver getVariantResolver(DB db) {
		return getContext().get(InjectorFeature.class).injector().getInstance(VariantResolver.class);
	}
}
