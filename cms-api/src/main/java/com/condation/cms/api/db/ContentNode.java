package com.condation.cms.api.db;

/*-
 * #%L
 * CMS Api
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
import com.condation.cms.api.workflow.DefaultWFStatusProvider;
import com.condation.cms.api.Constants;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.api.utils.SectionUtil;
import com.google.common.math.DoubleMath;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public record ContentNode(String uri, String url, String name, Map<String, Object> data,
        boolean directory, Map<String, ContentNode> children, LocalDate lastmodified) implements Serializable {

    public ContentNode(String uri, String url, String name, Map<String, Object> data, boolean directory, Map<String, ContentNode> children) {
        this(uri, url, name, data, directory, children, LocalDate.now());
    }

    public ContentNode(String uri, String url, String name, Map<String, Object> data, boolean directory) {
        this(uri, url, name, data, directory, new HashMap<>(), LocalDate.now());
    }

    public ContentNode(String uri, String url, String name, Map<String, Object> data) {
        this(uri, url, name, data, false, new HashMap<>(), LocalDate.now());
    }

    public ContentNode(String uri, String url, String name, Map<String, Object> data, LocalDate lastmodified) {
        this(uri, url, name, data, false, new HashMap<>(), lastmodified);
    }

    public String path () {
        return uri;
    }

    public String nodeType() {
        return (String) data.getOrDefault(Constants.MetaFields.TYPE, Constants.NodeType.PAGE);
    }

    public boolean isView() {
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

    public boolean hasMetaValue(final String field) {
        return MapUtil.getValue(data, field) != null;
    }

    public <T> T getMetaValue(final String field, final T defaultValue) {
        return MapUtil.getValue(data, field, defaultValue);
    }

    public <T> Optional<T> getMetaValue(final String field, final Class<T> type) {
        var value = MapUtil.getValue(data, field);
        return Optional.ofNullable((T) value);
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isHidden() {
        return name.startsWith(".");
    }

    /**
     * @deprecated use {@link WFStatusProvider#isPublished(ContentNode)} or
     * {@link NodeVisibility#isVisible(ContentNode)} depending on intent.
     */
    @Deprecated(since = "8.3.0", forRemoval = false)
    public boolean isDraft() {
        return !new DefaultWFStatusProvider().isPublished(this);
    }

    public boolean isParentPathHidden() {
        return uri().startsWith(".") || uri().contains("/.");
    }

    /**
     * @deprecated use {@link NodeVisibility#isVisible(ContentNode)} instead.
     */
    @Deprecated(since = "8.3.0", forRemoval = false)
    public boolean isVisible() {
        return NodeVisibility.isVisible(this);
    }

    public boolean isSectionEntry() {
        return SectionUtil.isSectionEntry(name);
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
