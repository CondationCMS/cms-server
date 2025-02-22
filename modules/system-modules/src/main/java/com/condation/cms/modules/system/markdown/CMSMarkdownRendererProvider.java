package com.condation.cms.modules.system.markdown;

/*-
 * #%L
 * cms-system-modules
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import com.condation.cms.api.extensions.MarkdownRendererProviderExtensionPoint;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.content.markdown.module.CMSMarkdownRenderer;
import com.condation.modules.api.annotation.Extension;

/**
 *
 * @author t.marx
 */
@Extension(MarkdownRendererProviderExtensionPoint.class)
public class CMSMarkdownRendererProvider extends MarkdownRendererProviderExtensionPoint {

	private final CMSMarkdownRenderer markdownRenderer = new CMSMarkdownRenderer();
	
	@Override
	public String getName() {
		return "system";
	}

	@Override
	public MarkdownRenderer getRenderer() {
		return markdownRenderer;
	}
	
}
