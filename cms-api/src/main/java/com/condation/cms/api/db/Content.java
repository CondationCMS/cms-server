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

import com.condation.cms.api.db.cms.ReadOnlyFile;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 *
 * @author thmar
 */
public interface Content {
	boolean isVisible (String uri);
	
	List<ContentNode>  listSections(final ReadOnlyFile contentFile);
	
	List<ContentNode> listContent(final ReadOnlyFile base, final String start);
	
	List<ContentNode> listDirectories(final ReadOnlyFile base, final String start);
	
	Optional<ContentNode> byUri (final String uri);
	
	Optional<Map<String,Object>> getMeta(final String uri);
	
	public <T> ContentQuery<T> query(final BiFunction<ContentNode, Integer, T> nodeMapper);

	public <T> ContentQuery<T> query(final String startURI, final BiFunction<ContentNode, Integer, T> nodeMapper);
}
