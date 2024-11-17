package com.condation.cms.modules.example;

/*-
 * #%L
 * example-module
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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import com.condation.cms.api.extensions.http.APIHandlerExtensionPoint;
import com.condation.cms.api.extensions.http.HttpHandler;
import com.condation.cms.api.extensions.http.PathMapping;
import com.condation.modules.api.annotation.Extension;

import lombok.RequiredArgsConstructor;

@Extension(APIHandlerExtensionPoint.class)
public class ExampleApiHandlerExtension extends APIHandlerExtensionPoint {

    @Override
    public PathMapping getMapping() {
        PathMapping mapping = new PathMapping();

        mapping.add(PathSpec.from("/test-api"), "GET", new ExampleHandler("CondationCMS test api"));

        return mapping;
    }

    @RequiredArgsConstructor
    private static class ExampleHandler implements HttpHandler {

        private final String message;

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            response.write(true, ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)), callback);
            return true;
        }

    }
}
