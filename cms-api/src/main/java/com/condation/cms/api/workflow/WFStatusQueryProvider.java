package com.condation.cms.api.workflow;

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

import com.condation.cms.api.db.ContentQuery;
import java.util.Optional;

/**
 * Optional status-provider capability for efficient, index-backed workflow
 * listings. Implementations must produce the same classification as
 * {@link #isPublished(com.condation.cms.api.db.ContentNode)}.
 */
public interface WFStatusQueryProvider extends WFStatusProvider {

	/**
	 * Adds the index-backed equivalent of {@link #isPublished} to the supplied
	 * query. Providers should override this method when their published state can
	 * be derived entirely from indexed node metadata.
	 *
	 * @param query query to extend
	 * @param <T> result type of the query
	 * @return the extended query, or an empty value when node-based evaluation is
	 * required
	 */
	default <T> Optional<ContentQuery<T>> published(ContentQuery<T> query) {
		return Optional.empty();
	}

	<T> ContentQuery<T> unpublished(ContentQuery<T> query);
}
