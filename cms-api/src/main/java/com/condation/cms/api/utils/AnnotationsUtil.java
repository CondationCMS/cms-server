package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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
import com.condation.cms.api.exceptions.AnnotationExecutionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
public class AnnotationsUtil {

	public static <A extends Annotation, R> List<CMSAnnotation<A, R>> process(Object target, Class<A> annotation, Class<R> returnType) {
		return process(target, annotation, Collections.emptyList(), returnType);
	}

	public static <A extends Annotation, R> List<CMSAnnotation<A, R>> process(Object target, Class<A> annotationClass, List<Class<?>> parameters, Class<R> returnType) {
		Objects.requireNonNull(target);
		Objects.requireNonNull(annotationClass);
		Objects.requireNonNull(parameters);

		List<CMSAnnotation<A, R>> result = new ArrayList();
		Class<?> clazz = target.getClass();
		for (Method method : clazz.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(annotationClass)) {
				continue;
			}
			if (!hasValidSignatur(method, parameters, returnType)) {
				continue;
			}

			A annotation = method.getAnnotation(annotationClass);

			result.add(new CMSAnnotation<>(annotation, (params) -> {
				try {
					return (R) method.invoke(target, params);
				} catch (IllegalAccessException | InvocationTargetException ex) {
					log.error("", ex);
					throw new AnnotationExecutionException(ex.getMessage());
				}
			}));
		}

		return result;
	}

	private static boolean hasValidSignatur(Method method, List<Class<?>> parameters, Class<?> returnType) {

		if (!Modifier.isPublic(method.getModifiers())) {
			return false;
		}

		if (returnType != Void.class) {
			Class<?> actualReturnType = method.getReturnType();
			if (returnType == Object.class) {
				if (actualReturnType == Void.TYPE) {
					return false;
				}
			} else if (!actualReturnType.equals(returnType)) {
				return false;
			}
		}

		Class<?>[] params = method.getParameterTypes();

		if (params.length != parameters.size()) {
			return false;
		}

		for (int i = 0; i < params.length; i++) {
			if (!params[i].isAssignableFrom(parameters.get(i))) {
				return false;
			}
		}

		return true;
	}

	public record CMSAnnotation<A extends Annotation, R>(A annotation, Function<Object[], R> function) {

		public R invoke(Object... parameters) {
			return function.apply(parameters);
		}
	}
}
