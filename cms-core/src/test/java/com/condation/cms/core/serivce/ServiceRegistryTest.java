package com.condation.cms.core.serivce;

/*-
 * #%L
 * cms-core
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author thorstenmarx
 */
public class ServiceRegistryTest {

	@BeforeAll
	static void registerServices() {
		ServiceRegistry.getInstance().register("site-1", MyService.class, new MyService("site-1"));
		ServiceRegistry.getInstance().register("site-2", MyService.class, new MyService("site-2"));
	}
	
	@AfterAll
	static void clear () {
		ServiceRegistry.getInstance().clear();
	}

	@Test
	public void testHasService() {
		Assertions.assertThat(ServiceRegistry.getInstance().has("site-1", MyService.class)).isTrue();
		
		Assertions.assertThat(ServiceRegistry.getInstance().has("site-3", MyService.class)).isFalse();
	}
	
	@Test
	public void testGetService() {
		var service1 = ServiceRegistry.getInstance().get("site-1", MyService.class);
		
		Assertions.assertThat(service1).isPresent();
		Assertions.assertThat(service1.get().getName()).isEqualTo("site-1");
		
		var service2 = ServiceRegistry.getInstance().get("site-2", MyService.class);
		
		Assertions.assertThat(service2).isPresent();
		Assertions.assertThat(service2.get().getName()).isEqualTo("site-2");
	}

	private static class MyService implements Service {

		private String name;

		public MyService(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
