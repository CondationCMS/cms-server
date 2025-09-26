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

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/*
 Extended SimpleDIContainer.java
 - Added support for registering multiple implementations for the same interface.
 - Now you can do:
     container.register(Interface.class, Impl1.class);
     container.register(Interface.class, Impl2.class);
 - You can still retrieve single or all implementations.
*/

public class SimpleDIContainer implements AutoCloseable {


    // type -> list of bean defs
    private final Map<Class<?>, List<BeanDefinition<?>>> beans = new ConcurrentHashMap<>();
    private final List<BeanDefinition<?>> allDefs = Collections.synchronizedList(new ArrayList<>());

    // --- Registration API ---
    public <T> void register(Class<T> exposedType, Class<? extends T> implType) {
        Objects.requireNonNull(exposedType);
        Objects.requireNonNull(implType);
        boolean primary = implType.isAnnotationPresent(Primary.class);
        boolean allowMultiple = implType.isAnnotationPresent(AllowMultiple.class);
        Scope.Type scope = implType.isAnnotationPresent(Scope.class) ? implType.getAnnotation(Scope.class).value() : Scope.Type.SINGLETON;
        String name = getNameFromAnnotation(implType);
        Supplier<T> supplier = () -> instantiateByConstructor(implType);
        registerSupplierInternal(exposedType, implType, name, supplier, scope, primary, allowMultiple);
    }

    public <T> void registerInstance(Class<T> exposedType, T instance) {
        Objects.requireNonNull(exposedType);
        Objects.requireNonNull(instance);
        boolean allowMultiple = instance.getClass().isAnnotationPresent(AllowMultiple.class) || exposedType.isAnnotationPresent(AllowMultiple.class);
        boolean primary = instance.getClass().isAnnotationPresent(Primary.class);
        String name = getNameFromAnnotation(instance.getClass());
        Supplier<T> supplier = () -> instance;
        registerSupplierInternal(exposedType, (Class<? extends T>) instance.getClass(), name, supplier, Scope.Type.SINGLETON, primary, allowMultiple);
    }

    private <T> void registerSupplierInternal(Class<T> exposedType, Class<? extends T> implType, String name, Supplier<T> supplier, Scope.Type scope, boolean primary, boolean allowMultiple) {
        BeanDefinition<T> def = new BeanDefinition<>(exposedType, implType, name, supplier, scope, primary, allowMultiple);
        beans.compute(exposedType, (k, list) -> {
            if (list == null) list = new ArrayList<>();
            else {
                boolean anyAllowed = list.stream().anyMatch(d -> d.allowMultiple) || def.allowMultiple;
                if (!list.isEmpty() && !anyAllowed) {
                    throw new IllegalStateException("Duplicate bean registration for type " + exposedType + " (no AllowMultiple)");
                }
            }
            list.add(def);
            return list;
        });
        allDefs.add(def);
    }

    private static String getNameFromAnnotation(AnnotatedElement el) {
        Named n = el.getAnnotation(Named.class);
        return n == null ? null : n.value();
    }

