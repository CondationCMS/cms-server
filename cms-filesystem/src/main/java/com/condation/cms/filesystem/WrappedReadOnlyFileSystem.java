package com.condation.cms.filesystem;

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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFileSystem;
import com.condation.cms.api.exceptions.AccessNotAllowedException;
import com.condation.cms.api.utils.PathUtil;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@Deprecated
@RequiredArgsConstructor
public class WrappedReadOnlyFileSystem implements ReadOnlyFileSystem {

	private final DBFileSystem dbFileSytem;

	@Override
	public ReadOnlyFile resolve(String uri) {
		return resolveWithBase(uri, dbFileSytem.hostBase());
	}

	private ReadOnlyFile resolveWithBase(final String uri, final Path basePath) {
		var resolved = dbFileSytem.resolve(uri);

		if (!PathUtil.isChild(dbFileSytem.hostBase(), resolved)) {
			throw new AccessNotAllowedException("not allowed to access nodes outside the host base directory");
		}

		return new NIOReadOnlyFile(resolved, basePath);
	}

	@Override
	public ReadOnlyFile contentBase() {
		return resolveWithBase(Constants.Folders.CONTENT, dbFileSytem.resolve(Constants.Folders.CONTENT));
	}

	@Override
	public ReadOnlyFile assetBase() {
		return resolveWithBase(Constants.Folders.ASSETS, dbFileSytem.resolve(Constants.Folders.ASSETS));
	}
}
