package com.condation.cms.server.handler.cache;

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


import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
public class CacheHandler extends Handler.Wrapper {

	private final List<String> cachedContentTypes = new ArrayList<>();

	private final List<HttpHeader> cachedHeaders = new ArrayList<>();

	private final ICache<CachedKey, CachedResponse> responseCache;

	public CacheHandler(final Handler wrapped, final CacheManager cacheManager) {
		super(wrapped);
		this.responseCache = cacheManager.get(
				"responseCache",
				new CacheManager.CacheConfig(100l, Duration.ofSeconds(5)));
		cachedContentTypes.add("text/html");
		cachedContentTypes.add("test/plain");
		cachedContentTypes.add("test/css");
		cachedContentTypes.add("test/javascript");
		cachedContentTypes.add("application/javascript");
		cachedContentTypes.add("application/json");

		cachedHeaders.add(HttpHeader.CONTENT_TYPE);
		cachedHeaders.add(HttpHeader.LOCATION);
	}

	private boolean matchesContentType(String contentType) {
		return cachedContentTypes.stream().anyMatch(ct -> contentType.startsWith(ct));
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		if (!request.getMethod().equalsIgnoreCase("GET")) {
			return super.handle(request, response, callback);
		}
		
		CachedKey key = new CachedKey(request.getHttpURI().getPathQuery());

		CachedResponse cached = responseCache.get(key);
		if (cached != null) {

			cached.headers.forEach((name, value) -> {
				response.getHeaders().add(
						name,
						value);
			});
			Content.Sink.write(response, true, cached.body, callback);

			return true;
		}

		final CacheResponseWrapper cacheResponse = new CacheResponseWrapper(request, response);
		return super.handle(request, cacheResponse, new Callback.Nested(callback) {
			@Override
			public void succeeded() {
				if (response.getStatus() == 200
						&& matchesContentType(cacheResponse.getHeaders().get(HttpHeader.CONTENT_TYPE))) {
					response.getHeaders().add(
							HttpHeader.CONTENT_TYPE,
							cacheResponse.getHeaders().get(HttpHeader.CONTENT_TYPE));

					var body = cacheResponse.getContent();

					CachedResponse cachedResponse = new CachedResponse(body, getHeaders(cacheResponse));
					responseCache.put(key, cachedResponse);

					Content.Sink.write(response, true, body, callback);
				}

				super.succeeded();
			}
		});
	}

	private Map<String, String> getHeaders(Response response) {
		Map<String, String> headers = new HashMap<>();
		cachedHeaders.forEach(header -> {
			if (response.getHeaders().contains(header)) {
				headers.put(header.asString(), response.getHeaders().get(header));
			}
		});
		return headers;
	}

	private record CachedKey(String path) implements Serializable {

	}
	
	
	private record CachedResponse(String body, Map<String, String> headers) implements Serializable {

	}

	private class CacheResponseWrapper extends Response.Wrapper {

		final ByteArrayOutputStream bout = new ByteArrayOutputStream();

//		final HttpFields.Mutable httpFields = HttpFields.build();

		public CacheResponseWrapper(Request request, Response wrapped) {
			super(request, wrapped);
		}

		public String getContent() {
			return bout.toString(StandardCharsets.UTF_8);
		}

//		@Override
//		public HttpFields.Mutable getHeaders() {
//			return httpFields;
//		}

		@Override
		public void write(boolean last, ByteBuffer byteBuffer, Callback callback) {
			bout.writeBytes(byteBuffer.array());
			super.write(last, byteBuffer, callback);
		}

	}
}
