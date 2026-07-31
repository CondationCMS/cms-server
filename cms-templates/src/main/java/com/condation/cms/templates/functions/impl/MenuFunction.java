package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * CMS Templates
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.templates.functions.TemplateFunction;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads a site menu by id for use in templates.
 */
@Slf4j
@RequiredArgsConstructor
public class MenuFunction implements TemplateFunction {

	public static final String NAME = "menu";

	private final RequestContext requestContext;

	@Override
	public Object invoke(Object... params) {
		if (params == null || params.length == 0 || !(params[0] instanceof String menuId)) {
			return null;
		}

		try {
			MenuService menuService = requestContext.get(InjectorFeature.class)
					.injector()
					.getInstance(MenuService.class);
			return menuService.get(menuId).orElse(null);
		} catch (IOException | IllegalArgumentException exception) {
			log.error("Could not load menu '{}'", menuId, exception);
			return null;
		}
	}

	@Override
	public String name() {
		return NAME;
	}
}
