package com.condation.cms.content.shortcodes;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.shortcodes.annotation.AnnotationShortCodeRegistrar;
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

	private final ShortCodeMap tagMap;
	private final ShortCodeParser parser;

	public ShortCodes(Map<String, Function<Parameter, String>> codes, ShortCodeParser tagParser) {
		this.parser = tagParser;
		this.tagMap = new ShortCodeMap();
		this.tagMap.putAll(codes);
	}

	public Set<String> getTagNames() {
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

	public static ShortCodes.Builder builder(ShortCodeParser tagParser) {
		return new Builder(tagParser);
	}

	public static class Builder {

		private final ShortCodeParser tagParser;
		private final Map<String, Function<Parameter, String>> shortCodes = new HashMap<>();
		private final AnnotationShortCodeRegistrar annotationRegistrar = new AnnotationShortCodeRegistrar();

		private Builder(ShortCodeParser tagParser) {
			this.tagParser = tagParser;
		}

		public Builder register(String name, Function<Parameter, String> tagFN) {
			shortCodes.put(name, tagFN);
			return this;
		}

		public Builder register(Map<String, Function<Parameter, String>> codes) {
			shortCodes.putAll(codes);
			return this;
		}

		public Builder register(Object handler) {
			annotationRegistrar.register(handler, shortCodes);
			return this;
		}

		public ShortCodes build() {
			return new ShortCodes(shortCodes, tagParser);
		}
	}
}