    // --- Retrieval API ---
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        List<BeanDefinition<?>> list = beans.get(type);
        if (list == null || list.isEmpty()) throw new NoSuchElementException("No bean for type: " + type);
        if (list.size() == 1) return (T) list.get(0).get(this);
        Optional<BeanDefinition<?>> primary = list.stream().filter(d -> d.primary).findFirst();
        if (primary.isPresent()) return (T) primary.get().get(this);
        throw new IllegalStateException("Multiple beans found for type " + type + ". Use getBean(type, name) or getBeans(type)");
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type, String name) {
        List<BeanDefinition<?>> list = beans.get(type);
        if (list == null) throw new NoSuchElementException("No bean for type: " + type);
        for (BeanDefinition<?> d : list) {
            if (Objects.equals(d.name, name)) return (T) d.get(this);
        }
        throw new NoSuchElementException("No bean of type " + type + " with name '" + name + "'");
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<T> type) {
        List<BeanDefinition<?>> list = beans.get(type);
        if (list == null) return Collections.emptyList();
        List<T> out = new ArrayList<>();
        for (BeanDefinition<?> d : list) out.add((T) d.get(this));
        return Collections.unmodifiableList(out);
    }

    // --- instantiation & injection ---
    <T> T createAndInject(BeanDefinition<T> def) {
        T instance = def.supplier.get();
        injectFields(instance);
        callAnnotatedMethods(instance, PostConstruct.class);
        return instance;
    }

    private <T> void injectFields(T instance) {
        Class<?> cls = instance.getClass();
        for (Field f : getAllFields(cls)) {
            if (f.isAnnotationPresent(Inject.class)) {
                boolean accessible = f.canAccess(instance);
                try {
                    f.setAccessible(true);
                    Class<?> t = f.getType();
                    Named n = f.getAnnotation(Named.class);
                    Object value = (n != null) ? getBean(t, n.value()) : getBean(t);
                    f.set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } finally {
                    try { f.setAccessible(accessible); } catch (Exception ignored) {}
                }
            }
        }
    }

    private static List<Field> getAllFields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        Class<?> cur = cls;
        while (cur != null && cur != Object.class) {
            fields.addAll(Arrays.asList(cur.getDeclaredFields()));
            cur = cur.getSuperclass();
        }
        return fields;
    }

    private void callAnnotatedMethods(Object instance, Class<? extends Annotation> annotation) {
        Class<?> cls = instance.getClass();
        for (Method m : getAllMethods(cls)) {
            if (m.isAnnotationPresent(annotation)) {
                boolean accessible = m.canAccess(instance);
                try {
                    m.setAccessible(true);
                    if (m.getParameterCount() > 0) throw new IllegalStateException("Lifecycle methods must be no-arg: " + m);
                    m.invoke(instance);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                } finally {
                    try { m.setAccessible(accessible); } catch (Exception ignored) {}
                }
            }
        }
    }

    private static List<Method> getAllMethods(Class<?> cls) {
        List<Method> methods = new ArrayList<>();
        Class<?> cur = cls;
        while (cur != null && cur != Object.class) {
            methods.addAll(Arrays.asList(cur.getDeclaredMethods()));
            cur = cur.getSuperclass();
        }
        return methods;
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiateByConstructor(Class<? extends T> implType) {
        try {
            Constructor<?>[] ctors = implType.getDeclaredConstructors();
            Constructor<?> injectCtor = null;
            for (Constructor<?> c : ctors) if (c.isAnnotationPresent(Inject.class)) injectCtor = c;
            if (injectCtor == null) {
                if (ctors.length == 1) injectCtor = ctors[0];
                else {
                    try {
                        injectCtor = implType.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException("No @Inject constructor and no default constructor for " + implType);
                    }
                }
            }
            boolean accessible = injectCtor.canAccess(null);
            try {
                injectCtor.setAccessible(true);
                Object[] args = Arrays.stream(injectCtor.getParameters()).map(p -> resolveParameter(p)).toArray();
                return (T) injectCtor.newInstance(args);
            } finally {
                try { injectCtor.setAccessible(accessible); } catch (Exception ignored) {}
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object resolveParameter(Parameter p) {
        Class<?> t = p.getType();
        Named n = p.getAnnotation(Named.class);
        if (n != null) return getBean(t, n.value());
        return getBean(t);
    }

    @Override
    public void close() {
        List<BeanDefinition<?>> copy = new ArrayList<>(allDefs);
        Collections.reverse(copy);
        for (BeanDefinition<?> d : copy) {
            if (d.scope == Scope.Type.SINGLETON && d.singletonInstance != null) {
                callAnnotatedMethods(d.singletonInstance, PreDestroy.class);
            }
        }
    }

    public void debugDump() {
        System.out.println("Registered beans:");
        for (BeanDefinition<?> d : allDefs) {
            System.out.printf(" - exposed=%s impl=%s (name=%s, scope=%s, primary=%s, allowMultiple=%s)\n", d.exposedType.getName(), d.implType.getName(), d.name, d.scope, d.primary, d.allowMultiple);
        }
    }

    
}
