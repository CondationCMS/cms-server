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

import org.apache.commons.jexl3.introspection.JexlPropertyGet;
import org.apache.commons.jexl3.introspection.JexlUberspect;

import java.lang.reflect.Method;
import java.util.Optional;
import org.apache.commons.jexl3.introspection.JexlPropertySet;

public class RecordPropertyResolver implements JexlUberspect.PropertyResolver {

    private Optional<Method> findRecordAccessor(Object obj, String name) {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 0) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

	@Override
	public JexlPropertyGet getPropertyGet(JexlUberspect uber, Object obj, Object identifier) {
		if (obj instanceof Record && identifier instanceof String) {
            String propertyName = (String) identifier;

            // Suche nach der passenden Methode
            Optional<Method> accessor = findRecordAccessor(obj, propertyName);
            if (accessor.isPresent()) {
                return new RecordPropertyGet(accessor.get());
            }
        }
        return null;
	}

	@Override
	public JexlPropertySet getPropertySet(JexlUberspect uber, Object obj, Object identifier, Object arg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

    private static class RecordPropertyGet implements JexlPropertyGet {
        private final Method method;

        public RecordPropertyGet(Method method) {
            this.method = method;
        }

        @Override
        public Object invoke(Object obj) throws Exception {
            return method.invoke(obj);
        }

        @Override
        public boolean tryFailed(Object result) {
            return result == null;
        }

        @Override
        public Object tryInvoke(Object obj, Object key) {
            return null;
        }

		@Override
		public boolean isCacheable() {
			return false;
		}
    }
}
