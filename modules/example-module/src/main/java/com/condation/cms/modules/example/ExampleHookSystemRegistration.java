package com.condation.cms.modules.example;

/*-
 * #%L
 * CMS Example Module
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
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.annotations.Scope;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;

import com.condation.cms.api.hooks.HookSystem;
import com.condation.modules.api.annotation.Extension;

/**
 *
 * @author t.marx
 */
@Extension(HookSystemRegisterExtensionPoint.class)
public class ExampleHookSystemRegistration extends HookSystemRegisterExtensionPoint {

    @Override
    public void register(HookSystem hookSystem) {
        hookSystem.registerAction("example/test", (hookContext) -> "example hook function");
    }

    @Action(value = "example", scope = Scope.APPLICATION)
    public String example() {
        return "<!-- example from application scope -->";
    }

}
