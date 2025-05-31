package com.condation.cms.modules.ui.services;

/*-
 * #%L
 * ui-module
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author t.marx
 */
public class LockService {

	private final ConcurrentMap<String, List<String>> locks = new ConcurrentHashMap<>();
	
	public boolean isLocked (String mode, String node) {
		if (locks.containsKey(mode)) {
			return locks.get(mode).contains(node);
		}
		return false;
	}
	
	private synchronized void addMode (String mode) {
		if (!locks.containsKey(mode)) {
			locks.put(mode, new ArrayList<>());
		}
	}
	
	public boolean lock (String mode, String node) {
		if (!locks.containsKey(mode)) {
			addMode(mode);
		}
		
		return locks.get(mode).add(node);
	}
	
	public boolean unlock (String mode, String node) {
		if (locks.containsKey(mode)) {
			locks.get(mode).remove(node);
		}
		return true;
	}
}
