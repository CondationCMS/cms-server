package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * pug-module
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.ThemeProperties;
import com.github.thmarx.cms.api.db.DBFileSystem;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.theme.Assets;
import com.github.thmarx.cms.api.theme.Theme;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class PugTemplateEngineNGTest {
	
	static PugTemplateEngine engine;
	
	@BeforeAll
	static void setup () {
		ServerProperties properties = new ServerProperties(Map.of("dev", true));
		var fileSystem = new DBFileSystem() {
			@Override
			public Path resolve(String path) {
				return Path.of("src/test/resources").resolve(path);
			}

			@Override
			public String loadContent(Path file) throws IOException {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}

			@Override
			public List<String> loadLines(Path file) throws IOException {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}

			@Override
			public String loadContent(Path file, Charset charset) throws IOException {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}

			@Override
			public List<String> loadLines(Path file, Charset charset) throws IOException {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
		};
		engine = new PugTemplateEngine(fileSystem, properties, new Theme() {
			@Override
			public String getName() {
				return "empty";
			}

			@Override
			public Path templatesPath() {
				return null;
			}

			@Override
			public Path assetsPath() {
				return null;
			}

			@Override
			public ThemeProperties properties() {
				return null;
			}

			@Override
			public boolean empty() {
				return true;
			}

			@Override
			public Assets getAssets() {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}

			@Override
			public Path extensionsPath() {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
		});
	}

	@Test
	public void testSomeMethod() throws IOException {
		TemplateEngine.Model model = new TemplateEngine.Model(Path.of("pom.xml"), null);
		model.values.put("pageName", "Pug rendered page");
		model.values.put("books", List.of(
				new Book("The Hitchhiker's Guide to the Galaxy", 5.70f, true),
				new Book("Life, the Universe and Everthing", 5.60f, false),
				new Book("The Restaurant at the End of the Universe", 5.40f, true)
		));
		
		var content = engine.render("index.pug", model);
		
		System.out.println(content);
		Assertions.assertThat(content).isNotBlank();
	}
	
	public static record Book (String name, float price, boolean available) {};
	
}
