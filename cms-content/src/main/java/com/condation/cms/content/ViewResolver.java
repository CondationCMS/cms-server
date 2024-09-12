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
import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.ContentParserFeature;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.content.views.ViewParser;
import com.condation.cms.extensions.request.RequestExtensions;
import com.google.common.base.Strings;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ViewResolver {

	private final ContentRenderer contentRenderer;

	private final DB db;

	public Optional<ContentResponse> getViewContent(final RequestContext context) {
		return getViewContent(context, true);
	}

	public Optional<ContentResponse> getViewContent(final RequestContext context, final boolean checkVisibility) {
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
			} else {
				return Optional.empty();
			}
		} else {
			var temp = contentBase.resolve(path + ".md");
			if (temp.exists()) {
				contentFile = temp;
			} else {
				return Optional.empty();
			}
		}

		var uri = PathUtil.toRelativeFile(contentFile, contentBase);
		if (checkVisibility && !db.getContent().isVisible(uri)) {
			return Optional.empty();
		}

		final ContentNode contentNode = db.getContent().byUri(uri).get();
		if (!contentNode.isView()) {
			return Optional.empty();
		}
		context.add(CurrentNodeFeature.class, new CurrentNodeFeature(contentNode));

		try {
			var view = ViewParser.parse(contentFile);
			
			var page = view.getNodes(
				db, 
				contentFile, 
				context.get(ContentParserFeature.class).contentParser(), 
				context.get(RenderContext.class).markdownRenderer(), 
				context.get(RequestExtensions.class).getContext(), 
				context.get(RequestFeature.class).queryParameters(), context);
			
			var content = contentRenderer.renderView(contentFile, view, contentNode, context, page);
			return Optional.of(new ContentResponse(content, contentNode));
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return Optional.empty();
	}
}
