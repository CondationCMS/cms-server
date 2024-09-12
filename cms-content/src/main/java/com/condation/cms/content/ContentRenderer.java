package com.condation.cms.content;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.model.ListNode;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.content.views.model.View;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author t.marx
 */
public interface ContentRenderer {

	String render(final ReadOnlyFile contentFile, final RequestContext context) throws IOException;

	String render(final ReadOnlyFile contentFile, final RequestContext context, final Map<String, List<Section>> sections) throws IOException;

	String render(final ReadOnlyFile contentFile, final RequestContext context, final Map<String, List<Section>> sections, final Map<String, Object> meta, final String markdownContent, final Consumer<TemplateEngine.Model> modelExtending) throws IOException;

	Map<String, List<Section>> renderSections(final List<ContentNode> sectionNodes, final RequestContext context) throws IOException;

	String renderTaxonomy(final Taxonomy taxonomy, Optional<String> taxonomyValue, final RequestContext context, final Map<String, Object> meta, final Page<ListNode> page) throws IOException;

	String renderView(final ReadOnlyFile viewFile, final View view, final ContentNode contentNode, final RequestContext requestContext, final Page<ListNode> page) throws IOException;
	
}
