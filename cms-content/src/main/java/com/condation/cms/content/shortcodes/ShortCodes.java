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


import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import java.util.Collections;
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

	public ShortCodes (Map<String, Function<Parameter, String>> codes, TagParser tagParser) {
		this.parser = tagParser;
		this.tagMap = new TagMap();
		this.tagMap.putAll(codes);
	}
	
	public Set<String> getShortCodeNames () {
		return tagMap.names();
	}
	
	public String replace (final String content) {
		return replace(content, Collections.emptyMap(), null);
	}
	
	public String replace (final String content, Map<String, Object> contextModel) {
		return replace(content, contextModel, null);
	}
	
	public String replace (final String content, Map<String, Object> contextModel, RequestContext requestContext) {
		return parser.parse(content, tagMap, contextModel);
	}
	
	public String execute (String name, Map<String, Object> parameters, RequestContext requestContext) {
		if (!tagMap.has(name)) {
			return "";
		}
		try {
			Parameter params;
			if (parameters != null) {
				params = new Parameter(parameters);
			} else {
				params = new Parameter();
			}
			return tagMap.get(name).apply(params);
		} catch (Exception e) {
			log.error("",e);
		}
		return "";
	}
}
