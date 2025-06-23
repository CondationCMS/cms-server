package com.condation.cms.modules.system.api.helpers;

/*-
 * #%L
 * api-module
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

import java.util.*;

/**
 *
 * @author thorstenmarx
 */
public class WhitelistFilter {

	public static Map<String, Object> applyWhitelist(Map<String, Object> input, Set<String> whitelistPaths) {
		Map<String, Object> result = new LinkedHashMap<>();
		Map<String, Set<String>> tree = buildWhitelistTree(whitelistPaths);
		applyRecursive(input, result, tree);
		return result;
	}

	private static void applyRecursive(Map<String, Object> input, Map<String, Object> result, Map<String, Set<String>> whitelistTree) {
		for (Map.Entry<String, Object> entry : input.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			// match explicit key or wildcard "*"
			Set<String> subpaths = Optional.ofNullable(whitelistTree.get(key))
					.orElse(whitelistTree.get("*"));

			if (subpaths == null) {
				continue;
			}

			if (subpaths.isEmpty()) {
				result.put(key, value);
			} else if (value instanceof Map) {
				Map<String, Object> nestedInput = (Map<String, Object>) value;
				Map<String, Object> nestedResult = new LinkedHashMap<>();
				Map<String, Set<String>> nestedTree = buildWhitelistTree(subpaths);
				applyRecursive(nestedInput, nestedResult, nestedTree);
				if (!nestedResult.isEmpty()) {
					result.put(key, nestedResult);
				}
			} else if (value instanceof List) {
				List<Object> filteredList = new ArrayList<>();
				for (Object item : (List<?>) value) {
					if (item instanceof Map) {
						Map<String, Object> filteredItem = new LinkedHashMap<>();
						Map<String, Set<String>> nestedTree = buildWhitelistTree(subpaths);
						applyRecursive((Map<String, Object>) item, filteredItem, nestedTree);
						if (!filteredItem.isEmpty()) {
							filteredList.add(filteredItem);
						}
					} else if (subpaths.contains("*")) {
						filteredList.add(item); // primitive List mit Wildcard erlauben
					}
				}
				if (!filteredList.isEmpty()) {
					result.put(key, filteredList);
				}
			} else if (subpaths.contains("*")) {
				result.put(key, value); // primitive mit Wildcard
			}
		}
	}

	private static Map<String, Set<String>> buildWhitelistTree(Collection<String> paths) {
		Map<String, Set<String>> tree = new LinkedHashMap<>();
		for (String path : paths) {
			String[] parts = path.split("\\.", 2);
			String root = parts[0];
			String child = (parts.length > 1) ? parts[1] : null;

			tree.computeIfAbsent(root, k -> new LinkedHashSet<>());
			if (child != null) {
				tree.get(root).add(child);
			}
		}
		return tree;
	}
}
