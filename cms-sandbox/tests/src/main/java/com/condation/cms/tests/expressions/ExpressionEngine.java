package com.condation.cms.tests.expressions;

/*-
 * #%L
 * tests
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

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * Erweiterbare Enterprise Expression Engine mit Parser für komplexe Ausdrücke.
 * Unterstützt:
 *  - Objektzugriff über :
 *  - Map-Keys, Listen, Methodenaufrufe
 *  - Vergleichs- und logische Operatoren (eq, lt, lte, gt, gte, and, or, not)
 *  - Erweiterbare globale Funktionen
 *  - Parser für komplexe, verschachtelte Ausdrücke mit Klammern
 *  - Boolean-, Null- und Collection-Literale
 */
public class ExpressionEngine {

	private final Map<String, BiFunction<Object, Object, Object>> operators = new LinkedHashMap<>();
	private final Map<String, Function<List<Object>, Object>> globalMethods = new HashMap<>();
	private final Map<String, ParsedExpression> cache = new HashMap<>();

	// Interface for a compiled/parsed expression
	private interface ParsedExpression {
		Object evaluate(Map<String, Object> context);
	}

	public ExpressionEngine() {
		registerDefaultOperators();
	}

	private void registerDefaultOperators() {
		registerOperator("eq", (a, b) -> Objects.equals(a, b));
		registerOperator("lt", (a, b) -> compare(a, b) < 0);
		registerOperator("lte", (a, b) -> compare(a, b) <= 0);
		registerOperator("gt", (a, b) -> compare(a, b) > 0);
		registerOperator("gte", (a, b) -> compare(a, b) >= 0);
		registerOperator("and", (a, b) -> toBool(a) && toBool(b));
		registerOperator("or", (a, b) -> toBool(a) || toBool(b));
	}

	private static boolean toBool(Object o) {
		if (o instanceof Boolean b) return b;
		if (o instanceof Number n) return n.doubleValue() != 0.0;
		if (o instanceof String s) return Boolean.parseBoolean(s);
		return o != null;
	}

	private int compare(Object a, Object b) {
		if (a instanceof Comparable && b != null && a.getClass().isAssignableFrom(b.getClass())) {
			return ((Comparable) a).compareTo(b);
		}
		throw new EvaluationException("Cannot compare " + a + " and " + b);
	}

	public void registerOperator(String name, BiFunction<Object, Object, Object> op) {
		operators.put(name, op);
	}

	public void registerMethod(String name, Function<List<Object>, Object> func) {
		globalMethods.put(name, func);
	}

	public Object evaluate(String expression, Map<String, Object> context) {
		ParsedExpression parsed = cache.computeIfAbsent(expression, this::parse);
		return parsed.evaluate(context);
	}

	private ParsedExpression parse(String expression) {
		ExpressionParser parser = new ExpressionParser(this);
		return parser.parse(expression);
	}

	Object resolve(String expr, Map<String, Object> context) {
		if (expr == null || expr.isEmpty()) return null;

		expr = expr.trim();

		// === Boolean und Null Literale ===
		if (expr.equals("true")) return true;
		if (expr.equals("false")) return false;
		if (expr.equals("null")) return null;

		// === String Literals ===
		if (expr.startsWith("\"") && expr.endsWith("\"")) {
			return expr.substring(1, expr.length() - 1);
		}

		// === Zahlen ===
		if (expr.matches("-?\\d+")) return Integer.parseInt(expr);
		if (expr.matches("-?\\d+\\.\\d+")) return Double.parseDouble(expr);

		// === Listen-Literal === [1,2,3]
		if (expr.startsWith("[") && expr.endsWith("]")) {
			String inside = expr.substring(1, expr.length() - 1).trim();
			if (inside.isEmpty()) return new ArrayList<>();
			List<Object> list = new ArrayList<>();
			for (String part : splitArgs(inside)) {
				list.add(resolve(part.trim(), context));
			}
			return list;
		}

		// === Map-Literal === {x: 1, y: 2}
		if (expr.startsWith("{") && expr.endsWith("}")) {
			String inside = expr.substring(1, expr.length() - 1).trim();
			if (inside.isEmpty()) return new LinkedHashMap<>();
			Map<String, Object> map = new LinkedHashMap<>();
			for (String entry : splitArgs(inside)) {
				int sep = entry.indexOf(':');
				if (sep < 0) continue;
				String key = entry.substring(0, sep).trim();
				String val = entry.substring(sep + 1).trim();
				if (key.startsWith("\"") && key.endsWith("\"")) {
					key = key.substring(1, key.length() - 1);
				}
				map.put(key, resolve(val, context));
			}
			return map;
		}

		// === NOT-Operator ===
		if (expr.startsWith("not ")) {
			Object val = evaluate(expr.substring(4).trim(), context);
			return !toBool(val);
		}

		// === Funktionsaufruf ===
		if (expr.contains("(") && expr.endsWith(")")) {
			String name = expr.substring(0, expr.indexOf('('));
			String inside = expr.substring(expr.indexOf('(') + 1, expr.length() - 1);
			List<Object> args = new ArrayList<>();
			if (!inside.isEmpty()) {
				for (String part : splitArgs(inside)) {
					args.add(resolve(part.trim(), context));
				}
			}
			if (globalMethods.containsKey(name)) {
				return globalMethods.get(name).apply(args);
			}
		}

		// === Objektauflösung mit "." ===
		String[] parts = expr.split("\\.");
		Object current = resolvePart(context, parts[0], context);
		for (int i = 1; i < parts.length; i++) {
			current = resolvePart(current, parts[i], context);
		}
		return current;
	}

