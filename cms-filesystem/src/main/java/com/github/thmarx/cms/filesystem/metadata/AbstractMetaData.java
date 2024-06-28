package com.github.thmarx.cms.filesystem.metadata;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.api.db.ContentNode;

/**
 *
 * @author t.marx
 */
public class AbstractMetaData {
	public static boolean isVisible (ContentNode node) {
		return node != null 
				// check if some parent is hidden
				&& !node.uri().startsWith(".") && !node.uri().contains("/.")
				&& node.isPublished() 
				&& !node.isHidden() 
				&& !node.isSection();
	}
}
