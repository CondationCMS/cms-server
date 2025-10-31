package com.condation.cms.content.template.functions;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.api.markdown.MarkdownRenderer;

public class MarkdownFunction {

    private final MarkdownRenderer renderer;

    public MarkdownFunction(MarkdownRenderer renderer) {
        this.renderer = renderer;
    }

    public String render (String markdown) {
        return renderer.render(markdown);
    }
}
