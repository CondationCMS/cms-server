package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.utils.FileUtils;
import com.github.slugify.Slugify;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class UIPathUtil {

	public static Slugify SLUGIFY = Slugify.builder()
			.customReplacement("ä", "ae")
			.customReplacement("Ä", "ae")
			.customReplacement("ü", "ue")
			.customReplacement("Ü", "ue")
			.customReplacement("ö", "oe")
			.customReplacement("Ö", "oe")
			.customReplacement("ß", "ss")
			.lowerCase(true).build();
	
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

	public static boolean hasChildren(Path path) {
		try {
			if (!Files.isDirectory(path)) {
				return false;
			}
			return FileUtils.countChildren(path) > 0;
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return false;
	}

	public static String getType(Path path) {
		if (Files.isDirectory(path)) {
			return "folder";
		} else {
			return "file";
		}
	}

	public static String slugify (String input) {
		return SLUGIFY.slugify(input);
	}
	
	public static String toValidFilename(String input) {
		
		input = input.toLowerCase();
		String extension = "";
		if (input.endsWith(".md")) {
			extension = input.substring(input.lastIndexOf("."));
			input = input.substring(0, input.lastIndexOf("."));
		}
		
		var slugified = slugify(input);
		
		return slugified + extension;
	}
}
