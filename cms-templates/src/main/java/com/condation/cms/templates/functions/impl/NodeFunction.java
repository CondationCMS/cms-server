package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.content.MapAccess;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.content.ContentResolvingStrategy;
import com.condation.cms.templates.functions.TemplateFunction;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
public class NodeFunction implements TemplateFunction {

	public static final String NAME = "select_node";
	
	private final RequestContext requestContext;
	
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
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		
		Optional<ReadOnlyFile> contentFileOpt = ContentResolvingStrategy.resolve(uri, db);
		
		if (contentFileOpt.isPresent()) {
			var node_uri = PathUtil.toRelativeFile(contentFileOpt.get(), contentBase);
			final Optional<ContentNode> nodeByUri = db.getContent().byUri(node_uri);
			if (nodeByUri.isPresent()) {
				return Map.of(
					"meta", new MapAccess(nodeByUri.get().data()),
					"uri", PathUtil.toURL(contentFileOpt.get(), contentBase)
				);
			}
		}
		
		return null;
	}

	@Override
	public String name() {
		return NAME;
	}
	
}
