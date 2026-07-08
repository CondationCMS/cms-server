package com.condation.cms.filesystem.metadata;

/*-
 * #%L
 * CMS FileSystem
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.NodeVisibility;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.request.RequestContextScope;

/**
 *
 * @author thorstenmarx
 */
public abstract class PageMetaData {
	
	public static boolean isPage (ContentNode node) {
		return !node.isSectionEntry();
	}
	
	public static boolean isVisible (ContentNode node) {
	
		if (node == null || !isPage(node)) {
			return false;
		}
		
		// Manager has access to hidden folder
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)
				&& RequestContextScope.REQUEST_CONTEXT.get().get(IsPreviewFeature.class).mode().equals(com.condation.cms.api.feature.features.IsPreviewFeature.Mode.MANAGER)
				) {
			return true;
		}
		
		if (node.isParentPathHidden()) {
			return false;
		}
		
		return NodeVisibility.isVisible(node) && !node.isHidden();
	}
}
