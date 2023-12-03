package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.api.db.Content;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class FileContent implements Content<MetaData.MetaNode> {

	private final FileSystem fileSystem;
	
	@Override
	public boolean isVisible(String uri) {
		return fileSystem.isVisible(uri);
	}

	@Override
	public List<MetaData.MetaNode> listSections(Path contentFile) {
		return fileSystem.listSections(contentFile);
	}

	@Override
	public List<MetaData.MetaNode> listContent(Path base, String start) {
		return fileSystem.listContent(base, start);
	}

	@Override
	public List<MetaData.MetaNode> listDirectories(Path base, String start) {
		return fileSystem.listDirectories(base, start);
	}

	@Override
	public Optional<MetaData.MetaNode> byUri(String uri) {
		return fileSystem.getMetaData().byUri(uri);
	}
	
	
	
}
