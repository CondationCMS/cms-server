package com.condation.cms.filesystem.metadata.persistent.field;

/*-
 * #%L
 * CMS FileSystem
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

import org.apache.lucene.document.Document;

public interface IndexFieldHandler<D extends IndexFieldDefinition> {

	String type();

	Class<D> definitionType();

	void add(Document document, String field, Object value, D definition);

	default void index(
			Document document,
			String field,
			Object value,
			IndexFieldDefinition definition) {
		add(document, field, value, definitionType().cast(definition));
	}
}
