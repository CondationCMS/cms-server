package com.condation.cms.content.usage;

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

import com.condation.cms.api.usage.Usage;
import com.condation.cms.api.usage.UsageResource;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Resolves public URLs using host/context routing, and typed fields using their site-local contract. */
final class UsageReferenceResolver {
    private final UsageSite site;
    private final PublicTargetLookup targets;

    UsageReferenceResolver(UsageSite site, PublicTargetLookup targets) {
        this.site = site;
        this.targets = targets;
    }

    Optional<UsageResource> resolve(UsageDocument source, UsageReference ref) throws IOException {
        String value = ref.value().trim();
        if (value.isEmpty() || value.startsWith("#")) return Optional.empty();
        URI uri = URI.create(value.replace(" ", "%20"));
        if (uri.getScheme() != null && !isHttp(uri)) return Optional.empty();
        if (uri.getRawAuthority() != null && !isHttp(uri) && !ref.publicUrl()) return Optional.empty();

        if (ref.origin() == Usage.Origin.MARKDOWN && ref.kind() == UsageResource.Kind.CONTENT
                && !uri.isAbsolute() && uri.getRawAuthority() == null) {
            uri = URI.create(com.condation.cms.api.utils.HTTPUtil.prependContext(value, site.properties())
                    .replace(" ", "%20"));
        }

        if (ref.publicUrl() || uri.isAbsolute() || uri.getRawAuthority() != null) {
            if (!uri.isAbsolute() && uri.getRawAuthority() == null && source.publicPath() == null
                    && !value.startsWith("/")) {
                return Optional.of(new UsageResource(site.id(), UsageResource.Kind.UNRESOLVED_URL, value));
            }
            var base = URI.create(site.properties().baseUrl());
            String publicPath = context(site) + (source.publicPath() == null ? "/" : source.publicPath());
            var sourceUrl = base.resolve(publicPath);
            var url = sourceUrl.resolve(uri).normalize();
            if (!matchesHost(site, url) || !matchesContext(url.getPath(), context(site))) {
                if (uri.isAbsolute() || uri.getRawAuthority() != null) return Optional.empty();
                return Optional.of(new UsageResource(site.id(), UsageResource.Kind.UNRESOLVED_URL, url.getPath()));
            }
            String path = url.getPath().substring(context(site).length());
            return Optional.of(publicTarget(path.isEmpty() ? "/" : path));
        }

        String siteId = ref.targetSite() == null ? site.id() : ref.targetSite();
        String path = normalize(uri.getPath());
        if (ref.kind() == UsageResource.Kind.COLLECTION_ITEM) {
            siteId = site.collectionSite(ref.collection());
            // CollectionField stores the filename stem, which may itself end in ".md".
            com.condation.cms.api.db.collection.CollectionItemId.requireValid(path);
            String item = path + ".md";
            return Optional.of(new UsageResource(siteId, ref.kind(), ref.collection() + "/" + item));
        }
        if (ref.kind() == UsageResource.Kind.CONTENT && siteId.equals(site.id())) {
            var node = site.db().getContent().byPath(path);
            if (node.isEmpty()) node = site.db().getContent().byUrl("/" + path);
            if (node.isPresent()) path = node.get().path();
        }
        return Optional.of(new UsageResource(siteId, ref.kind(), path));
    }

    private UsageResource publicTarget(String path) throws IOException {
        return findPublicTarget(path);
    }

    private UsageResource findPublicTarget(String path) throws IOException {
        String normalized = normalize(path);
        for (var prefix : new String[]{"media/", "assets/"}) {
            if (normalized.startsWith(prefix)) {
                return new UsageResource(site.id(), UsageResource.Kind.MEDIA, normalized.substring(prefix.length()));
            }
        }
        var node = site.db().getContent().byUrl(path);
        if (node.isPresent()) return new UsageResource(site.id(), UsageResource.Kind.CONTENT, node.get().path());
        String url = com.condation.cms.api.utils.PathUtil.normalizeURL(path);
        var alias = targets.aliasTarget(url);
        if (alias.isPresent()) return alias.get();
        var collection = targets.collectionTarget(url);
        if (collection.isPresent()) return collection.get();
        return new UsageResource(site.id(), UsageResource.Kind.UNRESOLVED_URL, path);
    }

    Usage.TargetStatus status(UsageResource resource) {
        if (!site.id().equals(resource.site()) || resource.kind() == UsageResource.Kind.UNRESOLVED_URL) {
            return Usage.TargetStatus.UNRESOLVED;
        }
        String folder = switch (resource.kind()) {
            case CONTENT -> "content";
            case MEDIA -> "assets";
            case COLLECTION_ITEM -> "collections";
            case UNRESOLVED_URL -> throw new IllegalStateException();
        };
        return Files.isRegularFile(site.root().resolve(folder).resolve(resource.path()))
                ? Usage.TargetStatus.EXISTS : Usage.TargetStatus.MISSING;
    }

    static String context(UsageSite site) {
        String value = site.properties().contextPath();
        return value == null || value.equals("/") ? "" : "/" + value.replaceAll("^/+|/+$", "");
    }

    private static boolean matchesContext(String path, String context) {
        return context.isEmpty() || path.equals(context) || path.startsWith(context + "/");
    }

    private static boolean matchesHost(UsageSite site, URI url) {
        var base = URI.create(site.properties().baseUrl());
        if (url.getHost() == null) return false;
        boolean hostMatches = url.getHost().equalsIgnoreCase(base.getHost())
                || (site.properties().hostnames() != null && site.properties().hostnames().stream()
                    .anyMatch(host -> host.equalsIgnoreCase(url.getHost())));
        return hostMatches && port(base) == port(url);
    }

    private static int port(URI uri) {
        return uri.getPort() >= 0 ? uri.getPort() : "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static boolean isHttp(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
    }

    private static String normalize(String path) {
        String result = Path.of(path == null || path.isBlank() ? "." : path.replaceAll("^/+", ""))
                .normalize().toString().replace('\\', '/');
        if (result.equals("..") || result.startsWith("../")) throw new IllegalArgumentException("path escapes site root");
        return result.equals(".") ? "" : result;
    }

    interface PublicTargetLookup {
        Optional<UsageResource> aliasTarget(String path) throws IOException;
        Optional<UsageResource> collectionTarget(String path) throws IOException;
    }
}
