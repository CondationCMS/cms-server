package com.condation.cms.filesystem.metadata.query;

/*-
 * #%L
 * cms-filesystem
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

import com.github.thmarx.cms.api.db.ContentQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import lombok.Data;
import lombok.Getter;



/**
 *
 * @author t.marx
 */
public abstract class ExtendableQuery<T> implements ContentQuery<T> {
	
	@Getter
	private final Context context = new Context();

	public ContentQuery<T> addCustomOperators (String operator, BiPredicate<Object, Object> queryOperations) {
		context.queryOperations.put(operator, queryOperations);
		return this;
	}
	
	public ContentQuery<T> addAllCustomOperators (Map<String, BiPredicate<Object, Object>> queryOperations) {
		context.queryOperations.putAll(queryOperations);
		return this;
	}
	
	@Data
	public static class Context {
		private final Map<String, BiPredicate<Object, Object>> queryOperations = new HashMap<>();
	}
}
