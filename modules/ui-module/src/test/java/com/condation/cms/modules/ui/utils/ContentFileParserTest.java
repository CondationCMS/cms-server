package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import com.condation.cms.core.content.io.ContentFileParser;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author t.marx
 */
public class ContentFileParserTest {
	
	@Test
    public void testMarkdownFileParser() throws IOException {
        // Lade die Testdatei
        String filePath = "src/test/resources/testdata/test1.md";
        
        // Initialisiere den Parser
        ContentFileParser parser = new ContentFileParser(filePath);
        
        // Überprüfe den Header
        Map<String, Object> header = parser.getHeader();
        assertThat(header).isNotNull();
        assertThat(header).containsEntry("title", "Test Title")
                          .containsEntry("author", "Test Author")
                          .containsEntry("date", "2024-07-30");

        // Überprüfe den Inhalt
        String content = parser.getContent();
        String expectedContent = "# This is a test markdown file\n\nThis is the content of the markdown file.";
        assertThat(content).isEqualToIgnoringWhitespace(expectedContent);
    }
	
}
