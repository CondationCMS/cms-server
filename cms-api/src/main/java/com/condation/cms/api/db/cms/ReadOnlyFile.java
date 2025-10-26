package com.condation.cms.api.db.cms;

/*-
 * #%L
 * cms-api
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


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 * @author t.marx
 */
public interface ReadOnlyFile {
	boolean exists ();
	
	String uri ();
	
	String relativePath();
	
	ReadOnlyFile resolve (String uri);
	
	String getContent () throws IOException;
	
	String getContent (Charset charset) throws IOException;
	
	List<String> getAllLines () throws IOException;
	
	List<String> getAllLines (Charset charset) throws IOException;
	
	ReadOnlyFile relativize (ReadOnlyFile node);
	
	boolean isDirectory();
	
	long getLastModifiedTime () throws IOException;
	
	String getFileName ();
	
	ReadOnlyFile getParent ();
	
	boolean hasParent ();
	
	List<ReadOnlyFile> children() throws IOException;
	
	String getContentType () throws IOException;
	
	ReadOnlyFile toAbsolutePath();
	
	String getCanonicalPath () throws IOException;
	
	boolean isChild (ReadOnlyFile maybeChild);
}
