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
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
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
		final DB db = getContext().get(DBFeature.class).db();

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
}
