package com.condation.cms.hooksystem;

/*-
 * #%L
 * CMS Api
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

import com.condation.cms.api.annotations.Scope;
import com.condation.cms.api.hooks.ActionFunction;
import com.condation.cms.api.hooks.FilterFunction;
import com.condation.cms.api.hooks.HookSystem;
import java.util.List;
import com.condation.cms.hooksystem.annotation.AnnotationHookRegistrar;
import com.condation.cms.hooksystem.executor.ActionExecutor;
import com.condation.cms.hooksystem.executor.FilterExecutor;
import com.condation.cms.hooksystem.registry.ActionRegistry;
import com.condation.cms.hooksystem.registry.FilterRegistry;
import java.util.Map;

/**
 * Default {@link HookSystem} implementation. Delegates to dedicated registries
 * and executors; annotation scanning is handled by {@link AnnotationHookRegistrar}.
 *
 * @author t.marx
 */
public class CMSHookSystem implements HookSystem {

    private final ActionRegistry actionRegistry;
    private final FilterRegistry filterRegistry;
    private final ActionExecutor actionExecutor;
    private final FilterExecutor filterExecutor;
    private final AnnotationHookRegistrar annotationRegistrar;
    
    private Scope scope = null; 

    public CMSHookSystem (Scope scope) {
        this.scope = scope;
        this.actionRegistry = new ActionRegistry();
        this.filterRegistry = new FilterRegistry();
        this.actionExecutor = new ActionExecutor(actionRegistry);
        this.filterExecutor = new FilterExecutor(filterRegistry);
        this.annotationRegistrar = new AnnotationHookRegistrar(actionRegistry, filterRegistry, scope);
    }
    
    public CMSHookSystem() {
        this(Scope.APPLICATION);
    }

    public CMSHookSystem(CMSHookSystem source) {
        this(Scope.REQUEST);
        this.actionRegistry.putAll(source.actionRegistry);
        this.filterRegistry.putAll(source.filterRegistry);
    }

    @Override
    public void register(Object sourceObject) {
        annotationRegistrar.register(sourceObject);
    }

    @Override
    public <T> void registerAction(String name, ActionFunction<T> function) {
        registerAction(name, function, 10);
    }

    @Override
    public <T> void registerAction(String name, ActionFunction<T> function, int priority) {
        actionRegistry.register(name, function, priority);
    }

    @Override
    public <T> void registerFilter(String name, FilterFunction<T> function) {
        registerFilter(name, function, 10);
    }

    @Override
    public <T> void registerFilter(String name, FilterFunction<T> function, int priority) {
        filterRegistry.register(name, function, priority);
    }

    @Override
    public <T> List<T> doAction(String name) {
        return doAction(name, Map.of());
    }

    @Override
    public <T> List<T> doAction(String name, Map<String, Object> arguments) {
        return actionExecutor.execute(name, arguments);
    }

    @Override
    public <T> T doFilter(String name, T value) {
        return filterExecutor.execute(name, value);
    }
}
