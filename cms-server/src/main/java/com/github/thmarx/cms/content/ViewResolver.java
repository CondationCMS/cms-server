package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.content.ContentResponse;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.feature.features.RequestFeature;
import com.github.thmarx.cms.api.utils.PathUtil;
import com.github.thmarx.cms.content.views.ViewParser;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class ViewResolver {

	private final Path contentBase;

	private final ContentRenderer contentRenderer;

	private final DB db;

	public Optional<ContentResponse> getViewContent(final RequestContext context) {
		String path;
		if (Strings.isNullOrEmpty(context.get(RequestFeature.class).uri())) {
			path = "";
		} else if (context.get(RequestFeature.class).uri().startsWith("/")) {
			// remove leading slash
			path = context.get(RequestFeature.class).uri().substring(1);
		} else {
			path = context.get(RequestFeature.class).uri();
		}

		var contentPath = contentBase.resolve(path);
		
		try {
			if (Files.exists(contentPath) && Files.isDirectory(contentPath)) {
				if (isView(contentPath)) {
					var viewFile = contentPath.resolve("view.yaml");
					var view = ViewParser.parse(viewFile);
					var content = contentRenderer.renderView(viewFile, view, context);
					return Optional.of(new ContentResponse(content, null));
				}
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return Optional.empty();
	}

	private boolean isView(final Path path) {
		var viewFile = path.resolve("view.yaml");
		return Files.exists(viewFile);
	}
}
