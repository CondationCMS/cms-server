package com.condation.cms.core.injector;

import com.condation.cms.api.injector.Module;
import com.condation.cms.api.injector.Injector;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

/**
 *
 * @author thorstenmarx
 */
public class Example {

	public static void main(String[] args) {
		Injector injector = DefaultInjector.create(new MyModule());
		
		var testService = injector.getInstance(TestService.class);
		
		var testService2 = injector.getInstance(TestService.class);
		
		System.out.println(testService == testService2);
		
		var testService3 = injector.getInstance("server", TestService.class);
		
		System.out.println(testService == testService3);
	}
	
	public static class MyModule implements Module {

		@Override
		public void register(Injector injector) {
			injector.register(TestService.class, (injector2) -> new TestServiceImpl()).singleton();
			
			injector.register("server", TestService.class, (injector2) -> new TestServiceImpl());
		}
		
	}
	
	public static interface TestService {
		
	}
	
	public static class TestServiceImpl implements TestService {
		
	}
}
