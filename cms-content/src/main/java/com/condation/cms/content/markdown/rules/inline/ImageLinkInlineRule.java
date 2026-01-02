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
import com.github.slugify.Slugify;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.InlineElementRule;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class ImageLinkInlineRule implements InlineElementRule {

	static final Slugify SLUG = Slugify.builder().build();

	static final String IMAGE_PATTERN = "!\\[(?<alt>[^\\[\\]]*)\\]\\((?<image>[^\\s\\)]+)(?: \"(?<title>[^\"]*)\")?\\)";
	static final Pattern PATTERN = Pattern.compile("\\[(" + IMAGE_PATTERN + ")\\]\\((?<url>.*?)\\)");

	@Override
	public InlineBlock next(String md) {
		var matcher = PATTERN.matcher(md);

		if (matcher.find()) {
			var href = matcher.group("url");
			var title = matcher.group("title");
			var imageSrc = matcher.group("image");
			var alt = matcher.group("alt");

			var id = SLUG.slugify(alt);

			

			if (RequestContextScope.REQUEST_CONTEXT.isBound()
					&& isInternalUrl(href)) {
				
				var requestContext = RequestContextScope.REQUEST_CONTEXT.get();
				
				href = HTTPUtil.modifyUrl(href, requestContext);
			}

			return new ImageLinkBlock(matcher.start(), matcher.end(), href, id, imageSrc, alt, title);
		}

		return null;
	}

	private boolean isInternalUrl(final String href) {
		return !href.startsWith("http") && !href.startsWith("https");
	}

	public static record ImageLinkBlock(int start, int end, String href, String id, String imageSrc, String alt, String title)
			implements InlineBlock {

		@Override
		public String render() {

			if (title != null && !"".equals(title)) {
				return "<a href=\"%s\" id=\"%s\"><img src=\"%s\" alt=\"%s\" title=\"%s\" /></a>".formatted(
						href,
						id,
						imageSrc,
						alt,
						title);
			}

			return "<a href=\"%s\" id=\"%s\" ><img src=\"%s\" alt=\"%s\" /></a>".formatted(
					href,
					id,
					imageSrc,
					alt);
		}
	}
}
