package com.condation.cms.content;

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

import com.condation.cms.content.tags.ShortCodeParser;
import com.condation.cms.content.tags.TagParser;
import org.apache.commons.jexl3.JexlBuilder;

/**
 *
 * @author t.marx
 */
public abstract class ContentBaseTest {
	
	private ShortCodeParser shortCodeParser;
	
	private TagParser tagParser;
	
	public TagParser getTagParser () {
		if (tagParser == null) {
			tagParser = new TagParser(
					new JexlBuilder().cache(512).strict(true).silent(false).create()
			);
		}
		
		return tagParser;
	}
	
	public ShortCodeParser getShortCodeParser () {
		if (shortCodeParser == null) {
			shortCodeParser = new ShortCodeParser(
			);
		}
		
		return shortCodeParser;
	}
}
