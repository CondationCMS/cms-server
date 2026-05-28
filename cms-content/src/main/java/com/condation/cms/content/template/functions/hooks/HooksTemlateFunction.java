package com.condation.cms.content.template.functions.hooks;

/*-
 * #%L
 * CMS Content
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

	public List<Object> execute(String name) {
		return execute(name, Map.of());
	}

	public List<Object> execute(String name, Map<String, Object> arguments) {
		return hookSystem.doAction(name, arguments);
	}

	public Object filter(String name) {
		return filter(name, List.of());
	}

	public Object filter(String name, List<Object> arguments) {
		return hookSystem.doFilter(name, arguments);
	}
}
