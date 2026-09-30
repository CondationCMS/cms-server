package com.condation.cms.core.injector;

/*-
 * #%L
 * CMS Core
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

import com.condation.cms.api.injector.Binding;
import com.condation.cms.api.injector.Module;
import com.condation.cms.api.injector.Injector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author thorstenmarx
 */
public class DefaultInjector implements Injector {
	
	private static final String DEFAULT_NAME = "#name#";
	
	private final ConcurrentMap<BindingKey, DefaultBinding<?>> bindings = new ConcurrentHashMap<>();
	
	private Injector parent;
	
	private DefaultInjector (Injector parent) {
		this.parent = parent;
	}
	
	public static Injector create (Module... modules) {
		return create(null, modules);
	}
	
	public static Injector create (Injector parent, Module... modules) {
		var injector = new DefaultInjector(parent);
		
		
		if (modules != null) {
			Stream.of(modules).forEach(module -> module.register(injector));
		}
		
		return injector;
	}

	@Override
	public <T> Binding register(Class<T> clazz, Function<Injector, T> newInstanceFunction) {
		return register(DEFAULT_NAME, clazz, newInstanceFunction);
	}

	@Override
	public <T> T getInstance(Class<T> clazz) {
		return getInstance(DEFAULT_NAME, clazz);
	}

	@Override
	public <T> Binding register(String name, Class<T> clazz, Function<Injector, T> newInstanceFunction) {
		var binding = new DefaultBinding<T>();
		
		binding.function = newInstanceFunction;
		
		final BindingKey bindingKey = new BindingKey(name, clazz);

		if (bindings.putIfAbsent(bindingKey, binding) != null) {
			throw new IllegalStateException(
					"Binding already registered: " + clazz.getName() + " (name: " + name + ")"
			);
		}
		
		return binding;
	}

	@Override
	public <T> T getInstance(String name, Class<T> clazz) {
		DefaultBinding<T> binding = (DefaultBinding<T>) bindings.get(new BindingKey(name, clazz));
		
		if (binding != null) {
			return binding.newInstance(this);
		} 
		
		if (parent != null) {
			return parent.getInstance(name, clazz);
		}
		
		throw new IllegalStateException("No binding for " + clazz.getName() + " with name '" + name + "'");
	}

	@Override
	public void initializeEager() {
		bindings.values().stream()
				.filter(binding -> binding.eager)
				.forEach(binding -> binding.newInstance(this));
	}
	
	private record BindingKey (String name, Class<?> clazz) {}
}
