package com.github.thmarx.cms.filesystem.query;

/*-
 * #%L
 * cms-filesystem
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
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.utils.MapUtil;
import com.github.thmarx.cms.filesystem.index.SecondaryIndex;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public final class QueryUtil {

	public static enum Operator {
		CONTAINS,
		CONTAINS_NOT,
		IN,
		NOT_IN,
		EQ,
		NOT_EQ,
		GT,
		GTE,
		LT,
		LTE;
	}

	private static Map<Operator, Filter> filters = new HashMap<>();

	static {
		filters.put(Operator.EQ, (node_value, value) -> Objects.equals(node_value, value));
		filters.put(Operator.NOT_EQ, (node_value, value) -> !Objects.equals(node_value, value));
		filters.put(Operator.CONTAINS, (node_value, value) -> ((List) node_value).contains(value));
		filters.put(Operator.CONTAINS_NOT, (node_value, value) -> !((List) node_value).contains(value));
		filters.put(Operator.GT, (node_value, value) -> compare(node_value, value) > 0);
		filters.put(Operator.GTE, (node_value, value) -> compare(node_value, value) >= 0);
		filters.put(Operator.LT, (node_value, value) -> compare(node_value, value) < 0);
		filters.put(Operator.LTE, (node_value, value) -> compare(node_value, value) <= 0);
		filters.put(Operator.IN, (node_value, value) -> {
			List<?> values = Collections.emptyList();
			if (value instanceof List) {
				values = (List<?>) value;
			} else if (value.getClass().isArray()) {
				values = Arrays.asList((Object[]) value);
			}
			return values.contains(node_value);
		});
		filters.put(Operator.NOT_IN, (node_value, value) -> {
			List<?> values = Collections.emptyList();
			if (value instanceof List) {
				values = (List<?>) value;
			} else if (value.getClass().isArray()) {
				values = Arrays.asList((Object[]) value);
			}
			return !values.contains(node_value);
		});
	}

	private static List<String> operations = List.of(
			"=" , "!=", ">", ">=", "<", "<=",
			"in", "not in", "contains", "not contains"
			);
	public static boolean isDefaultOperation (final String operation) {
		return operations.contains(operation);
	}
	
	public static Operator operator4String(final String operator) {
		if (Strings.isNullOrEmpty(operator)) {
			return Operator.EQ;
		}
		return switch (operator) {
			case "=" ->
				Operator.EQ;
			case "!=" ->
				Operator.NOT_EQ;
			case ">" ->
				Operator.GT;
			case ">=" ->
				Operator.GTE;
			case "<" ->
				Operator.LT;
			case "<=" ->
				Operator.LTE;
			case "in" ->
				Operator.IN;
			case "not in" ->
				Operator.NOT_IN;
			case "contains" ->
				Operator.CONTAINS;
			case "not contains" ->
				Operator.CONTAINS_NOT;
			default ->
				throw new IllegalArgumentException("unknown operator " + operator);
		};
	}

	protected static Map<Object, List<ContentNode>> groupby(final Stream<ContentNode> nodes, final String field) {
		return nodes.collect(Collectors.groupingBy((node) -> MapUtil.getValue(node.data(), field)));
	}

	protected static QueryContext<?> sorted(final QueryContext<?> context, final String field, final boolean asc) {

		var tempNodes = context.getNodes().sorted(
				(node1, node2) -> {
					var value1 = MapUtil.getValue(node1.data(), field);
					var value2 = MapUtil.getValue(node2.data(), field);

					return compare(value1, value2);
				}
		).toList();

		if (!asc) {
			tempNodes = tempNodes.reversed();
		}

		context.setNodes(tempNodes.stream());

		return context;
	}

	private static int compare(Object o1, Object o2) {
		if (Objects.equals(o1, o2)) {
			return 0;
		}
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}

		if (!o1.getClass().equals(o2.getClass())) {
			return 0;
		}

		if (o1 instanceof Float) {
			return Float.compare((float) o1, (float) o2);
		} else if (o1 instanceof Double) {
			return Double.compare((double) o1, (double) o2);
		} else if (o1 instanceof Short) {
			return Short.compare((short) o1, (short) o2);
		} else if (o1 instanceof Integer) {
			return Integer.compare((int) o1, (int) o2);
		} else if (o1 instanceof Long) {
			return Long.compare((long) o1, (long) o2);
		} else if (o1 instanceof String string) {
			return string.compareTo((String) o2);
		} else if (o1 instanceof Date date) {
			return date.compareTo((Date) o2);
		}

		return 0;
	}

	protected static QueryContext<?> filteredWithIndex(final QueryContext<?> context, final String field, final Object value, final Operator operator) {

		if (Operator.EQ.equals(operator)) {
			SecondaryIndex<Object> index = (SecondaryIndex<Object>) context.getIndexProviding().getOrCreateIndex(field, node -> MapUtil.getValue(node.data(), field));
			context.setNodes(context.getNodes().filter(node -> index.eq(node, value)));
			return context;
		} else {
			context.setNodes(context.getNodes().filter(createPredicate(field, value, operator)));
			return context;
		}
	}

	protected static QueryContext filtered(final QueryContext context, final String field, final Object value, final Operator operator) {
		context.setNodes(context.getNodes().filter(createPredicate(field, value, operator)));
		return context;
	}

	private static Predicate<? super ContentNode> createPredicate(final String field, final Object value, final Operator operator) {
		return (node) -> {
			var node_value = MapUtil.getValue(node.data(), field);

			if (node_value == null) {
				return false;
			}

			if (filters.containsKey(operator)) {
				return filters.get(operator).matches(node_value, value);
			}

			log.error("unknown operation " + operator.name());
			return false;
		};
	}

	protected static QueryContext filter_extension(final QueryContext context, final String field, final Object value, final BiPredicate<Object, Object> predicate) {
		context.setNodes(context.getNodes().filter(createExtensionPredicate(field, value, predicate)));
		return context;
	}

	private static Predicate<? super ContentNode> createExtensionPredicate(final String field, final Object value, final BiPredicate<Object, Object> predicate) {
		return (node) -> {
			var node_value = MapUtil.getValue(node.data(), field);

			if (node_value == null) {
				return false;
			}
			
			return predicate.test(node_value, value);
		};
	}
	
}
