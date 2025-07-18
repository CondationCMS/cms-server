package	com.condation.cms.modules.system.tags;

/*-
 * #%L
 * cms-auth
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

import com.condation.cms.api.extensions.RegisterTagsExtensionPoint;
import com.condation.cms.api.feature.features.SiteMediaServiceFeature;
import com.condation.cms.api.model.Parameter;
import com.condation.modules.api.annotation.Extension;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author thmar
 */
@Extension(RegisterTagsExtensionPoint.class)
public class ImageTags extends RegisterTagsExtensionPoint {

	@Override
	public Map<String, Function<Parameter, String>> tags() {
		return Map.of(
				"cms:image", this::getImage
		);
	}
	
	private String getImage (Parameter param) {
		var imageFile = (String)param.getOrDefault("image", "");
		var format = (String)param.get("format");
		var alt = param.getOrDefault("alt", "");
		
		if (imageFile.startsWith("/")) {
			imageFile = imageFile.substring(1);
		}
		
		var mediaService = requestContext.get(SiteMediaServiceFeature.class).mediaService();
		
		var media = mediaService.get(imageFile);
		
		var mediaUrl = "/assets/" + media.uri();
		if (!Strings.isNullOrEmpty(format)) {
			mediaUrl = "/media/" + media.uri() + "?format=" + format;
		}
		var altText = (String)media.meta().getOrDefault("alt", alt);
		
		return "<img src=\"%s\" alt=\"%s\" width=\"%d\" height=\"%d\" />"
				.formatted(
						mediaUrl,
						StringEscapeUtils.ESCAPE_HTML4.translate(altText),
						media.meta().getOrDefault("width", -1),
						media.meta().getOrDefault("height", -1)
				);
	}
	
}
