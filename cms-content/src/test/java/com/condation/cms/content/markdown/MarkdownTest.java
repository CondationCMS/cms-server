package com.condation.cms.content.markdown;

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


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author t.marx
 */
public abstract class MarkdownTest {
	
	protected String removeComments (final String html) {
		return html.replaceAll("<!--[\\s\\S]*?-->", "");
	}
	
	protected String load (final String file) throws IOException {
		try {
			return Files.readString(Path.of(MarkdownTest.class.getResource(file).toURI()), StandardCharsets.UTF_8);
		} catch (URISyntaxException ex) {
			throw new IOException(ex);
		}
	}
}
