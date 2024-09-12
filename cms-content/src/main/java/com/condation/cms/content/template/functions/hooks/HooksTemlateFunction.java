package com.condation.cms.content.template.functions.hooks;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.HookSystem;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class HooksTemlateFunction {
	
	private final HookSystem hookSystem;
	
	@Deprecated(since = "4.18.0", forRemoval = true)
	public ActionContext<Object> call (String name) {
		return execute(name, Map.of());
	}
	@Deprecated(since = "4.18.0", forRemoval = true)
	public ActionContext<Object> call (String name, Map<String, Object> arguments) {
		return execute(name, arguments);
	}
	
	public ActionContext<Object> execute (String name) {
		return execute(name, Map.of());
	}
	public ActionContext<Object> execute (String name, Map<String, Object> arguments) {
		return hookSystem.execute(name, arguments);
	}
	
	public FilterContext<Object> filter (String name) {
		return filter(name, List.of());
	}
	public FilterContext<Object> filter (String name, List<Object> arguments) {
		return hookSystem.filter(name, arguments);
	}
}
