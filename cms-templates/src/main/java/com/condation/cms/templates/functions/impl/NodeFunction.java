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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.ContentRenderer;
import com.condation.cms.content.SectionEntry;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public class NodeFunction extends AbstractNodeFunction {

	public static final String NAME = "select_node";

	public NodeFunction(RequestContext requestContext) {
		super(requestContext);
	}
	
	@Override
	protected void extendMap(Map<String, Object> node, ReadOnlyFile contentFile) {
		try {
			var db = requestContext.get(InjectorFeature.class).injector().getInstance(DB.class);
			var contentRenderer = requestContext.get(InjectorFeature.class).injector().getInstance(ContentRenderer.class);
			List<ContentNode> sectionEntryList = db.getContent().listSectionEntries(contentFile);
			
			Map<String, List<SectionEntry>> sectionEntries = contentRenderer.renderSectionEntries(sectionEntryList, requestContext);
			node.put("sections", sectionEntries);
		} catch (IOException iOException) {
			log.error("error loading sections", iOException);
		}
	}

	@Override
	public String name() {
		return NAME;
	}
	
}
