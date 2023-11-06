package com.github.thmarx.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class PathUtil {

	public static String toUri(final Path contentFile, final Path contentBase) {
		Path relativize = contentBase.relativize(contentFile);
//		if (Files.isDirectory(contentFile)) {
//			relativize = relativize.resolve("index.md");
//		}
		var uri = relativize.toString();
		uri = uri.replaceAll("\\\\", "/");
		return uri;
	}
	
	public static boolean isChild(Path possibleParent, Path maybeChild) throws IOException {
		return maybeChild.toFile().getCanonicalPath().startsWith(possibleParent.toFile().getCanonicalPath());
	}
	
	public static boolean hasChildren (Path path) {
		try {
			if (!Files.isDirectory(path)) {
				return false;
			}
			return Files.list(path).count() > 0;
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return false;
	}
	
	public static String getType (Path path) {
		if (Files.isDirectory(path)) {
			return "folder";
		} else {
			return "file";
		}
	}
}
