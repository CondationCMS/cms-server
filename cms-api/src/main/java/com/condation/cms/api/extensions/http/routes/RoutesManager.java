package com.condation.cms.api.extensions.http.routes;

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

import com.condation.cms.api.annotations.Route;
import com.condation.cms.api.extensions.http.HttpHandler;
import com.condation.cms.api.extensions.http.PathMapping;
import org.eclipse.jetty.http.pathmap.PathSpec;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

@Slf4j
public class RoutesManager {

    private final PathMapping pathMapping = new PathMapping();

	public Optional<HttpHandler> findFirst (String path, String method) {
		return pathMapping.getMatchingHandler(path, method);
	}
	
    public void register(Object controller) {
        Class<?> clazz = controller.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            Route route = method.getAnnotation(Route.class);
            if (route != null && isValidHandlerMethod(method)) {
                method.setAccessible(true);

                PathSpec pathSpec = PathSpec.from(route.value());

                HttpHandler handler = (request, response, callback) -> {
                    try {
						return (Boolean) method.invoke(controller, request, response, callback);
                    } catch (Exception e) {
                        log.error("", e);
                        response.setStatus(500);
						return true;
                    }
                };
				pathMapping.add(pathSpec, route.method(), handler);
            }
        }
    }

    private boolean isValidHandlerMethod(Method method) {
        // Muss "boolean handle(Request, Response, Callback)" sein
        if (!Modifier.isPublic(method.getModifiers())) return false;
        if (!method.getReturnType().equals(boolean.class)) return false;

        Class<?>[] params = method.getParameterTypes();
        return params.length == 3 &&
               Request.class.isAssignableFrom(params[0]) &&
               Response.class.isAssignableFrom(params[1]) &&
               Callback.class.isAssignableFrom(params[2]);
    }
}
