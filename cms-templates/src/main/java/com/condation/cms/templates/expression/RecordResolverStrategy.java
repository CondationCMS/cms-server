package com.condation.cms.templates.expression;

/*-
 * #%L
 * cms-templates
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

import java.util.List;
import org.apache.commons.jexl3.JexlOperator;
import org.apache.commons.jexl3.introspection.JexlUberspect;

/**
 *
 * @author t.marx
 */
public class RecordResolverStrategy implements JexlUberspect.ResolverStrategy{

	@Override
		public List<JexlUberspect.PropertyResolver> apply(final JexlOperator operator, final Object object) {
			if(object instanceof Record) {
				return List.of(new RecordPropertyResolver());
			}
			return JexlUberspect.JEXL_STRATEGY.apply(operator, object);
		}
}
