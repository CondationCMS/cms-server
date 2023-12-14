package com.github.thmarx.cms.filesystem.functions.navigation;

import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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

/**
 *
 * @author t.marx
 */
public record NavNode (String name, String path, int depth, boolean current, List<NavNode> children) {
	public NavNode (String name, String path, int depth) {
		this(name, path, depth, false, Collections.emptyList());
	}
	public NavNode (String name, String path, boolean current) {
		this(name, path, 1, current, Collections.emptyList());
	}
	public NavNode (String name, String path, boolean current, List<NavNode> children) {
		this(name, path, 1, current, children);
	}
}
