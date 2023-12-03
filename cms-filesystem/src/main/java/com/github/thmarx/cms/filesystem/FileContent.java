package com.github.thmarx.cms.filesystem;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.db.Content;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.ContentQuery;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class FileContent implements Content {

	private final FileSystem fileSystem;
	
	@Override
	public boolean isVisible(String uri) {
		return fileSystem.isVisible(uri);
	}

	@Override
	public List<ContentNode> listSections(Path contentFile) {
		return fileSystem.listSections(contentFile);
	}

	@Override
	public List<ContentNode> listContent(Path base, String start) {
		return fileSystem.listContent(base, start);
	}

	@Override
	public List<ContentNode> listDirectories(Path base, String start) {
		return fileSystem.listDirectories(base, start);
	}

	@Override
	public Optional<ContentNode> byUri(String uri) {
		return fileSystem.getMetaData().byUri(uri);
	}

	@Override
	public <T> ContentQuery<T> query(BiFunction<ContentNode, Integer, T> nodeMapper) {
		return fileSystem.query(nodeMapper);
	}

	@Override
	public <T> ContentQuery<T> query(String startURI, BiFunction<ContentNode, Integer, T> nodeMapper) {
		return fileSystem.query(startURI, nodeMapper);
	}

	@Override
	public Optional<Map<String, Object>> getMeta(String uri) {
		return fileSystem.getMeta(uri);
	}
	
}
