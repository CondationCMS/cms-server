/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.tests.injection.test1;

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
