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
import com.condation.cms.api.annotations.Filter;
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.annotations.Param;
import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.api.hooks.ActionFunction;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.FilterFunction;
import com.condation.cms.api.hooks.HookSystem;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Request based hook system.
 *
 * @author t.marx
 */
@Slf4j
public class CMSHookSystem implements HookSystem {

	Multimap<String, ActionHook> actions = ArrayListMultimap.create();

	Multimap<String, FilterHook> filters = ArrayListMultimap.create();

	public CMSHookSystem () {
		
	}
	public CMSHookSystem(CMSHookSystem source) {
		this.actions.putAll(source.actions);
		this.filters.putAll(source.filters);
	}

	public void register(Object sourceObject) {
		Class<?> clazz = sourceObject.getClass();
		for (Method method : clazz.getMethods()) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (method.isAnnotationPresent(Action.class)) {
				Action annotation = method.getAnnotation(Action.class);
				ActionFunction<?> fn = buildActionFunction(sourceObject, method);
				if (fn != null) {
					registerAction(annotation.value(), fn, annotation.priority());
				}
			}
			if (method.isAnnotationPresent(Filter.class)) {
				Filter annotation = method.getAnnotation(Filter.class);
				FilterFunction<?> fn = buildFilterFunction(sourceObject, method);
				if (fn != null) {
					registerFilter(annotation.value(), fn, annotation.priority());
				}
			}
		}
	}

	private ActionFunction<?> buildActionFunction(Object target, Method method) {
		Parameter[] params = method.getParameters();
		// context style: single ActionContext parameter
		if (params.length == 1 && ActionContext.class.isAssignableFrom(params[0].getType())) {
			return context -> {
				try {
					return method.invoke(target, context);
				} catch (IllegalAccessException | InvocationTargetException e) {
					log.error("error invoking action hook", e);
					return null;
				}
			};
		}
		// named-params style: all parameters must carry @Param
		String[] names = paramNames(params);
		if (names != null) {
			return context -> {
				Object[] args = resolveArgs(context.arguments(), params, names);
				try {
					return method.invoke(target, args);
				} catch (IllegalAccessException | InvocationTargetException e) {
					log.error("error invoking action hook", e);
					return null;
				}
			};
		}
		log.warn("Method {} annotated with @Action has unsupported signature — skipped", method);
		return null;
	}

	private FilterFunction<?> buildFilterFunction(Object target, Method method) {
		Parameter[] params = method.getParameters();
		// context style: single FilterContext parameter
		if (params.length == 1 && FilterContext.class.isAssignableFrom(params[0].getType())) {
			return context -> {
				try {
					return method.invoke(target, context);
				} catch (IllegalAccessException | InvocationTargetException e) {
					log.error("error invoking filter hook", e);
					return null;
				}
			};
		}
		// direct-value style: single non-FilterContext parameter (the value itself)
		if (params.length == 1) {
			return context -> {
				try {
					return method.invoke(target, context.value());
				} catch (IllegalAccessException | InvocationTargetException e) {
					log.error("error invoking filter hook", e);
					return null;
				}
			};
		}
		log.warn("Method {} annotated with @Filter has unsupported signature — skipped", method);
		return null;
	}

	private String[] paramNames(Parameter[] params) {
		if (params.length == 0) {
			return new String[0];
		}
		String[] names = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			Param p = params[i].getAnnotation(Param.class);
			if (p == null) {
				return null;
			}
			names[i] = p.value();
		}
		return names;
	}

	private Object[] resolveArgs(Map<String, Object> arguments, Parameter[] params, String[] names) {
		Object[] args = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			args[i] = arguments.get(names[i]);
		}
		return args;
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

	public ActionContext<Object> doAction(final String name) {
		return doAction(name, Map.of());
	}

	public ActionContext<Object> doAction(final String name, final Map<String, Object> arguments) {
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
	 public <T> FilterContext<T> doFilter(final String name, final T parameters) {
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
