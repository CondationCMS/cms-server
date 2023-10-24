package com.github.thmarx.cms.server.jetty;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.github.thmarx.cms.utils.PathUtil;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;

/**
 *
 * @author t.marx
 */
@Slf4j
public class FileFolderPathResource extends Resource {

	private final Path path;

	public FileFolderPathResource(Path path) {
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public boolean isContainedIn(Resource r) {
		try {
			return PathUtil.isChild(path, r.getPath());
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return false;
	}

	@Override
	public boolean isDirectory() {
		return Files.isDirectory(path);
	}

	@Override
	public boolean isReadable() {
		return Files.isReadable(path);
	}

	@Override
	public URI getURI() {
		return path.toUri();
	}

	@Override
	public String getName() {
		return path.toAbsolutePath().toString();
	}

	@Override
	public String getFileName() {
		Path fn = path.getFileName();
		if (fn == null) {
			return "";
		}
		return fn.toString();
	}

	@Override
	public Resource resolve(String subUriPath) {
		// Check that the path is within the root,
		// but use the original path to create the
		// resource, to preserve aliasing.
		if (URIUtil.isNotNormalWithinSelf(subUriPath)) {
			throw new IllegalArgumentException(subUriPath);
		}

		if ("/".equals(subUriPath)) {
			return this;
		}

		var parentName = path.getFileName().toString();
		if (subUriPath.startsWith("/" + parentName)) {
			subUriPath = subUriPath.substring(parentName.length() + 1);
		}

		URI uri = getURI();
		URI resolvedUri = URIUtil.addPath(uri, subUriPath);
		Path path = Paths.get(resolvedUri);
		if (Files.exists(path)) {
			return newResource(path);
		}

		return null;
	}

	protected Resource newResource(Path path) {
		return new FileFolderPathResource(path);
	}

	@Override
	public Instant lastModified() {
		Path path = getPath();
		if (path == null) {
			return Instant.EPOCH;
		}

		if (!Files.exists(path)) {
			return Instant.EPOCH;
		}

		try {
			FileTime ft = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
			return ft.toInstant();
		} catch (IOException e) {
			log.trace("IGNORED", e);
			return Instant.EPOCH;
		}
	}

	@Override
	public long length() {
		try {
			return Files.size(getPath());
		} catch (IOException e) {
			// in case of error, use Files.size() logic of 0L
			return 0L;
		}
	}
}
