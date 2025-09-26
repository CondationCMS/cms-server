package com.condation.cms.tests.injection.test2;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SimpleDIContainer {
    private final Map<String, BeanDefinition> beans = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<BeanDefinition>> beansByType = new ConcurrentHashMap<>();
    private final Set<String> currentlyCreating = new HashSet<>();
    
    /**
     * Register a bean instance with the container
     */
    public <T> void registerBean(Class<T> type, T instance) {
        registerBean(type, type.getSimpleName(), instance, false);
    }
    
    /**
     * Register a bean instance with name and primary flag
     */
    public <T> void registerBean(Class<T> type, String name, T instance, boolean isPrimary) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(instance, "Instance cannot be null");
        
        BeanDefinition definition = new BeanDefinition(type, name, isPrimary, instance);
        beans.put(name, definition);
        
        // Register by type for lookup
        beansByType.computeIfAbsent(type, k -> new ArrayList<>()).add(definition);
        
        // Also register by all implemented interfaces
        for (Class<?> iface : type.getInterfaces()) {
            beansByType.computeIfAbsent(iface, k -> new ArrayList<>()).add(definition);
        }
        
        // Register by superclasses
        Class<?> superclass = type.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            beansByType.computeIfAbsent(superclass, k -> new ArrayList<>()).add(definition);
            superclass = superclass.getSuperclass();
        }
    }
    
    /**
     * Register a bean class that will be instantiated by the container
     */
    public <T> void registerBean(Class<T> type) {
        Component component = type.getAnnotation(Component.class);
        String name = (component != null && !component.value().isEmpty()) 
            ? component.value() 
            : type.getSimpleName();
        
        boolean isPrimary = type.isAnnotationPresent(Primary.class);
        
        try {
            T instance = createInstance(type);
            registerBean(type, name, instance, isPrimary);
        } catch (Exception e) {
            throw new DIException("Failed to register bean of type " + type.getName(), e);
        }
    }
    
    /**
     * Get bean by name
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        BeanDefinition definition = beans.get(name);
        if (definition == null) {
            throw new BeanNotFoundException("No bean found with name: " + name);
        }
        return (T) definition.getInstance();
    }
    
    /**
     * Get bean by type
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        List<BeanDefinition> candidates = beansByType.get(type);
        if (candidates == null || candidates.isEmpty()) {
            throw new BeanNotFoundException("No bean found of type: " + type.getName());
        }
        
        if (candidates.size() == 1) {
            return (T) candidates.get(0).getInstance();
        }
        
        // Multiple candidates - look for primary
        List<BeanDefinition> primaryBeans = candidates.stream()
            .filter(BeanDefinition::isPrimary)
            .collect(Collectors.toList());
            
        if (primaryBeans.size() == 1) {
            return (T) primaryBeans.get(0).getInstance();
        } else if (primaryBeans.size() > 1) {
            throw new DIException("Multiple primary beans found for type: " + type.getName());
        }
        
        throw new DIException("Multiple beans found for type " + type.getName() + 
            " and no primary bean specified. Available beans: " +
            candidates.stream().map(BeanDefinition::getName).collect(Collectors.joining(", ")));
    }
    
    /**
     * Get bean by type and name
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type, String name) {
        BeanDefinition definition = beans.get(name);
        if (definition == null) {
            throw new BeanNotFoundException("No bean found with name: " + name);
        }
        
        if (!type.isAssignableFrom(definition.getType())) {
            throw new DIException("Bean with name '" + name + "' is not of type " + type.getName());
        }
        
        return (T) definition.getInstance();
    }
    
    /**
     * Get all beans of a specific type
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeansOfType(Class<T> type) {
        List<BeanDefinition> candidates = beansByType.get(type);
        if (candidates == null) {
            return Collections.emptyList();
        }
        
        return candidates.stream()
            .map(def -> (T) def.getInstance())
            .collect(Collectors.toList());
    }
    
    /**
     * Check if container has a bean with given name
     */
    public boolean hasBean(String name) {
        return beans.containsKey(name);
    }
    
    /**
     * Check if container has a bean of given type
     */
    public boolean hasBean(Class<?> type) {
        List<BeanDefinition> candidates = beansByType.get(type);
        return candidates != null && !candidates.isEmpty();
    }
    
    /**
     * Create instance of a class with dependency injection
     */
    private <T> T createInstance(Class<T> clazz) {
        String className = clazz.getName();
        
        // Check for circular dependency
        if (currentlyCreating.contains(className)) {
            throw new CircularDependencyException("Circular dependency detected for: " + className);
        }
        
        try {
            currentlyCreating.add(className);
            
            // Find constructor to use
            Constructor<T> constructor = findConstructor(clazz);
            
            // Create instance
            T instance;
            if (constructor.getParameterCount() == 0) {
                instance = constructor.newInstance();
            } else {
                Object[] args = resolveConstructorArguments(constructor);
                instance = constructor.newInstance(args);
            }
            
            // Inject fields
            injectFields(instance);
            
            return instance;
            
        } catch (Exception e) {
            throw new DIException("Failed to create instance of " + className, e);
        } finally {
            currentlyCreating.remove(className);
        }
    }
    
    /**
     * Find the constructor to use for instantiation
     */
    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        
        // Look for @Inject annotated constructor
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                constructor.setAccessible(true);
                return (Constructor<T>) constructor;
            }
        }
        
        // Fall back to default constructor
        try {
            Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor;
        } catch (NoSuchMethodException e) {
            throw new DIException("No suitable constructor found for " + clazz.getName() + 
                ". Either provide a default constructor or annotate a constructor with @Inject");
        }
    }
    
    /**
     * Resolve constructor arguments
     */
    private Object[] resolveConstructorArguments(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();
            
            Named named = parameter.getAnnotation(Named.class);
            if (named != null) {
                args[i] = getBean(paramType, named.value());
            } else {
                args[i] = getBean(paramType);
            }
        }
        
        return args;
    }
    
    /**
     * Inject fields marked with @Inject
     */
    private void injectFields(Object instance) {
        Class<?> clazz = instance.getClass();
        
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    
                    try {
                        Object value;
                        Named named = field.getAnnotation(Named.class);
                        if (named != null) {
                            value = getBean(field.getType(), named.value());
                        } else {
                            value = getBean(field.getType());
                        }
                        
                        field.set(instance, value);
                    } catch (IllegalAccessException e) {
                        throw new DIException("Failed to inject field: " + field.getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
    
    /**
     * Get bean names
     */
    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(beans.keySet());
    }
    
    /**
     * Clear all beans
     */
    public void clear() {
        beans.clear();
        beansByType.clear();
        currentlyCreating.clear();
    }
    
    /**
     * Get container statistics
     */
    public String getStats() {
        return String.format("DI Container Stats: %d beans registered, %d types covered", 
            beans.size(), beansByType.size());
    }
}
