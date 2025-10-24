package com.condation.cms.api.db;

/*-
 * #%L
 * cms-api
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.DateRange;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.api.utils.SectionUtil;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public record ContentNode(String uri, String name, Map<String, Object> data,
		boolean directory, Map<String, ContentNode> children, LocalDate lastmodified) implements Serializable {

	public ContentNode(String uri, String name, Map<String, Object> data, boolean directory, Map<String, ContentNode> children) {
		this(uri, name, data, directory, children, LocalDate.now());
	}

	public ContentNode(String uri, String name, Map<String, Object> data, boolean directory) {
		this(uri, name, data, directory, new HashMap<>(), LocalDate.now());
	}

	public ContentNode(String uri, String name, Map<String, Object> data) {
		this(uri, name, data, false, new HashMap<>(), LocalDate.now());
	}

	public ContentNode(String uri, String name, Map<String, Object> data, LocalDate lastmodified) {
		this(uri, name, data, false, new HashMap<>(), lastmodified);
	}

	public String nodeType () {
		return (String)data.getOrDefault(Constants.MetaFields.TYPE, Constants.NodeType.PAGE);
	}
	public boolean isView () {
		return Constants.NodeType.VIEW.equals(nodeType());
	}
	
	public String contentType() {
		String defaultContentType = Constants.DEFAULT_CONTENT_TYPE;
		if (RequestContextScope.REQUEST_CONTEXT.isBound()) {
			RequestContext requestContext = RequestContextScope.REQUEST_CONTEXT.get();
			defaultContentType = requestContext.get(SitePropertiesFeature.class).siteProperties().defaultContentType();
		}
		return (String) ((Map<String, Object>) data
				.getOrDefault("content", Map.of()))
				.getOrDefault("type", defaultContentType);
	}
	
	public boolean hasMetaValue (final String field) {
		return MapUtil.getValue(data, field) != null;
	}
	
	public <T> T getMetaValue (final String field, final T defaultValue) {
		return MapUtil.getValue(data, field, defaultValue);
	}
	
	public <T> Optional<T> getMetaValue (final String field, final Class<T> type) {
		var value = MapUtil.getValue(data, field);
		return Optional.ofNullable((T)value);
	}

	public boolean isDirectory() {
		return directory;
	}

	public boolean isHidden() {
		return name.startsWith(".");
	}

	public boolean isDraft() {
		return !((boolean) data().getOrDefault(Constants.MetaFields.PUBLISHED, false));
	}
	
	public boolean isParentPathHidden () {
		return uri().startsWith(".") || uri().contains("/.");
	}

	public boolean isVisible() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)) {
			return true;
		}
		
		if (isDraft()) {
			return false;
		}
		
		var publish_date = (Date) data.getOrDefault(Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now()));
		
		var unpublish_date = (Date) data.getOrDefault(Constants.MetaFields.UNPUBLISH_DATE, null);
		
		return DateRange.isNowWithin(publish_date, unpublish_date);
	}

	public boolean isSection() {
		return SectionUtil.isSection(name);
	}

	public boolean isRedirect() {
		return MapUtil.getValue(data, Constants.MetaFields.REDIRECT_LOCATION) != null;
	}

	public int getRedirectStatus() {
		return MapUtil.getValue(data, Constants.MetaFields.REDIRECT_STATUS, Constants.DEFAULT_REDIRECT_STATUS);
	}

	public String getRedirectLocation() {
		return MapUtil.getValue(data, Constants.MetaFields.REDIRECT_LOCATION, "");
	}

	@Override
	public boolean equals(Object other) {

		if (this == other) {
			return true;
		}

		if (other == null) {
			return false;
		}

		if (!(other instanceof ContentNode)) {
			return false;
		}

		var otherNode = (ContentNode) other;

		return uri.equals(otherNode.uri);
	}
}
