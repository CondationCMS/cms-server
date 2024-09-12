package com.condation.cms.filesystem;

/*-
 * #%L
 * cms-filesystem
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


import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadyOnlyFileSystem;
import com.condation.cms.api.db.cms.WrappedReadOnlyFileSystem;
import com.condation.cms.api.db.taxonomy.Taxonomies;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.filesystem.taxonomy.FileTaxonomies;
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
	final Configuration configuration;
	
	private FileSystem fileSystem;
	private FileContent content;
	private ReadyOnlyFileSystem readOnlyFileSystem;
	
	private FileTaxonomies taxonomies;
	
	public void init () throws IOException {
		init(MetaData.Type.MEMORY);
	}
	
	public void init (MetaData.Type metaDataType) throws IOException {
		fileSystem = new FileSystem(hostBaseDirectory, eventBus, contentParser);
		fileSystem.init(metaDataType);
		readOnlyFileSystem = new WrappedReadOnlyFileSystem(fileSystem);
		
		content = new FileContent(fileSystem, readOnlyFileSystem);
		
		taxonomies = new FileTaxonomies(configuration, fileSystem);
		
	}

	@Override
	public ReadyOnlyFileSystem getReadOnlyFileSystem() {
		return readOnlyFileSystem;
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
	public Content getContent() {
		return content;
	}

	@Override
	public Taxonomies getTaxonomies() {
		return taxonomies;
	}
	
}
