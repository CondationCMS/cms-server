package com.github.thmarx.cms.api.db;

/**
 *
 * @author thmar
 */
public interface DB<CN> extends AutoCloseable{
	
	public DBFileSystem getFileSystem();
	
	public Content<CN> getContent();
}
