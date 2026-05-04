package com.condation.cms.templates;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.templates.parser.ComponentNode;
import com.condation.cms.templates.renderer.Renderer;
import java.io.Writer;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author t.marx
 */
public interface Component {

	String getName();
	
	default Set<String> getCloseingNames () {
		return Collections.emptySet();
	}
    
    default Optional<String> alternateTagName () {
        return Optional.empty();
    }
	
	default boolean isClosing () {
		return false;
	}
	
	default void render (ComponentNode node, Renderer.Context context, Writer writer) {
		// default render does nothing
	};
}
