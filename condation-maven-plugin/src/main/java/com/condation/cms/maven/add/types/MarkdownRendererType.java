package com.condation.cms.maven.add.types;

/*-
 * #%L
 * CondationCMS Maven Plugin
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

import com.condation.cms.maven.add.ExtensionSpec;
import com.condation.cms.maven.add.Question;

/**
 * Provider extension, the provided implementation has to be completed.
 */
public final class MarkdownRendererType extends BaseType {

    public MarkdownRendererType() {
        super("markdown-renderer", "Markdown renderer provider",
                Question.text("name", "Name", "my-markdown"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.markdown.MarkdownRenderer;
                import com.condation.cms.api.extensions.MarkdownRendererProviderExtensionPoint;
                import com.condation.modules.api.annotation.Extension;

                @Extension(MarkdownRendererProviderExtensionPoint.class)
                public class {{class}} extends MarkdownRendererProviderExtensionPoint {

                    @Override
                    public String getName() {
                        return "{{name}}";
                    }

                    @Override
                    public MarkdownRenderer getRenderer() {
                        throw new UnsupportedOperationException("TODO");
                    }
                }
                """,
                "name", literal(spec, "name"));
    }
}
