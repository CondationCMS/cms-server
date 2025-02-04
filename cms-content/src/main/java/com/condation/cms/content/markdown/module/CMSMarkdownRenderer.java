package com.condation.cms.content.markdown.module;

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
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.content.markdown.CMSMarkdown;
import com.condation.cms.content.markdown.Options;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

/**
 *
 * @author t.marx
 */
@Slf4j
public class CMSMarkdownRenderer implements MarkdownRenderer {

	final Slugify slug = Slugify.builder().build();
	
	final CMSMarkdown renderer;
	
	public CMSMarkdownRenderer() {
		renderer = new CMSMarkdown(Options.all());
	}

	@Override
	public void close() {
	}

	@Override
	public String excerpt(String markdown, int length) {
		String content = render(markdown);
		String text = Jsoup.parse(content).text();

		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}

	@Override
	public String render(String markdown) {
		try {
			return renderer.render(markdown);
		} catch (IOException ex) {
			log.error("", ex);
		}
		return markdown;
	}

}
