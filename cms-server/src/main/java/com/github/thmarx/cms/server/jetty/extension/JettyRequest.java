package com.github.thmarx.cms.server.jetty.extension;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.extensions.http.Request;
import com.github.thmarx.cms.server.jetty.handler.JettyContentHandler;
import com.github.thmarx.cms.utils.HTTPUtil;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class JettyRequest implements Request {

	private final org.eclipse.jetty.server.Request original;

	private String body = "";
	public boolean bodyRead = false;

	@Override
	public String getBody() {
		return getBody(StandardCharsets.UTF_8);
	}

	@Override
	public String getBody(final Charset charset) {
		if (!bodyRead) {
			try (var inputStream = org.eclipse.jetty.server.Request.asInputStream(original)) {
				
				body = new String(inputStream.readAllBytes(), charset);
				bodyRead = true;
			} catch (Exception ex) {
				log.error("", ex);
			}
		}
		return body;
	}

	@Override
	public List<String> getQueryParamter(final String name) {
		var queryParameters = HTTPUtil.queryParameters(original.getHttpURI().getQuery());
		if (queryParameters.containsKey(name)) {
			return queryParameters.get(name);
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getQueryParamters() {
		var queryParameters = HTTPUtil.queryParameters(original.getHttpURI().getQuery());
		return new ArrayList<>(queryParameters.keySet());
	}
}
