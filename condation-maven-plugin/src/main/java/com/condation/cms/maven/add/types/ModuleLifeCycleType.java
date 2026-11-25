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

/**
 * Module lifecycle (modules-framework): called when the module is activated or deactivated.
 */
public final class ModuleLifeCycleType extends BaseType {

    public ModuleLifeCycleType() {
        super("module-lifecycle", "Module lifecycle (activate / deactivate)");
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.modules.api.Context;
                import com.condation.modules.api.ModuleLifeCycleExtension;
                import com.condation.modules.api.annotation.Extension;

                @Extension(ModuleLifeCycleExtension.class)
                public class {{class}} extends ModuleLifeCycleExtension<Context> {

                    private static final System.Logger LOGGER = System.getLogger({{class}}.class.getName());

                    @Override
                    public void init() {
                        // called once when the extension is created
                    }

                    @Override
                    public void activate() {
                        LOGGER.log(System.Logger.Level.INFO, "module activated");
                    }

                    @Override
                    public void deactivate() {
                        LOGGER.log(System.Logger.Level.INFO, "module deactivated");
                    }
                }
                """);
    }
}
