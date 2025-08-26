package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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

import com.condation.cms.api.utils.PathUtil;
import java.io.IOException;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class PathUtilTest {
	
	@Test
	public void test_canonical () throws IOException {
		String file = "pom.xml";
		
		System.out.println("canonical: " + Path.of(file).toFile().getCanonicalPath());
		System.out.println("alternative: " + Path.of(file).toAbsolutePath().normalize().toString());
	}

	@Test
	public void test_to_url() {
		
		Path contentBase = Path.of("src/");
		
		String toURI = PathUtil.toURL(contentBase.resolve("index.md"), contentBase);
		assertThat(toURI).isEqualTo("/");
		
		toURI = PathUtil.toURL(contentBase.resolve("modules/index.md"), contentBase);
		assertThat(toURI).isEqualTo("/modules");
		
		toURI = PathUtil.toURL(contentBase.resolve("modules/test.md"), contentBase);
		assertThat(toURI).isEqualTo("/modules/test");
		
		toURI = PathUtil.toURL(contentBase.resolve("test.md"), contentBase);
		assertThat(toURI).isEqualTo("/test");
		
		toURI = PathUtil.toURL(contentBase.resolve(""), contentBase);
		assertThat(toURI).isEqualTo("/");
	}
	
}
