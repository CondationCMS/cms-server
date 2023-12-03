package com.github.thmarx.cms.api.db;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author thmar
 */
public interface Content<CN> {
	boolean isVisible (String uri);
	
	List<CN>  listSections(Path contentFile);
	
	List<CN> listContent(final Path base, final String start);
	
	List<CN> listDirectories(final Path base, final String start);
	
	Optional<CN> byUri (final String uri);
}
