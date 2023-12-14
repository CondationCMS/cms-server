package com.github.thmarx.cms.modules.example;

/*-
 * #%L
 * seo-module
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

import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.db.ContentNode;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class SitemapGenerator implements AutoCloseable {

	private final OutputStream output;
	private final SiteProperties siteProperties;
	
	
	public void start () throws IOException {
		output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8));
		output.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">".getBytes(StandardCharsets.UTF_8));
	}
	
	public void addNode (final ContentNode node) throws IOException {
		output.write("<url>".getBytes(StandardCharsets.UTF_8));
		output.write("<loc>%s/%s</loc>".formatted(
				siteProperties.getOrDefault("baseurl", ""),
				patchURi(node.uri())
		).getBytes(StandardCharsets.UTF_8));
		output.write("<lastmod>%s</lastmod>"
				.formatted(DateTimeFormatter.ISO_LOCAL_DATE.format(node.lastmodified()))
				.getBytes(StandardCharsets.UTF_8));
		output.write("</url>".getBytes(StandardCharsets.UTF_8));
	}

	private String patchURi (String uri) {
		if (uri.endsWith("index.md")) {
			uri = uri.replace("index.md", "");
		}
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.lastIndexOf("/"));
		}
		if (uri.endsWith(".md")) {
			uri = uri.substring(0, uri.lastIndexOf("."));
		}
		return uri;
	}
	
	@Override
	public void close() throws Exception {
		output.write("</urlset>".getBytes(StandardCharsets.UTF_8));
		output.close();
	}
}
