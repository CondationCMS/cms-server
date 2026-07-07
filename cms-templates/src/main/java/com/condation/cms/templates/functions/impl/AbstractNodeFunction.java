package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.api.content.MapAccess;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.content.ContentResolvingStrategy;
import com.condation.cms.templates.functions.TemplateFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public abstract class AbstractNodeFunction implements TemplateFunction {
	protected final RequestContext requestContext;

	protected void extendMap (Map<String, Object> node, ReadOnlyFile contentFile) {
		
	}
	
	@Override
	public Object invoke(Object... params) {
		
		if (params == null || params.length == 0 ) {
			return null;
		}
		if (!(params[0] instanceof String)) {
			return null;
		}
		String uri = ContentResolvingStrategy.uriToPath((String)params[0]);
		
		var db = requestContext.get(InjectorFeature.class).injector().getInstance(DB.class);
		var contentBase = db.getFileSystem().contentBase();
		
		Optional<ReadOnlyFile> contentFileOpt = ContentResolvingStrategy.resolve(uri, db);
		
		if (contentFileOpt.isPresent()) {
			var node_uri = PathUtil.toRelativeFile(contentFileOpt.get(), contentBase);
			final Optional<ContentNode> nodeByUri = db.getContent().byUri(node_uri);
			if (nodeByUri.isPresent()) {
				var node = new HashMap<String, Object>();
				node.put("meta", new MapAccess(nodeByUri.get().data()));
				node.put("uri", PathUtil.toURL(contentFileOpt.get(), contentBase));
				
				extendMap(node, contentFileOpt.get());
				
				return node;
			}
		}
		
		return null;
	}
}
