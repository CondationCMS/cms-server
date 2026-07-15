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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author thmar
 */
public interface DBFileSystem {

	Path hostBase();

	Path resolve(String path);

	String loadContent(final Path file) throws IOException;

	List<String> loadLines(final Path file) throws IOException;

	String loadContent(final Path file, final Charset charset) throws IOException;

	List<String> loadLines(final Path file, final Charset charset) throws IOException;

	ReadOnlyFile contentBase();

	ReadOnlyFile assetBase();
}
