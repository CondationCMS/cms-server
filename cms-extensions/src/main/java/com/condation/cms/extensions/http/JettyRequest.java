package com.condation.cms.extensions.http;

/*-
 * #%L
 * cms-extensions
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

import com.condation.cms.api.utils.HTTPUtil;
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
public class JettyRequest {

	private final org.eclipse.jetty.server.Request original;

	private String body = "";
	public boolean bodyRead = false;

	public String getBody() {
		return getBody(StandardCharsets.UTF_8);
	}
	
	public String getMethod () {
		return original.getMethod();
	}

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

	public List<String> getQueryParamter(final String name) {
		var queryParameters = HTTPUtil.queryParameters(original.getHttpURI().getQuery());
		if (queryParameters.containsKey(name)) {
			return queryParameters.get(name);
		}
		return Collections.emptyList();
	}

	public List<String> getQueryParamters() {
		var queryParameters = HTTPUtil.queryParameters(original.getHttpURI().getQuery());
		return new ArrayList<>(queryParameters.keySet());
	}
}
