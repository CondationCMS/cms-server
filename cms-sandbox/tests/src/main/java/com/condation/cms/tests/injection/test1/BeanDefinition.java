/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.tests.injection.test1;

/*-
 * #%L
 * tests
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import java.util.function.Supplier;

// --- core types ---

final class BeanDefinition<T> {

	final Class<T> exposedType; // interface or base type used for registration
	final Class<? extends T> implType; // concrete implementation
	final String name;
	final Supplier<T> supplier;
	final Scope.Type scope;
	final boolean primary;
	final boolean allowMultiple;
	volatile T singletonInstance;

	BeanDefinition(Class<T> exposedType, Class<? extends T> implType, String name, Supplier<T> supplier, Scope.Type scope, boolean primary, boolean allowMultiple) {
		this.exposedType = exposedType;
		this.implType = implType;
		this.name = name;
		this.supplier = supplier;
		this.scope = scope;
		this.primary = primary;
		this.allowMultiple = allowMultiple;
	}

	T get(SimpleDIContainer container) {
		if (scope == Scope.Type.SINGLETON) {
			if (singletonInstance == null) {
				synchronized (this) {
					if (singletonInstance == null) {
						singletonInstance = container.createAndInject(this);
					}
				}
			}
			return singletonInstance;
		} else {
			return container.createAndInject(this);
		}
	}
	
}
