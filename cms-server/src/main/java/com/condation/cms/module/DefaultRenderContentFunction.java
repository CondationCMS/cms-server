package com.condation.cms.module;

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



import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.content.RenderContentFunction;
import com.condation.cms.content.ContentResolver;
import com.condation.cms.request.RequestContextFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRenderContentFunction implements RenderContentFunction {

	private final ContentResolver contentResolver;
	private final RequestContextFactory requestContextFactory;
	
	@Override
	public Optional<ContentResponse> render(String uri, Map<String, List<String>> parameters) {
		try (
				var requestContext = requestContextFactory.create(null, uri, parameters);) {
			
			return contentResolver.getContent(requestContext);
		} catch (Exception e) {
			log.error("", e);
		}
		return Optional.empty();
	}
	
}
