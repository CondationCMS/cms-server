package com.condation.cms.content.template.functions;

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
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.HTTPUtil;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCurrentNodeFunction {

	protected final DB db;
	protected final ReadOnlyFile currentNode;
	protected final ContentParser contentParser;
	protected final MarkdownRenderer markdownRenderer;
	protected final ContentNodeMapper contentNodeMapper;
	protected final RequestContext context;

	protected String getUrl(ReadOnlyFile node) {
		StringBuilder sb = new StringBuilder();

		while (node != null && !node.equals(db.getReadOnlyFileSystem().contentBase())) {

			var filename = node.getFileName();
			if (!filename.equals("index.md")) {
				if (filename.endsWith(".md")) {
					filename = filename.substring(0, filename.length() - 3);
				}
				sb.insert(0, filename);
				sb.insert(0, "/");
			}
			if (node.hasParent()) {
				node = node.getParent();
			} else {
				node = null;
			}
		}

		var url = sb.toString();

		url = "".equals(url) ? "/" : url;
		
		return HTTPUtil.modifyUrl(url, context);
	}

	protected boolean isPreview() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)) {
			return true;
		}

		return false;
	}
	protected String getPreviewMode() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)) {
			return RequestContextScope.REQUEST_CONTEXT.get().get(IsPreviewFeature.class).mode().getValue();
		}

		return IsPreviewFeature.Mode.PREVIEW.getValue();
	}

	protected Optional<ContentParser.Content> parse(ReadOnlyFile node) {
		try {
			//Path rel = contentBase.relativize(node);
			if (node.isDirectory()) {
				node = node.resolve("index.md");
			}
			var md = contentParser.parse(node);

			return Optional.of(md);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return Optional.empty();
	}
}
