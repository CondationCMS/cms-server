package com.condation.cms.content;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author t.marx
 */
public class DefaultContentParserTest {
	
	ContentParser sut;
	
	@BeforeEach
	private void setup () {
		sut = new DefaultContentParser();
	}	
	
	@Test
	public void testSomeMethod() throws Exception {
		
		List<String> lines = new ArrayList<>();
		lines.add("---");
		lines.add("title: The title");
		lines.add("---");
		lines.add("Here is the Content");
		lines.add("---");
		lines.add("With an hr");
		
		var expectedMeta = Map.of("title", "The title");
		var expectedContent = "Here is the Content\r\n";
		expectedContent += "---\r\n";
		expectedContent += "With an hr\r\n";
		
		var readOnlyFile = Mockito.mock(ReadOnlyFile.class);
		Mockito.when(readOnlyFile.getAllLines()).thenReturn(lines);
		
		var content = sut.parse(readOnlyFile);
		
		Assertions.assertThat(content.meta())
				.isEqualTo(expectedMeta);
		
		Assertions.assertThat(content.content())
				.isEqualTo(expectedContent);
		
	}
	
}
