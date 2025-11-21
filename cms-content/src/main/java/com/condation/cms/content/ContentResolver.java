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

import com.condation.cms.api.Constants;
import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.content.RedirectContentResponse;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.content.ContentResolvingStrategy;
import com.google.common.base.Strings;
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
	
	public Optional<ContentResponse> getStaticContent (String uri) {
		if (uri.endsWith(".md")) {
			return Optional.empty();
		}
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		ReadOnlyFile staticFile = contentBase.resolve(uri);
		if (staticFile.isDirectory()) {
			return Optional.empty();
		}
		try {
			if (staticFile.exists()) {
				return Optional.ofNullable(new DefaultContentResponse(
						staticFile.getContent(), 
						staticFile.getContentType(), 
						null
				));
			}
		} catch (IOException ex) {
			log.error("", ex);
		}
		return Optional.empty();
	}
	
	public Optional<ContentResponse> getContent (final RequestContext context) {
		return getContent(context, true);
	}
	
	public Optional<ContentResponse> getErrorContent (final RequestContext context) {
		return getContent(context, false);
	}
	
	private Optional<ContentResponse> getContent(final RequestContext context, boolean checkVisibility) {
		/*
		String path;
		if (Strings.isNullOrEmpty(context.get(RequestFeature.class).uri())) {
			path = "";
		} else if (context.get(RequestFeature.class).uri().startsWith("/")) {
			// remove leading slash
			path = context.get(RequestFeature.class).uri().substring(1);
		} else {
			path = context.get(RequestFeature.class).uri();
		}
		
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		var contentPath = contentBase.resolve(path);
		ReadOnlyFile contentFile = null;
		if (contentPath.exists() && contentPath.isDirectory()) {
			// use index.md
			var tempFile = contentPath.resolve("index.md");
			if (tempFile.exists()) {
				contentFile = tempFile;
			}
		} else {
			var temp = contentBase.resolve(path + ".md");
			if (temp.exists()) {
				contentFile = temp;
			}
		}
		*/
		
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		var path = ContentResolvingStrategy.uriToPath(context.get(RequestFeature.class).uri());
		Optional<ReadOnlyFile> contentFileOpt = ContentResolvingStrategy.resolve(context.get(RequestFeature.class).uri(), db);
		ReadOnlyFile contentFile = contentFileOpt.orElse(null);
		// handle alias
		ContentNode contentNode = null;
		boolean aliasRedirect = false;
		if (contentFile == null || !contentFile.exists()) {
			var query = db.getContent().query((node, count) -> node);
			var result = query.whereContains(Constants.MetaFields.ALIASES, "/" + path).get();
			if (!result.isEmpty()) {
				contentNode = result.getFirst();
				contentFile = contentBase.resolve(contentNode.uri());
				aliasRedirect = true;
			}
		} else {
			var uri = PathUtil.toRelativeFile(contentFile, contentBase);
			final Optional<ContentNode> nodeByUri = db.getContent().byUri(uri);
			if (nodeByUri.isPresent()) {
				contentNode = nodeByUri.get();
			}
		}
		
		if (contentNode == null) {
			return Optional.empty();
		}
		
		if (checkVisibility && !db.getContent().isVisible(contentNode)) {
			return Optional.empty();
		}
		
		
		if (contentNode.isRedirect()) {
			return Optional.of(new DefaultContentResponse(contentNode));
		} else if (!Constants.NodeType.PAGE.equals(contentNode.nodeType())) {
			return Optional.empty();
		}
		context.add(CurrentNodeFeature.class, new CurrentNodeFeature(contentNode));
		
		if (contentNode.isRedirect()) {
			return Optional.of(new RedirectContentResponse(contentNode.getRedirectLocation(), contentNode.getRedirectStatus()));
		} else if (aliasRedirect) {
			var doRedirect = contentNode.getMetaValue(Constants.MetaFields.ALIASES_REDIRECT, true);
			if (doRedirect) {
				var url = PathUtil.toURL(contentFile, contentBase);
				url = HTTPUtil.modifyUrl(url, context);
				return Optional.of(new RedirectContentResponse(url, 301));
			}
		}
		
		try {
			
			List<ContentNode> sections = db.getContent().listSections(contentFile);
			
			Map<String, List<Section>> renderedSections = contentRenderer.renderSections(sections, context);
			
			var content = contentRenderer.render(contentFile, context, renderedSections);
			
			var contentType = contentNode.contentType();
			
			return Optional.of(new DefaultContentResponse(content, contentType, contentNode));
		} catch (IOException ex) {
			log.error(null, ex);
			return Optional.empty();
		}
	}
}
