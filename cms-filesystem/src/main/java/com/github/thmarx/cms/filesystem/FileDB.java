package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.api.db.Content;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.db.DBFileSystem;
import com.github.thmarx.cms.api.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class FileDB implements DB {

	private final Path hostBaseDirectory;
	private final EventBus eventBus;
	final Function<Path, Map<String, Object>> contentParser;
	
	private FileSystem fileSystem;
	private FileContent content;
	
	public void init () throws IOException {
		fileSystem = new FileSystem(hostBaseDirectory, eventBus, contentParser);
		fileSystem.init();
		
		content = new FileContent(fileSystem);
	}
	
	@Override
	public DBFileSystem getFileSystem() {
		return fileSystem;
	}

	@Override
	public void close() throws Exception {
		fileSystem.shutdown();
	}

	@Override
	public Content<MetaData.MetaNode> getContent() {
		return content;
	}
	
}
