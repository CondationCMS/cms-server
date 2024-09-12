package com.condation.cms.api.query;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.annotations.Experimental;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 *
 * @author t.marx
 */
@Experimental
public class QueryGateway {
	
	public Map<Class<? extends Query>, QueryHandler> handlers = new HashMap<>();
	
	public  <T, Q extends Query<T>> void register (Class<Q> query, QueryHandler<Q, T> handler) {
		handlers.put(query, handler);
	}
	
	public void init () {
		ServiceLoader<QueryProvider> loader = ServiceLoader.load(QueryProvider.class);
		loader.forEach(provider -> register(provider.queryClass(), provider.handler()));
	}
	
	public <T> T execute (Query<T> query) {
		if (!handlers.containsKey(query.getClass())) {
			return null;
		}
		
		return (T) handlers.get(query.getClass()).handle(query);
	}
}
