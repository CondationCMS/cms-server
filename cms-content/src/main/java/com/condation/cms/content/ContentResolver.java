package com.condation.cms.content;

/*-
 * #%L
 * CMS Content
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
import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.content.RedirectContentResponse;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.core.content.ContentResolvingStrategy;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ContentResolver {

	private final ContentRenderer contentRenderer;
	
	private final DB db;
	
	public Optional<ContentResponse> getContent (final RequestContext context) {
		return getContent(context, true);
	}
	
	public Optional<ContentResponse> getErrorContent (final RequestContext context) {
		return getContent(context, false);
	}
	
	private Optional<ContentResponse> getContent(final RequestContext context, boolean checkVisibility) {
        final String uri = context.get(RequestFeature.class).uri();
		var path = ContentResolvingStrategy.uriToPath(uri);

		Optional<ContentNode> contentNodeOpt = db.getContent().byUrl(uri);

		// handle alias
		ContentNode contentNode = null;
        Optional<String> aliasRedirectUrl = Optional.empty();
		if (contentNodeOpt.isEmpty()) {
			var query = db.getContent().query((node, count) -> node);
			var result = query.whereContains(Constants.MetaFields.ALIASES, "/" + path).get();
			if (!result.isEmpty()) {
				contentNode = result.getFirst();
                aliasRedirectUrl = Optional.of(contentNode.url());
			}
		} else {
			contentNode = contentNodeOpt.get();
		}
		
		if (contentNode == null) {
			return Optional.empty();
		}

        var contentFile = db.getFileSystem().contentBase().resolve(contentNode.path());
		
		if (checkVisibility && !db.getContent().isVisible(contentNode)) {
			return Optional.empty();
		}
		
		
		if (contentNode.isRedirect()) {
			return Optional.of(new RedirectContentResponse(contentNode.getRedirectLocation(), contentNode.getRedirectStatus()));
		} else if (aliasRedirectUrl.isPresent()) {
			var doRedirect = contentNode.getMetaValue(Constants.MetaFields.ALIASES_REDIRECT, true);
			if (doRedirect) {
                var url = HTTPUtil.modifyUrl(aliasRedirectUrl.get(), context);
				return Optional.of(new RedirectContentResponse(url, 301));
			}
		} else if (!Constants.NodeType.PAGE.equals(contentNode.nodeType())) {
			return Optional.empty();
		}
		context.add(CurrentNodeFeature.class, new CurrentNodeFeature(contentNode));
		
		try {
			
			List<ContentNode> sectionEntries = db.getContent().listSectionEntries(contentFile);
			
			Map<String, List<SectionEntry>> renderedSectionEntries = contentRenderer.renderSectionEntries(sectionEntries, context);
			
			var content = contentRenderer.render(contentFile, context, renderedSectionEntries);
			
			var contentType = contentNode.contentType();
			
			return Optional.of(new DefaultContentResponse(content, contentType, contentNode));
		} catch (IOException ex) {
			log.error(null, ex);
			return Optional.empty();
		}
	}
}
