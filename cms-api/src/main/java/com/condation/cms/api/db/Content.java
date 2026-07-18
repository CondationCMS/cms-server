package com.condation.cms.api.db;

/*-
 * #%L
 * CMS Api
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.cms.ReadOnlyFile;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author thmar
 */
public interface Content {
	boolean isVisible (String uri);
	
	boolean isVisible (ContentNode node);
	
	List<ContentNode>  listSectionEntries(final ReadOnlyFile contentFile);
	
	List<ContentNode> listContent(final ReadOnlyFile base, final String start);
	
	List<ContentNode> listDirectories(final ReadOnlyFile base, final String start);
	
	@Deprecated(since = "8.3.0")
	Optional<ContentNode> byUri (final String uri);

	Optional<ContentNode> byPath (final String path);

	Optional<ContentNode> byUrl (final String url);

	Optional<Map<String,Object>> getMeta(final String path);
	
	public <T> ContentQuery<T> query(final BiFunction<ContentNode, Integer, T> nodeMapper);

	public <T> ContentQuery<T> query(final String startURI, final BiFunction<ContentNode, Integer, T> nodeMapper);
	
	public List<ContentNode> searchByTitle (@NonNull String input);
}
