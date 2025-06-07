package com.condation.cms.templates.loaders;

/*-
 * #%L
 * cms-templates
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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class ClasspathTemplateLoaderTest {
	

	@Test
	public void testSomeMethod() {
		
		var templateLoader = new ClasspathTemplateLoader("templates");
		
		var templateContent = templateLoader.load("test.html");
	
		Assertions.assertThat(removeXmlComments(templateContent))
				.isNotNull()
				.isEqualTo("Hello Template!");
	}
	
	public static String removeXmlComments(String xml) {
        return xml.replaceAll("(?s)<!--.*?-->", "").trim();
    }
	
}
