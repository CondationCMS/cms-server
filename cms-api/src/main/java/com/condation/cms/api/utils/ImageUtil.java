package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author thmar
 */
public class ImageUtil {

	/**
	 * This method takes image pathes like
	 *
	 * /de/assets/test.jpg /de/media/test.jpg /assets/test.jpg /media/test.jpg
	 *
	 * and extract the real path of the image and returns: test.jpg
	 *
	 * @param image
	 * @param requestContext
	 * @return raw image path
	 */
	public static String getRawPath(String image, RequestContext requestContext) {
		if (image == null || image.isEmpty()) {
			return "";
		}

		String normalized = image.trim();

		if (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}

		String contextPath = requestContext.get(SitePropertiesFeature.class).siteProperties().contextPath();
		if (contextPath != null && !contextPath.isEmpty()) {
			contextPath = contextPath.replaceAll("^/+", "").replaceAll("/+$", "");
			if (normalized.startsWith(contextPath + "/")) {
				normalized = normalized.substring(contextPath.length() + 1);
			}
		}
		for (var path : List.of("assets", "media")) {
			if (normalized.startsWith(path + "/")) {
				normalized = normalized.substring(path.length() + 1);
			}
		}

		return normalized;
	}
}
