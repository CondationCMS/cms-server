package com.condation.cms.templates;

/*-
 * #%L
 * cms-templates
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

import com.condation.cms.content.shortcodes.ShortCodes;
import com.condation.cms.templates.tags.shortcode.EndShortCodeTag;
import com.condation.cms.templates.tags.shortcode.ShortCodeTag;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public record DynamicConfiguration(ShortCodes shortCodes, Map<String, Tag> dynamicTags) {
	
	public DynamicConfiguration {
		for (var tag : shortCodes.getShortCodeNames()) {
			var openTag = new ShortCodeTag(tag, shortCodes);
			var closeTag = new EndShortCodeTag(tag);
			
			dynamicTags.put(openTag.getTagName(), openTag);
			dynamicTags.put(closeTag.getTagName(), closeTag);
		}
	}
	
	public DynamicConfiguration(ShortCodes shortcodes) {
		this(shortcodes, new HashMap<>());
	}
	
	public boolean hasTag (String tagName) {
		return dynamicTags.containsKey(tagName);
	}
	
	public Optional<Tag> getTag (String tagName) {
		return Optional.ofNullable(dynamicTags.get(tagName));
	}
}
