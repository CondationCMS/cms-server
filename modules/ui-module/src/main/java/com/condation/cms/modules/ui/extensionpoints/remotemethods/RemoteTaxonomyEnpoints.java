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
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.taxonomy.Value;
import com.condation.cms.api.eventbus.events.ReloadTaxonomyConfig;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.modules.ui.utils.TaxonomyYamlWriter;
import com.condation.cms.modules.ui.utils.UIPathUtil;
import com.condation.modules.api.annotation.Extension;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteTaxonomyEnpoints extends AbstractRemoteMethodeExtension {

	private final TaxonomyYamlWriter taxonomyYamlWriter = new TaxonomyYamlWriter();

	@RemoteMethod(name = "taxonomy.get", permissions = {Permissions.CONTENT_EDIT})
	public Object get(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);
		Map<String, Object> result = new HashMap<>();

		db.getTaxonomies().all().forEach(tax -> {
			result.put(tax.getSlug(), tax.getTitle());
		});

		return result;
	}

	@RemoteMethod(name = "taxonomy.values", permissions = {Permissions.CONTENT_EDIT})
	public Object remove(Map<String, Object> parameters) {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();

		var slug = (String) parameters.get("slug");

		var taxonomy = db.getTaxonomies().forSlug(slug);

		taxonomy.ifPresent(tax -> {
			tax.getValues().forEach((key, val) -> {
				Map<String, String> entry = new HashMap<>();
				entry.put("id", val.getId());
				entry.put("title", val.getTitle());
				result.put(key, entry);
			});
		});

		return result;
	}

	@RemoteMethod(name = "taxonomy.value.create", permissions = {Permissions.CONTENT_EDIT})
	public Object createValue(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		try {
			var slug = ((String) parameters.getOrDefault("slug", "")).trim();
			var title = ((String) parameters.getOrDefault("title", "")).trim();

			if (slug.isBlank()) {
				throw new RPCException(1, "taxonomy slug is required");
			}
			if (title.isBlank()) {
				throw new RPCException(1, "taxonomy value title is required");
			}

			var taxonomy = db.getTaxonomies().forSlug(slug)
					.orElseThrow(() -> new RPCException(1, "taxonomy not found: " + slug));
			var id = UIPathUtil.slugify(title);
			if (id.isBlank()) {
				throw new RPCException(1, "taxonomy value id is empty");
			}

			if (!taxonomy.getValues().containsKey(id)) {
				taxonomyYamlWriter.writeValue(db.getFileSystem().hostBase(), taxonomy.getSlug(), id, title);
				taxonomy.getValues().put(id, new Value(id, title));
				getContext().get(EventBusFeature.class).eventBus().publish(new ReloadTaxonomyConfig());
			}

			Map<String, Object> result = new HashMap<>();
			result.put("id", id);
			result.put("title", title);
			return result;
		} catch (RPCException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creating taxonomy value", e);
			throw new RPCException(0, e.getMessage());
		}
	}
}
