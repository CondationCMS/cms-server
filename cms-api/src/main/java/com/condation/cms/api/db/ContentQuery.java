package com.condation.cms.api.db;

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


import java.util.List;
import java.util.Map;

/**
 *
 * @author t.marx
 * @param <T>
 */
public interface ContentQuery<T> {

	ContentQuery<T> excerpt(final long excerptLength);

	Page<T> page(final long page, final long size);
	
	List<T> get();
	
	Map<Object, List<ContentNode>> groupby(final String field);

	Sort<T> orderby(final String field);

	ContentQuery<T> json();
	
	ContentQuery<T> html();
	
	ContentQuery<T> contentType(String contentType);
	
	ContentQuery<T> where(final String field, final Object value);

	ContentQuery<T> where(final String field, final String operator, final Object value);

	ContentQuery<T> whereContains(final String field, final Object value);

	ContentQuery<T> whereNotContains(final String field, final Object value);

	ContentQuery<T> whereIn(final String field, final Object... value);

	ContentQuery<T> whereIn(final String field, final List<Object> value);

	ContentQuery<T> whereNotIn(final String field, final Object... value);

	ContentQuery<T> whereNotIn(final String field, final List<Object> value);
	
	ContentQuery<T> whereExists(final String field);
	
	ContentQuery<T> expression(final String expressions);

	public static interface Sort<T> {
		public ContentQuery<T> asc();

		public ContentQuery<T> desc();
	}
}
