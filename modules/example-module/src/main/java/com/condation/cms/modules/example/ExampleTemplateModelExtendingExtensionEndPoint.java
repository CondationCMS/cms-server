package com.condation.cms.modules.example;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.condation.cms.api.extensions.TemplateModelExtendingExtensionPoint;

/*-
 * #%L
 * example-module
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

 
import com.condation.cms.api.template.TemplateEngine;
import com.condation.modules.api.annotation.Extension;

/**
 *
 * @author thmar
 */
@Extension(TemplateModelExtendingExtensionPoint.class)
public class ExampleTemplateModelExtendingExtensionEndPoint extends TemplateModelExtendingExtensionPoint {

	@Override
	public Map<String, Object> getModel() {
		return Map.of("searcher", new Searcher());
	}

	@Override
	public void init() {
	}
	
	public static class Searcher {
		public Result search (String query) {
			Result result = new Result(List.of(
					new Item(UUID.randomUUID().toString()),
					new Item(UUID.randomUUID().toString()),
					new Item(UUID.randomUUID().toString()),
					new Item(UUID.randomUUID().toString())
			), 100);
			
			return result;
		}
	}
	
	public static record Result (List<Item> items, int total){};
	
	public static record Item (String name){};
}
