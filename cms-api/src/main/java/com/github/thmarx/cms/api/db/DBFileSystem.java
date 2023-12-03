package com.github.thmarx.cms.api.db;

import java.nio.file.Path;

/**
 *
 * @author thmar
 */
public interface DBFileSystem {
	Path resolve(String path);
}
