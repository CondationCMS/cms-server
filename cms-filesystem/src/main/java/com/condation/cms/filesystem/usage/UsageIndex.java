package com.condation.cms.filesystem.usage;

/*-
 * #%L
 * cms-filesystem
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

import java.io.IOException;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author marx
 */
public interface UsageIndex extends AutoCloseable {

	/**
	 * removes all documents
	 */
	public void clearAll ();
	
	/**
	 * adds a new usage.
	 *
	 * @param reference
	 * @throws IOException
	 */
	void addUsage(final Reference reference) throws IOException;

	/**
	 * Incoming references:
	 * returns References who use the Target.
	 *
	 * @param target
	 * @param targetType
	 * @return
	 * @throws IOException
	 */
	List<Reference> getUsedBy(final String target, final String targetType) throws IOException;

	/**
	 * Incoming references:
	 * returns References who use the Target.
	 *
	 * @param target
	 * @param targetType
	 * @param referenceType
	 * @return
	 * @throws IOException
	 */
	List<Reference> getUsedBy(final String target, final String targetType, final String referenceType) throws IOException;
	
	/**
	 * Outgoiing references:
	 * Returns the used references.
	 *
	 * @param source
	 * @param sourceType
	 * @return
	 * @throws IOException
	 */
	List<Reference> getUses(final String source, final String sourceType) throws IOException;
	
	/**
	 * Outgoiing references:
	 * Returns the used references.
	 *
	 * @param source
	 * @param sourceType
	 * @param referenceType
	 * @return
	 * @throws IOException
	 */
	List<Reference> getUses(final String source, final String sourceType, final String referenceType) throws IOException;
	
	public boolean isUsing(final String source, final String sourceType, final String referenceType, final String target, final String targetType) throws IOException;
	
	/**
	 * Call this after a instance is deleted, source and targets for this
	 * instance will be deleted.
	 *
	 * @param id
	 * @param type
	 * @throws IOException
	 */
	void clearUsage(final String id, final String type) throws IOException;
	
	/**
	 * Removes all targets from a source of a reference type
	 * 
	 * @param source
	 * @param sourceType
	 * @param referenceType
	 * @throws IOException 
	 */
	void removeTargets (final String source, final String sourceType, final String referenceType) throws IOException;

	@RequiredArgsConstructor
	@EqualsAndHashCode
	public static class Reference {
		public final String source;
		public final String sourceType;
		public final String target;
		public final String targetType;
		public final String referenceType;
	}
}
