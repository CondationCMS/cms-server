package com.condation.cms.api.hooks;

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
import java.util.List;
import java.util.Map;

/**
 *
 * Request based hook system.
 *
 * @author t.marx
 */
public interface HookSystem {

	void register(Object sourceObject);

	public <T> void registerAction(final String name, final ActionFunction<T> hookFunction);

	public <T> void registerAction(final String name, final ActionFunction<T> hookFunction, int priority);

	public <T> void registerFilter(final String name, final FilterFunction<T> hookFunction);

	public <T> void registerFilter(final String name, final FilterFunction<T> hookFunction, int priority);

	public <T> List<T> doAction(final String name);

	public <T> List<T> doAction(final String name, final Map<String, Object> arguments);

	/**
	 * Calls all filters with the given value in priority order and returns the
	 * final transformed value. If no filter is registered, the original value
	 * is returned unchanged.
	 */
	public <T> T doFilter(final String name, final T value);
}
