package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * cms-templates
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

import com.condation.cms.templates.functions.TemplateFunction;
import java.util.Date;

/**
 *
 * @author thorstenmarx
 */
public class DateFunction implements TemplateFunction {

	public static final String NAME = "date";
	
	@Override
	public Object invoke(Object... params) {
		return new Date();
	}

	@Override
	public String name() {
		return NAME;
	}
	
}
