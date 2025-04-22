/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.condation.cms.api.extensions.http.routes;

/*-
 * #%L
 * cms-api
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

import com.condation.cms.api.annotations.Route;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class RoutesManagerTest {
	
	@Test
	public void testRegister() {
		Object controller = new MyRoutes();
		RoutesManager instance = new RoutesManager();
		instance.register(controller);
		
		var handler1 = instance.findFirst("/test1", "GET");
		Assertions.assertThat(handler1).isPresent();
		var handler2 = instance.findFirst("/test2", "GET");
		Assertions.assertThat(handler2).isPresent();
	}
	
	@Test
	public void test_no_handler() {
		Object controller = new MyRoutes();
		RoutesManager instance = new RoutesManager();
		instance.register(controller);
		
		var handler = instance.findFirst("/test3", "GET");
		Assertions.assertThat(handler).isEmpty();
	}

	public class MyRoutes {
		
		@Route("/test1")
		public boolean handle1 (Request request, Response response, Callback callback) {
			return true;
		}
		
		@Route("/test2")
		public boolean handle2 (Request request, Response response, Callback callback) {
			return true;
		}
	}

	
}
