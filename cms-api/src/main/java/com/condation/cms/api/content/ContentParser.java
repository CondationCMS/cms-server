package com.condation.cms.api.content;

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
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public interface ContentParser {
	
	void clearCache();
	
	Content parse(final ReadOnlyFile contentFile) throws IOException;
	
	Map<String, Object> parseMeta(final ReadOnlyFile contentFile) throws IOException;
	
	record ContentRecord(String content, String meta) implements Serializable {}

	record Content(String content, Map<String, Object> meta) implements Serializable {}
}
