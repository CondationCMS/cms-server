package com.github.thmarx.cms.module;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.request.RequestContextFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class RenderContentFunction implements BiFunction<String, Map<String, List<String>>, Optional<String>> {

	private final Supplier<ContentResolver> contentResolver;
	private final Supplier<RequestContextFactory> requestContextFactory;
	
	@Override
	public Optional<String> apply(String uri, Map<String, List<String>> parameters) {
		try (
				var requestContext = requestContextFactory.get().create(uri, parameters);) {

			return contentResolver.get().getContent(requestContext);
		} catch (Exception e) {
			log.error("", e);
		}
		return Optional.empty();
	}
	
}
