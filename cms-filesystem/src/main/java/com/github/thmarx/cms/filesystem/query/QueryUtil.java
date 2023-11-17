package com.github.thmarx.cms.filesystem.query;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.filesystem.MetaData;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
final class QueryUtil {
	
	public static enum Operator {
		EQUALS,
		CONTAINS,
		CONTAINS_NOT,
		NOT_EQUALS;
	}

	protected static List<MetaData.MetaNode> sorted(final Collection<MetaData.MetaNode> nodes, final String field, final boolean asc) {
		
		var tempNodes = nodes.stream().sorted(
				(node1, node2) -> {
					var value1 = getValue(node1.data(), field);
					var value2 = getValue(node2.data(), field);
					
					return compare(value1, value2);
				}
		).toList();
		
		if (!asc) {
			tempNodes = tempNodes.reversed();
		}
		
		return tempNodes;
	}
	
	private static int compare (Object o1, Object o2) {
		if (Objects.equals(o1, o2)) {
			return 0;
		}
		if (o1 == null) {
			return -1;
		}
		if (o2 == null ) {
			return 1;
		}
		
		if (!o1.getClass().equals(o2.getClass())) {
			return 0;
		}
				
		if (o1 instanceof Float) {
			return Float.compare((float)o1, (float)o2);
		} else if (o1 instanceof Double) {
			return Double.compare((double)o1, (double)o2);
		} else if (o1 instanceof Short) {
			return Short.compare((short)o1, (short)o2);
		} else if (o1 instanceof Integer) {
			return Integer.compare((int)o1, (int)o2);
		} else if (o1 instanceof Long) {
			return Long.compare((long)o1, (long)o2);
		} else if (o1 instanceof String string) {
			return string.compareTo((String)o2);
		} else if (o1 instanceof Date date) {
			return date.compareTo((Date)o2);
		}
		
		return 0;
	}

	protected static Collection<MetaData.MetaNode> filtered(final Collection<MetaData.MetaNode> nodes, final String field, final Object value, final Operator operator) {
		return nodes.stream().filter(createPredicate(field, value, operator)).toList();
	}

	private static Predicate<? super MetaData.MetaNode> createPredicate(final String field, final Object value, final Operator operator) {
		return (node) -> {
			var node_value = getValue(node.data(), field);
			
			if (node_value == null) {
				return false;
			}
			
			if (Operator.EQUALS.equals(operator)) {
				return Objects.equals(node_value, value);
			} else if (Operator.NOT_EQUALS.equals(operator)) {
				return !Objects.equals(node_value, value);
			} else if (Operator.CONTAINS.equals(operator) && node_value instanceof List) {
				return ((List)node_value).contains(value);
			} else if (Operator.CONTAINS_NOT.equals(operator) && node_value instanceof List) {
				return !((List)node_value).contains(value);
			}
			
			log.error("unknown operation " + operator.name());
			return false;
		};
	}

	private static Object getValue(final Map<String, Object> map, final String field) {
		String[] keys = field.split("\\.");
		Map subMap = map;
		for (int i = 0; i < keys.length - 1; i++) {
			subMap = (Map<String, Object>) subMap.getOrDefault(keys[i], Collections.emptyMap());
		}
		return subMap.get(keys[keys.length - 1]);
	}
}
