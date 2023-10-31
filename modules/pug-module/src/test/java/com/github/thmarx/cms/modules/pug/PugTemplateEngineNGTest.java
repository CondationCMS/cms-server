package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * pug-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.template.TemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class PugTemplateEngineNGTest {
	
	PugTemplateEngine engine;
	
	@BeforeClass
	void setup () {
		ServerProperties properties = new ServerProperties(Map.of("dev", true));
		var fileSystem = new ModuleFileSystem() {
			@Override
			public Path resolve(String path) {
				return Path.of("src/test/resources").resolve(path);
			}
		};
		engine = new PugTemplateEngine(fileSystem, properties);
	}

	@Test
	public void testSomeMethod() throws IOException {
		TemplateEngine.Model model = new TemplateEngine.Model(Path.of("pom.xml"));
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
