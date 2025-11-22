package com.condation.cms.server.filter;

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
import com.condation.cms.api.PerformanceProperties;
import com.condation.cms.api.annotations.Experimental;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.request.RequestContextFactory;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import stormpot.Allocator;
import stormpot.Expiration;
import stormpot.Pool;
import stormpot.Poolable;
import stormpot.Slot;
import stormpot.Timeout;

/**
 *
 * @author t.marx
 */
@Experimental(since = "5.3.0")
@Slf4j
public class PooledRequestContextFilter extends Handler.Wrapper {

	public static final String REQUEST_CONTEXT = "_requestContext";

	Pool<RequestContextPoolable> requestContextPool;

	private final PerformanceProperties properties;

	public PooledRequestContextFilter(final Handler handler, final RequestContextFactory requestContextFactory,
			final PerformanceProperties properties) {
		super(handler);
		this.properties = properties;

		requestContextPool = Pool.from(new RequestContextAllocator(requestContextFactory))
				.setExpiration(Expiration.after(properties.pool_expire(), TimeUnit.SECONDS))
				.setSize(properties.pool_size())
				.build();
	}

	@Override
	public boolean handle(Request httpRequest, Response rspns, Callback clbck) throws Exception {
		var requestContextPoolable = requestContextPool.claim(new Timeout(Duration.ofSeconds(1)));
		var requestContext = requestContextPoolable.requestContext;
		try {
			
			httpRequest.setAttribute(REQUEST_CONTEXT, requestContext);
			
			var uri = RequestUtil.getContentPath(httpRequest);
			var queryParameters = HTTPUtil.queryParameters(httpRequest.getHttpURI().getQuery());
			var contextPath = httpRequest.getContext().getContextPath();

			requestContext.add(RequestFeature.class, new RequestFeature(contextPath, uri, queryParameters, httpRequest));

			return ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, requestContext).call(() -> {
				return super.handle(httpRequest, rspns, clbck);
			});
		} finally {
			requestContext.features.remove(RequestFeature.class);
			requestContext.features.remove(IsPreviewFeature.class);
			requestContextPoolable.release();
		}
	}

	@RequiredArgsConstructor
	private static class RequestContextAllocator implements Allocator<RequestContextPoolable> {

		private final RequestContextFactory requestContextFactory;

		@Override
		public RequestContextPoolable allocate(Slot slot) throws Exception {
			log.trace("allocate");
			return new RequestContextPoolable(slot, requestContextFactory.createContext());
		}

		@Override
		public void deallocate(RequestContextPoolable poolable) throws Exception {
			log.trace("deallocate");
			poolable.close();
		}

	}

	@RequiredArgsConstructor
	private static class RequestContextPoolable implements Poolable, Closeable {

		private final Slot slot;
		@Getter
		private final RequestContext requestContext;

		@Override
		public void release() {
			slot.release(this);
		}

		@Override
		public void close() throws IOException {
			try {
				requestContext.close();
			} catch (Exception ex) {
				log.error("", ex);
			}
		}

	}

}
