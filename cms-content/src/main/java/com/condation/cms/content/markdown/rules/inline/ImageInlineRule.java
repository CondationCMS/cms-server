package com.condation.cms.content.markdown.rules.inline;

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


import com.condation.cms.api.feature.features.SiteMediaServiceFeature;
import com.condation.cms.api.utils.ImageUtil;
import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.InlineElementRule;
import com.google.common.base.Strings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class ImageInlineRule implements InlineElementRule {
	
	public static final Pattern PATTERN = Pattern.compile("!\\[(?<alt>[^\\]]*)\\]\\((?<url>[^\\s)]+)(?: \"(?<title>[^\"]*)\")?\\)");

	@Override
	public InlineBlock next(String md) {
		Matcher matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			return new ImageInlineRule.ImageInlineBlock(matcher.start(), matcher.end(), 
					matcher.group("url").trim(), matcher.group("alt").trim(), matcher.group("title"));
		}
		return null;
	}
	
	public static record ImageInlineBlock(int start, int end, String src, String alt, String title) implements InlineBlock {

		@Override
		public String render() {
			var altText = alt;
			var requestContext = getRequestContext();
			if (Strings.isNullOrEmpty(altText) && requestContext.isPresent()) {
				var imageUrl = ImageUtil.getRawPath(src, requestContext.get());
				var media = requestContext.get().get(SiteMediaServiceFeature.class).mediaService().get(imageUrl);
			
				if (media != null && media.meta().containsKey("alt")) {
					altText = (String) media.meta().get("alt");
				}
			}
			
			var uiSelector = "";
			if (isPreview()) {
				uiSelector = " data-cms-ui-selector=\"content-image\" ";
			}
			
			if (title != null && !"".equals(title.trim())) {
				return "<img src=\"%s\" alt=\"%s\" title=\"%s\" %s />".formatted(src, altText, title, uiSelector);
			}
			return  "<img src=\"%s\" alt=\"%s\" %s />".formatted(src, altText, uiSelector);
		}
	}
}
