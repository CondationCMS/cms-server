package com.condation.cms.content.shortcodes;

/*-
 * #%L
 * cms-content
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
import com.condation.cms.api.annotations.ShortCode;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class ShortCodes {

	private final TagMap tagMap;
	private final TagParser parser;

	public ShortCodes(Map<String, Function<Parameter, String>> codes, TagParser tagParser) {
		this.parser = tagParser;
		this.tagMap = new TagMap();
		this.tagMap.putAll(codes);
	}

	public Set<String> getShortCodeNames() {
		return tagMap.names();
	}

	public String replace(final String content) {
		return replace(content, Collections.emptyMap(), null);
	}

	public String replace(final String content, Map<String, Object> contextModel) {
		return replace(content, contextModel, null);
	}

	public String replace(final String content, Map<String, Object> contextModel, RequestContext requestContext) {
		return parser.parse(content, tagMap, contextModel, requestContext);
	}

	public String execute(String name, Map<String, Object> parameters, RequestContext requestContext) {
		if (!tagMap.has(name)) {
			return "";
		}
		try {
			Parameter params;
			if (parameters != null) {
				params = new Parameter(parameters, requestContext);
			} else {
				params = new Parameter(requestContext);
			}
			return tagMap.get(name).apply(params);
		} catch (Exception e) {
			log.error("", e);
		}
		return "";
	}

	public static ShortCodes.Builder builder(TagParser tagParser) {
		return new Builder(tagParser);
	}

	public static class Builder {

		private final TagParser tagParser;

		private final Map<String, Function<Parameter, String>> codes = new HashMap<>();

		private Builder(TagParser tagParser) {
			this.tagParser = tagParser;
		}

		public Builder register(String name, Function<Parameter, String> shortCodeFN) {
			codes.put(name, shortCodeFN);
			return this;
		}
		
		public Builder register (Map<String, Function<Parameter, String>> codes) {
			this.codes.putAll(codes);
			return this;
		}

		public Builder register (List<Object> handlers) {
			if (handlers == null || handlers.isEmpty()) {
				return this;
			}
			
			handlers.forEach(this::register);
			
			return this;
		}
		
		public Builder register(Object handler) {
			if (handler == null) {
				return this;
			}

			Class<?> clazz = handler.getClass();
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(ShortCode.class)) {
					if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Parameter.class) {
						method.setAccessible(true); // falls private
						ShortCode annotation = method.getAnnotation(ShortCode.class);
						String key = annotation.value();

						codes.put(key, param -> {
							try {
								return (String) method.invoke(handler, param);
							} catch (Exception e) {
								throw new RuntimeException("Error calling shortcode: " + key, e);
							}
						});
					} else {
						log.error("ignore methode" + method.getName() + " – wrong signature.");
					}
				}
			}

			return this;
		}

		public ShortCodes build() {
			return new ShortCodes(codes, tagParser);
		}
	}
}