	private List<String> splitArgs(String inside) {
		List<String> args = new ArrayList<>();
		int depth = 0;
		StringBuilder current = new StringBuilder();
		for (char c : inside.toCharArray()) {
			if (c == ',' && depth == 0) {
				args.add(current.toString());
				current.setLength(0);
			} else {
				if (c == '(' || c == '[' || c == '{') depth++;
				if (c == ')' || c == ']' || c == '}') depth--;
				current.append(c);
			}
		}
		if (current.length() > 0) args.add(current.toString());
		return args;
	}

	private Object resolvePart(Object base, String part, Map<String, Object> context) {
		if (base == null) {
			throw new EvaluationException("Cannot resolve part '" + part + "' on null object");
		}

		// Liste: z. B. users[0]
		if (part.matches(".+\\[\\d+\\]")) {
			String name = part.substring(0, part.indexOf('['));
			int idx = Integer.parseInt(part.replaceAll(".*\\[(\\d+)\\].*", "$1"));
			Object listObj = resolvePart(base, name, context);

			if (listObj instanceof List<?> list) {
				if (idx >= 0 && idx < list.size()) {
					return list.get(idx);
				} else {
					throw new EvaluationException("Index " + idx + " out of bounds for list " + name);
				}
			} else {
				throw new EvaluationException("Cannot access by index on non-list object: " + name);
			}
		}

		// Map
		if (base instanceof Map<?, ?> map) {
			if (map.containsKey(part)) {
				return map.get(part);
			}
			if (base == context) {
				return null;
			}
			throw new EvaluationException("Could not resolve part '" + part + "' on object " + base);
		}

		// Try getter/method/field
		try {
			try {
				Method m = base.getClass().getMethod(part);
				return m.invoke(base);
			} catch (NoSuchMethodException ignored) {}

			String getter = "get" + Character.toUpperCase(part.charAt(0)) + part.substring(1);
			try {
				Method m = base.getClass().getMethod(getter);
				return m.invoke(base);
			} catch (NoSuchMethodException ignored) {}

			try {
				Field f = base.getClass().getDeclaredField(part);
				f.setAccessible(true);
				return f.get(base);
			} catch (NoSuchFieldException ignored) {}
		} catch (Exception e) {
			throw new EvaluationException("Error resolving part: " + part, e);
		}
		throw new EvaluationException("Could not resolve part '" + part + "' on object " + base);
	}

	private static class ExpressionParser {
		private final ExpressionEngine engine;

		ExpressionParser(ExpressionEngine engine) {
			this.engine = engine;
		}

		public ParsedExpression parse(String expr) {
			expr = expr.trim();
			if (expr.startsWith("(") && expr.endsWith(")") && isBalanced(expr.substring(1, expr.length() - 1))) {
				expr = expr.substring(1, expr.length() - 1).trim();
			}

			for (String op : engine.operators.keySet()) {
				int idx = findTopLevelOperator(expr, op);
				if (idx >= 0) {
					String left = expr.substring(0, idx).trim();
					String right = expr.substring(idx + op.length()).trim();
					if (right.isEmpty()) {
						throw new ExpressionParseException("Missing right operand for operator: " + op);
					}
					ParsedExpression lVal = parse(left);
					ParsedExpression rVal = parse(right);
					return context -> engine.operators.get(op).apply(lVal.evaluate(context), rVal.evaluate(context));
				}
			}
			final String finalExpr = expr;
			return context -> engine.resolve(finalExpr, context);
		}

		private int findTopLevelOperator(String expr, String op) {
			int depth = 0;
			for (int i = 0; i < expr.length() - op.length() + 1; i++) {
				char c = expr.charAt(i);
				if (c == '(' || c == '[' || c == '{') depth++;
				if (c == ')' || c == ']' || c == '}') depth--;
				if (depth == 0 && expr.startsWith(op, i)) {
					boolean leftSpace = i == 0 || Character.isWhitespace(expr.charAt(i - 1));
					boolean rightSpace = (i + op.length() >= expr.length())
							|| Character.isWhitespace(expr.charAt(i + op.length()));
					if (leftSpace && rightSpace) return i;
				}
			}
			return -1;
		}

		private boolean isBalanced(String s) {
			int depth = 0;
			for (char c : s.toCharArray()) {
				if (c == '(') depth++;
				if (c == ')') depth--;
				if (depth < 0) return false;
			}
			return depth == 0;
		}
	}

	// Beispielmain
	public static void main(String[] args) {
		ExpressionEngine engine = new ExpressionEngine();
		engine.registerMethod("contains", argsList -> argsList.get(0).toString().contains(argsList.get(1).toString()));

		Map<String, Object> ctx = new HashMap<>();
		ctx.put("user", Map.of("name", "Thorsten", "age", 42));

		System.out.println(engine.evaluate("true", ctx)); // true
		System.out.println(engine.evaluate("false", ctx)); // false
		System.out.println(engine.evaluate("null", ctx)); // null
		System.out.println(engine.evaluate("[1, 2, 3]", ctx)); // [1, 2, 3]
		System.out.println(engine.evaluate("{x: 1, y: 2}", ctx)); // {x=1, y=2}
	}
}
