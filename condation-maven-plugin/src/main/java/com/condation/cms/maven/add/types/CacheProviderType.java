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
public final class CacheProviderType extends BaseType {

    public CacheProviderType() {
        super("cache-provider", "Cache provider",
                Question.text("name", "Name", "my-cache"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.cache.CacheProvider;
                import com.condation.cms.api.extensions.CacheProviderExtensionPoint;
                import com.condation.modules.api.annotation.Extension;

                @Extension(CacheProviderExtensionPoint.class)
                public class {{class}} extends CacheProviderExtensionPoint {

                    @Override
                    public String getName() {
                        return "{{name}}";
                    }

                    @Override
                    public CacheProvider getCacheProvider() {
                        throw new UnsupportedOperationException("TODO");
                    }
                }
                """,
                "name", literal(spec, "name"));
    }
}
