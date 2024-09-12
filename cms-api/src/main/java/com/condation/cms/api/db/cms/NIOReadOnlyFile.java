package com.condation.cms.api.db.cms;

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


import com.condation.cms.api.exceptions.AccessNotAllowedException;
import com.condation.cms.api.utils.PathUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class NIOReadOnlyFile implements ReadOnlyFile {

	protected final Path file;
	private final Path basePath;
	
	@Override
	public boolean exists() {
		return Files.exists(file);
	}

	@Override
	public ReadOnlyFile resolve(String uri) {
		var resolved = file.resolve(uri);
		
		if (!PathUtil.isChild(basePath, resolved)) {
			throw new AccessNotAllowedException("not allowed to access nodes outside the host base directory");
		}
		
		return new NIOReadOnlyFile(resolved, basePath);
	}

	@Override
	public String getContent() throws IOException {
		return getContent(StandardCharsets.UTF_8);
	}

	@Override
	public String getContent(Charset charset) throws IOException {
		return Files.readString(file, charset);
	}
	
	@Override
	public List<String> getAllLines() throws IOException {
		return getAllLines(StandardCharsets.UTF_8);
	}

	@Override
	public List<String> getAllLines(Charset charset) throws IOException {
		return Files.readAllLines(file, charset);
	}

	@Override
	public ReadOnlyFile relativize(ReadOnlyFile node) {
		var resolved = file.relativize(((NIOReadOnlyFile)node).file);
		return new NIOReadOnlyFile(resolved, basePath);
	}

	@Override
	public boolean isDirectory() {
		return Files.isDirectory(file);
	}

	@Override
	public long getLastModifiedTime() throws IOException {
		return Files.getLastModifiedTime(file).toMillis();
	}

	@Override
	public String getFileName() {
		return file.getFileName().toString();
	}

	@Override
	public ReadOnlyFile getParent() {
		var resolved = file.getParent();
		
		if (!PathUtil.isChild(basePath, resolved)) {
			throw new AccessNotAllowedException("not allowed to access nodes outside the host base directory");
		}
		
		return new NIOReadOnlyFile(resolved, basePath);
	}

	@Override
	public List<ReadOnlyFile> children() throws IOException {
		return Files.list(file).map(path -> new NIOReadOnlyFile(file, basePath)).map(ReadOnlyFile.class::cast).toList();
	}

	@Override
	public String toString() {
		return file.toString();
	}

	@Override
	public String getContentType() throws IOException {
		return Files.probeContentType(file);
	}

	@Override
	public ReadOnlyFile toAbsolutePath() {
		return new NIOReadOnlyFile(file.toAbsolutePath(), basePath);
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return file.toAbsolutePath().normalize().toString();
	}

	@Override
	public boolean isChild(ReadOnlyFile maybeChild) {
	
		try {
			if (maybeChild == null) {
				return false;
			}
			return maybeChild.getCanonicalPath().startsWith(getCanonicalPath());
		} catch (IOException ex) {
			log.error("", ex);
		}
		
		return false;
	}

	@Override
	public boolean hasParent() {
		var resolved = file.getParent();
		
		return PathUtil.isChild(basePath, resolved);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + Objects.hashCode(this.file);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NIOReadOnlyFile other = (NIOReadOnlyFile) obj;
		return Objects.equals(this.file, other.file);
	}
	
	
	
}
