package com.github.thmarx.cms.request;

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

import com.github.thmarx.cms.content.ContentTags;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.feature.Feature;
import com.github.thmarx.cms.api.theme.Theme;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public record RenderContext(MarkdownRenderer markdownRenderer, ContentTags contentTags, Theme theme) 
		implements AutoCloseable, Feature {

	@Override
	public void close() throws Exception {
		markdownRenderer.close();
	}
	
	
}
