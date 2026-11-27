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
 * Registers hooks on server level.
 */
public final class ServerHookType extends BaseType {

    public ServerHookType() {
        super("server-hook", "Server hook registration");
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.extensions.server.ServerHookSystemRegisterExtensionPoint;
                import com.condation.cms.api.hooks.HookSystem;
                import com.condation.modules.api.annotation.Extension;

                @Extension(ServerHookSystemRegisterExtensionPoint.class)
                public class {{class}} extends ServerHookSystemRegisterExtensionPoint {

                    @Override
                    public void register(HookSystem hookSystem) {
                        // register actions and filters on the server hook system
                    }
                }
                """);
    }
}
