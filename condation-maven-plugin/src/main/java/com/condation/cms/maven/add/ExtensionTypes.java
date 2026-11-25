package com.condation.cms.maven.add;

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

import com.condation.cms.maven.add.types.ApiHandlerType;
import com.condation.cms.maven.add.types.BackupPostProcessingType;
import com.condation.cms.maven.add.types.CacheProviderType;
import com.condation.cms.maven.add.types.HookType;
import com.condation.cms.maven.add.types.HttpHandlerType;
import com.condation.cms.maven.add.types.HttpRoutesType;
import com.condation.cms.maven.add.types.MarkdownRendererType;
import com.condation.cms.maven.add.types.ModuleLifeCycleType;
import com.condation.cms.maven.add.types.QueryOperatorType;
import com.condation.cms.maven.add.types.RealtimeType;
import com.condation.cms.maven.add.types.RoutesType;
import com.condation.cms.maven.add.types.ServerHookType;
import com.condation.cms.maven.add.types.ServerLifecycleType;
import com.condation.cms.maven.add.types.ShortCodeType;
import com.condation.cms.maven.add.types.TemplateComponentType;
import com.condation.cms.maven.add.types.TemplateEngineType;
import com.condation.cms.maven.add.types.TemplateFunctionType;
import com.condation.cms.maven.add.types.TemplateModelType;
import com.condation.cms.maven.add.types.VariantSelectorType;
import java.util.List;
import java.util.Optional;

/**
 * Registry of all extension types the add goal can generate.
 */
public final class ExtensionTypes {

    private static final List<ExtensionType> ALL = List.of(
            new HookType(),
            new ShortCodeType(),
            new TemplateFunctionType(),
            new TemplateComponentType(),
            new RealtimeType(),
            new ApiHandlerType(),
            new HttpHandlerType(),
            new HttpRoutesType(),
            new RoutesType(),
            new TemplateModelType(),
            new QueryOperatorType(),
            new VariantSelectorType(),
            new BackupPostProcessingType(),
            new CacheProviderType(),
            new MarkdownRendererType(),
            new TemplateEngineType(),
            new ServerLifecycleType(),
            new ServerHookType(),
            new ModuleLifeCycleType()
    );

    private ExtensionTypes() {
    }

    public static List<ExtensionType> all() {
        return ALL;
    }

    public static Optional<ExtensionType> byId(String id) {
        return ALL.stream().filter(t -> t.id().equals(id)).findFirst();
    }
}
