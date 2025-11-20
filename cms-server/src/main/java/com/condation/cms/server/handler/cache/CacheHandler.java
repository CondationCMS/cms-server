package com.condation.cms.server.handler.cache;

/*-
 * #%L
 * cms-server
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

import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;

public class CacheHandler extends Handler.Wrapper {

	private final List<String> cachedContentTypes = new ArrayList<>();
	private final List<HttpHeader> cachedHeaders = new ArrayList<>();
	private final ICache<CachedKey, CachedResponse> responseCache;

	public CacheHandler(final Handler wrapped, final CacheManager cacheManager) {
		super(wrapped);

		this.responseCache = cacheManager.get(
				"responseCache",
				new CacheManager.CacheConfig(100L, Duration.ofSeconds(5))
		);

		// supported content types
		cachedContentTypes.add("text/html");
		cachedContentTypes.add("text/plain");
		cachedContentTypes.add("text/css");
		cachedContentTypes.add("text/javascript");
		cachedContentTypes.add("application/javascript");
		cachedContentTypes.add("application/json");

		cachedHeaders.add(HttpHeader.CONTENT_TYPE);
		cachedHeaders.add(HttpHeader.LOCATION);
	}

	private boolean matchesContentType(String contentType) {
		if (contentType == null) return false;
		return cachedContentTypes.stream().anyMatch(contentType::startsWith);
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		// Only cache GET requests
		if (!request.getMethod().equalsIgnoreCase("GET")) {
			return super.handle(request, response, callback);
		}

		CachedKey key = new CachedKey(request.getHttpURI().getPathQuery());

		// check cache
		CachedResponse cached = responseCache.get(key);
		if (cached != null) {

			// restore headers
			cached.headers.forEach((name, value) -> {
				response.getHeaders().add(name, value);
			});

			// write cached body
			ByteBuffer buf = ByteBuffer.wrap(cached.body);
			Content.Sink.write(response, true, buf);

			// complete callback
			callback.succeeded();
			return true;
		}

		// wrap response
		CacheResponseWrapper wrapper = new CacheResponseWrapper(request, response);

		return super.handle(request, wrapper, new Callback.Nested(callback) {
			@Override
			public void succeeded() {
				try {
					String contentType = wrapper.getHeaders().get(HttpHeader.CONTENT_TYPE);
					int status = response.getStatus();

					if (status == 200 && matchesContentType(contentType)) {

						byte[] body = wrapper.getContentBytes();
						Map<String, String> headers = getHeaders(wrapper);

						responseCache.put(key, new CachedResponse(body, headers));

						ByteBuffer buf = ByteBuffer.wrap(body);
						Content.Sink.write(response, true, buf);
						callback.succeeded();
						return;
					}

					// no caching → just complete original callback
					callback.succeeded();

				} catch (Exception ex) {
					callback.failed(ex);
				}
			}

			@Override
			public void failed(Throwable x) {
				callback.failed(x);
			}
		});
	}

	private Map<String, String> getHeaders(Response response) {
		Map<String, String> map = new HashMap<>();
		for (HttpHeader h : cachedHeaders) {
			if (response.getHeaders().contains(h)) {
				map.put(h.asString(), response.getHeaders().get(h));
			}
		}
		return map;
	}

	private record CachedKey(String path) implements Serializable {}
	private record CachedResponse(byte[] body, Map<String, String> headers) implements Serializable {}

	private static class CacheResponseWrapper extends Response.Wrapper {

		private final ByteArrayOutputStream bout = new ByteArrayOutputStream();

		public CacheResponseWrapper(Request request, Response wrapped) {
			super(request, wrapped);
		}

		@Override
		public void write(boolean last, ByteBuffer byteBuffer, Callback callback) {
			byte[] arr = new byte[byteBuffer.remaining()];
			byteBuffer.get(arr);
			bout.write(arr, 0, arr.length);

			// forward to underlying response
			super.write(last, ByteBuffer.wrap(arr), callback);
		}

		byte[] getContentBytes() {
			return bout.toByteArray();
		}
	}
}
