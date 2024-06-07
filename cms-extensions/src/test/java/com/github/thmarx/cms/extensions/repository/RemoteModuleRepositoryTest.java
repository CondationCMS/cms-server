package com.github.thmarx.cms.extensions.repository;

/*-
 * #%L
 * cms-extensions
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class RemoteModuleRepositoryTest {
	

	RemoteModuleRepository<ModuleInfo> moduleRepository = new RemoteModuleRepository<>(
			ModuleInfo.class,
			"https://raw.githubusercontent.com/thmarx/module-registry"
			);

	@Test
	public void test_exist() {
		Assertions.assertThat(moduleRepository.exists("none-module")).isFalse();
		
		Assertions.assertThat(moduleRepository.exists("downloads-module")).isTrue();
	}
	
	@Test
	public void test_info() {
		Assertions.assertThat(moduleRepository.getInfo("none-module")).isEmpty();
		
		Assertions.assertThat(moduleRepository.getInfo("downloads-module"))
				.isPresent()
				.get()
				.isInstanceOf(ModuleInfo.class)
				;
	}
	
	@Test
	public void test_download() throws IOException {
		var moduleInfo = moduleRepository.getInfo("downloads-module").get();
		
		var modulesPath = Path.of("target/modules-" + System.currentTimeMillis());
		Files.createDirectories(modulesPath);
		
		moduleRepository.download(moduleInfo.getFile(), modulesPath);
	}
}
