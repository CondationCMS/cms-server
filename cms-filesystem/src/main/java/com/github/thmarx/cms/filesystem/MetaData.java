package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.filesystem.index.IndexProviding;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public interface MetaData extends IndexProviding {
	
	public enum Type {
		MEMORY, PERSISTENT
	}
	
	void open () throws IOException;
	void close () throws IOException;

	void addFile(final String uri, final Map<String, Object> data, final LocalDate lastModified);

	Optional<ContentNode> byUri(final String uri);

	void createDirectory(final String uri);

	Optional<ContentNode> findFolder(String uri);

	List<ContentNode> listChildren(String uri);
	
	void clear ();
	
	Map<String, ContentNode> nodes();

	Map<String, ContentNode> tree();
}
