package com.condation.cms.server.handler.api;

/*-
 * #%L
 * cms-server
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

public class APIRegistry {

    private Map<String, Route> routes = new HashMap<>();

    public void register (String path, String method, BiFunction<Request, Response, Boolean> handler) {
        routes.put(path, new Route(path, method, handler));
    }

    public static record Route (String path, String method, BiFunction<Request, Response, Boolean> handler) {};
}
