package com.condation.cms.modules.example;

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


import com.condation.cms.api.extensions.ContentQueryOperatorExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import java.util.function.BiPredicate;

/**
 *
 * @author t.marx
 */
@Extension(ContentQueryOperatorExtensionPoint.class)
public class AnyContentQueryOperator extends ContentQueryOperatorExtensionPoint {

	@Override
	public String getOperator() {
		return "any";
	}

	@Override
	public BiPredicate<Object, Object> getPredicate() {
		return (node_value, value) -> true;
	}
	
}
