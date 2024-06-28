package com.github.thmarx.cms.api.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 *
 * @author thmar
 */
public class FileUtils {

	public static void deleteFolder(Path pathToBeDeleted) throws IOException {
		Files.walk(pathToBeDeleted)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}
}