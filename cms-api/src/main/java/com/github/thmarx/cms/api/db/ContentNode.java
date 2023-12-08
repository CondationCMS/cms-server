package com.github.thmarx.cms.api.db;

/*-
 * #%L
 * cms-api
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

import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.PreviewContext;
import com.github.thmarx.cms.api.utils.SectionUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public record ContentNode(String uri, String name, Map<String, Object> data, 
		boolean directory, Map<String, ContentNode> children, LocalDate lastmodified)  {

		public ContentNode(String uri, String name, Map<String, Object> data, boolean directory, Map<String, ContentNode> children) {
			this(uri, name, data, directory, children, LocalDate.now());
		}
	
		public ContentNode(String uri, String name, Map<String, Object> data, boolean directory) {
			this(uri, name, data, directory, new HashMap<String, ContentNode>(), LocalDate.now());
		}

		public ContentNode(String uri, String name, Map<String, Object> data) {
			this(uri, name, data, false, new HashMap<String, ContentNode>(), LocalDate.now());
		}
		
		public ContentNode(String uri, String name, Map<String, Object> data, LocalDate lastmodified) {
			this(uri, name, data, false, new HashMap<String, ContentNode>(), lastmodified);
		}
		
		public String contentType () {
			return (String) ((Map<String, Object>)data
					.getOrDefault("content", Map.of()))
					.getOrDefault("type", Constants.DEFAULT_CONTENT_TYPE);
		}
		
		public boolean isDirectory() {
			return directory;
		}

		public boolean isHidden() {
			return name.startsWith(".");
		}

		public boolean isDraft() {
			return (boolean) data().getOrDefault(Constants.MetaFields.DRAFT, false);
		}

		public boolean isPublished() {
			if (PreviewContext.IS_PREVIEW.get()) {
				return true;
			}
			var localDate = (Date) data.getOrDefault(Constants.MetaFields.PUBLISHED, Date.from(Instant.now()));
			var now = Date.from(Instant.now());
			return !isDraft() && (localDate.before(now) || localDate.equals(now));
		}

		public boolean isSection() {
			return SectionUtil.isSection(name);
		}
		
		@Override
		public boolean equals (Object other) {
			
			if (this == other) {
				return true;
			}
			
			if (other == null) {
				return false;
			}
			
			if (!(other instanceof ContentNode)) {
				return false;
			}
			
			var otherNode = (ContentNode)other;
			
			return uri.equals(otherNode.uri);
		}
	}
