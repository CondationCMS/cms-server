package com.condation.cms.api.hooks;

/*-
 * #%L
 * cms-api
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
import com.condation.cms.api.annotations.Filter;
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.utils.AnnotationsUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Request based hook system.
 *
 * @author t.marx
 */
@Slf4j
public class HookSystem {

	Multimap<String, ActionHook> actions = ArrayListMultimap.create();

	Multimap<String, FilterHook> filters = ArrayListMultimap.create();

	public HookSystem () {
		
	}
	public HookSystem(HookSystem source) {
		this.actions.putAll(source.actions);
		this.filters.putAll(source.filters);
	}

	public void register(Object sourceObject) {
		// Action-Methoden registrieren
		List<AnnotationsUtil.CMSAnnotation<Action, Void>> actionMethods
				= AnnotationsUtil.process(sourceObject, Action.class, List.of(ActionContext.class), Void.class);

		for (AnnotationsUtil.CMSAnnotation<Action, Void> ann : actionMethods) {
			Action annotation = ann.annotation();
			registerAction(annotation.value(), context -> ann.invoke(context), annotation.priority());
		}

		// Filter-Methoden registrieren
		List<AnnotationsUtil.CMSAnnotation<Filter, Object>> filterMethods
				= AnnotationsUtil.process(sourceObject, Filter.class, List.of(FilterContext.class), Object.class);

		for (AnnotationsUtil.CMSAnnotation<Filter, Object> ann : filterMethods) {
			Filter annotation = ann.annotation();
			registerFilter(annotation.value(), context -> ann.invoke(context), annotation.priority());
		}
	}

	public <T> void registerAction(final String name, final ActionFunction<T> hookFunction) {
		registerAction(name, hookFunction, 10);
	}

	public <T> void registerAction(final String name, final ActionFunction<T> hookFunction, int priority) {
		actions.put(name, new ActionHook<>(name, priority, hookFunction));
	}

	public <T> void registerFilter(final String name, final FilterFunction<T> hookFunction) {
		registerFilter(name, hookFunction, 10);
	}

	public <T> void registerFilter(final String name, final FilterFunction<T> hookFunction, int priority) {
		filters.put(name, new FilterHook<>(name, priority, hookFunction));
	}

	public ActionContext<Object> execute(final String name) {
		return execute(name, Map.of());
	}

	public ActionContext<Object> execute(final String name, final Map<String, Object> arguments) {
		var context = new ActionContext(new HashMap<>(arguments), new ArrayList<>());
		actions.get(name).stream()
				.sorted((h1, h2) -> Integer.compare(h1.priority(), h2.priority()))
				.map((action) -> {
					try {
						return action.function().apply(context);
					} catch (Exception e) {
						log.error("error executing action", e);
					}
					return null;
				})
				.filter(value -> value != null)
				.forEach(context.results()::add);

		return context;
	}

	/**
	 * calls all filters with the given parameters, if no filter is executed,
	 * the original parameters are returned
	 *
	 * @param <T>
	 * @param name
	 * @param parameters
	 * @return
	 */
	public <T> FilterContext<T> filter(final String name, final T parameters) {
		final FilterContext<T> returnContext = new FilterContext(
				parameters
		);
		filters.get(name).stream()
				.sorted((h1, h2) -> Integer.compare(h1.priority(), h2.priority()))
				.forEach((var action) -> {
					try {
						var context = new FilterContext(returnContext.value());
						var result = action.function().apply(context);
						returnContext.value((T) result);
					} catch (Exception e) {
						log.error("error on filter", e);
					}
				});

		return returnContext;
	}
}
