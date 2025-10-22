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
 * Unterstützt: - Objektzugriff über : - Map-Keys, Listen, Methodenaufrufe -
 * Vergleichs- und logische Operatoren (eq, lt, lte, gt, gte, and, or, not) -
 * Erweiterbare globale Funktionen - Parser für komplexe, verschachtelte
 * Ausdrücke mit Klammern
 */
public class ExpressionEngine {

	private final Map<String, BiFunction<Object, Object, Object>> operators = new LinkedHashMap<>();
	private final Map<String, Function<List<Object>, Object>> globalMethods = new HashMap<>();

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
		if (o instanceof Boolean b) {
			return b;
		}
		if (o instanceof Number n) {
			return n.doubleValue() != 0.0;
		}
		if (o instanceof String s) {
			return Boolean.parseBoolean(s);
		}
		return o != null;
	}

	private int compare(Object a, Object b) {
		if (a instanceof Comparable && b != null && a.getClass().isAssignableFrom(b.getClass())) {
			return ((Comparable) a).compareTo(b);
		}
		throw new IllegalArgumentException("Cannot compare " + a + " and " + b);
	}

	public void registerOperator(String name, BiFunction<Object, Object, Object> op) {
		operators.put(name, op);
	}

	public void registerMethod(String name, Function<List<Object>, Object> func) {
		globalMethods.put(name, func);
	}

	/**
	 * Führt einen komplexen Ausdruck aus, inkl. verschachtelter Operatoren und
	 * Klammern.
	 */
	public Object evaluate(String expression, Map<String, Object> context) {
		ExpressionParser parser = new ExpressionParser(this, context);
		return parser.parse(expression);
	}

	Object resolve(String expr, Map<String, Object> context) {
		if (expr == null || expr.isEmpty()) {
			return null;
		}

		// String Literals
		if (expr.startsWith("\"") && expr.endsWith("\"")) {
			return expr.substring(1, expr.length() - 1);
		}

		// Numbers
		if (expr.matches("[0-9]+")) {
			return Integer.parseInt(expr);
		}
		if (expr.matches("[0-9]+\\.[0-9]+")) {
			return Double.parseDouble(expr);
		}

		// Boolean
		if (expr.equals("true")) {
			return true;
		}
		if (expr.equals("false")) {
			return false;
		}

		// NOT-Operator
		if (expr.startsWith("not ")) {
			Object val = resolve(expr.substring(4).trim(), context);
			return !toBool(val);
		}

		// Function call
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

		// Object/Map resolution with :
		String[] parts = expr.split(":");
		Object current = context.get(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			current = resolvePart(current, parts[i]);
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
				if (c == '(') {
					depth++;
				}
				if (c == ')') {
					depth--;
				}
				current.append(c);
			}
		}
		if (current.length() > 0) {
			args.add(current.toString());
		}
		return args;
	}

	private Object resolvePart(Object base, String part) {
		if (base == null) {
			return null;
		}

		// Liste: e.g. users[0]
		if (part.matches(".+\\[\\d+\\]")) {
			String name = part.substring(0, part.indexOf('['));
			int idx = Integer.parseInt(part.replaceAll(".*\\[(\\d+)\\].*", "$1"));
			base = resolvePart(base, name);
			if (base instanceof List<?> list) {
				return list.get(idx);
			}
		}

		// Map
		if (base instanceof Map<?, ?> map && map.containsKey(part)) {
			return map.get(part);
		}

		// Try getter/method/field
		try {
			// Direct method
			try {
				Method m = base.getClass().getMethod(part);
				return m.invoke(base);
			} catch (NoSuchMethodException ignored) {
			}

			// getXxx()
			String getter = "get" + Character.toUpperCase(part.charAt(0)) + part.substring(1);
			try {
				Method m = base.getClass().getMethod(getter);
				return m.invoke(base);
			} catch (NoSuchMethodException ignored) {
			}

			// Field
			try {
				Field f = base.getClass().getDeclaredField(part);
				f.setAccessible(true);
				return f.get(base);
			} catch (NoSuchFieldException ignored) {
			}
		} catch (Exception e) {
			throw new RuntimeException("Error resolving part: " + part, e);
		}
		return null;
	}

	/**
	 * Parser für komplexe logische Ausdrücke mit Klammern und logischen
	 * Operatoren.
	 */
	private static class ExpressionParser {

		private final ExpressionEngine engine;
		private final Map<String, Object> context;

		ExpressionParser(ExpressionEngine engine, Map<String, Object> context) {
			this.engine = engine;
			this.context = context;
		}

		public Object parse(String expr) {
			expr = expr.trim();
			// Entferne äußere Klammern, falls vollständig umschließend
			if (expr.startsWith("(") && expr.endsWith(")") && isBalanced(expr.substring(1, expr.length() - 1))) {
				expr = expr.substring(1, expr.length() - 1).trim();
			}

			// Suche Operator auf oberster Ebene
			for (String op : engine.operators.keySet()) {
				int idx = findTopLevelOperator(expr, op);
				if (idx > 0) {
					String left = expr.substring(0, idx).trim();
					String right = expr.substring(idx + op.length()).trim();
					Object lVal = parse(left);
					Object rVal = parse(right);
					return engine.operators.get(op).apply(lVal, rVal);
				}
			}
			return engine.resolve(expr, context);
		}

		private int findTopLevelOperator(String expr, String op) {
			int depth = 0;
			for (int i = 0; i < expr.length() - op.length() + 1; i++) {
				char c = expr.charAt(i);
				if (c == '(') {
					depth++;
				}
				if (c == ')') {
					depth--;
				}
				if (depth == 0 && expr.startsWith(op, i)) {
					boolean leftSpace = i == 0 || Character.isWhitespace(expr.charAt(i - 1));
					boolean rightSpace = (i + op.length() >= expr.length()) || Character.isWhitespace(expr.charAt(i + op.length()));
					if (leftSpace && rightSpace) {
						return i;
					}
				}
			}
			return -1;
		}

		private boolean isBalanced(String s) {
			int depth = 0;
			for (char c : s.toCharArray()) {
				if (c == '(') {
					depth++;
				}
				if (c == ')') {
					depth--;
				}
				if (depth < 0) {
					return false;
				}
			}
			return depth == 0;
		}
	}

	// Beispielmain
	public static void main(String[] args) {
		ExpressionEngine engine = new ExpressionEngine();
		engine.registerMethod("len", argsList -> {
			Object val = argsList.get(0);
			if (val instanceof Collection<?> col) {
				return col.size();
			}
			if (val instanceof Map<?, ?> map) {
				return map.size();
			}
			return val != null ? val.toString().length() : 0;
		});
		// Ergänzung im Konstruktor oder in main():
		engine.registerMethod("contains", argsList -> {
			if (argsList.size() < 2) {
				return false;
			}
			Object val = argsList.get(0);
			Object part = argsList.get(1);
			return val != null && val.toString().contains(String.valueOf(part));
		});

		engine.registerMethod("startsWith", argsList -> {
			if (argsList.size() < 2) {
				return false;
			}
			Object val = argsList.get(0);
			Object prefix = argsList.get(1);
			return val != null && val.toString().startsWith(String.valueOf(prefix));
		});

		engine.registerMethod("endsWith", argsList -> {
			if (argsList.size() < 2) {
				return false;
			}
			Object val = argsList.get(0);
			Object suffix = argsList.get(1);
			return val != null && val.toString().endsWith(String.valueOf(suffix));
		});

		Map<String, Object> ctx = new HashMap<>();
		ctx.put("user", Map.of("name", "Thorsten", "age", 42));

		System.out.println(engine.evaluate("(user:age gt 30) and (user:name eq \"Thorsten\")", ctx)); // true
		System.out.println(engine.evaluate("not (user:age lt 20)", ctx)); // true
		System.out.println(engine.evaluate("(len(\"Hallo\") eq 5) or (user:age lt 10)", ctx)); // true
		
		System.out.println(engine.evaluate("contains(user:name, \"ors\")", ctx)); // true
		System.out.println(engine.evaluate("startsWith(user:name, \"Tho\")", ctx)); // true
		System.out.println(engine.evaluate("endsWith(user:name, \"ten\")", ctx)); // true

	}
}
