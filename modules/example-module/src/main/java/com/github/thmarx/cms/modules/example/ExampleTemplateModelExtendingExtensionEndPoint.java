/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.modules.example;

/*-
 * #%L
 * example-module
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

import com.github.thmarx.cms.api.extensions.TemplateModelExtendingExtentionPoint;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.modules.api.annotation.Extension;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author thmar
 */
@Extension(TemplateModelExtendingExtentionPoint.class)
public class ExampleTemplateModelExtendingExtensionEndPoint extends TemplateModelExtendingExtentionPoint {

	@Override
	public void extendModel(TemplateEngine.Model model) {
		model.values.put("searcher", new Searcher());
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
