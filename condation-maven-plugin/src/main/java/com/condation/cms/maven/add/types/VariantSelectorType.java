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
 * Selector choosing a content variant.
 */
public final class VariantSelectorType extends BaseType {

    private static final String LABEL = "label";

    public VariantSelectorType() {
        super("variant-selector", "Variant selector",
                Question.text("id", "Selector id", "my-variant"),
                Question.text(LABEL, "Selector label", "My variant"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.db.ContentNode;
                import com.condation.cms.api.extensions.VariantSelectorExtensionPoint;
                import com.condation.cms.api.request.RequestContext;
                import com.condation.cms.api.variants.Variant;
                import com.condation.cms.api.variants.VariantSelection;
                import com.condation.modules.api.annotation.Extension;
                import java.util.List;

                @Extension(VariantSelectorExtensionPoint.class)
                public class {{class}} extends VariantSelectorExtensionPoint {

                    @Override
                    public String id() {
                        return "{{id}}";
                    }

                    @Override
                    public String label() {
                        return "{{label}}";
                    }

                    @Override
                    public VariantSelection select(ContentNode canonicalNode, List<Variant> variants, RequestContext context) {
                        // return VariantSelection.automatic(variant) to deliver one of the variants
                        return VariantSelection.canonical();
                    }
                }
                """,
                "id", literal(spec, "id"),
                LABEL, literal(spec, LABEL));
    }
}
