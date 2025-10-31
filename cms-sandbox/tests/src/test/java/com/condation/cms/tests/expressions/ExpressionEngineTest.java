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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEngineTest {

    private ExpressionEngine engine;
    private Map<String, Object> ctx;

    @BeforeEach
    void setup() {
        engine = new ExpressionEngine();

        // Beispiel: einfache globale Methode
        engine.registerMethod("contains", argsList -> {
            if (argsList.size() < 2) return false;
            Object val = argsList.get(0);
            Object part = argsList.get(1);
            return val != null && val.toString().contains(part.toString());
        });

        ctx = new HashMap<>();
        ctx.put("user", Map.of("name", "Thorsten", "age", 42));
        ctx.put("numbers", Arrays.asList(1, 2, 3, 4));
        ctx.put("nested", Map.of("inner", Map.of("key", "value")));
    }

    @Test
    void testBooleanAndNullLiterals() {
        assertThat(engine.evaluate("true", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("false", ctx)).isEqualTo(false);
        assertThat(engine.evaluate("null", ctx)).isNull();
    }

    @Test
    void testNumberLiterals() {
        assertThat(engine.evaluate("123", ctx)).isEqualTo(123);
        assertThat(engine.evaluate("-5", ctx)).isEqualTo(-5);
        assertThat(engine.evaluate("3.14", ctx)).isEqualTo(3.14);
    }

    @Test
    void testStringLiterals() {
        assertThat(engine.evaluate("\"Hello\"", ctx)).isEqualTo("Hello");
        assertThat(engine.evaluate("\"123\"", ctx)).isEqualTo("123");
    }

    @Test
    void testListAndMapLiterals() {
        assertThat((List<Integer>)engine.evaluate("[1, 2, 3]", ctx)).containsExactly(1, 2, 3);
        Map<String, Object> expectedMap = new LinkedHashMap<>();
        expectedMap.put("x", 1);
        expectedMap.put("y", 2);
        assertThat(engine.evaluate("{x: 1, y: 2}", ctx)).isEqualTo(expectedMap);
    }

    @Test
    void testOperators() {
        ctx.put("val1", 10);
        ctx.put("val2", 20);

        assertThat(engine.evaluate("val1 eq 10", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("val1 lt val2", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("val1 lte val2", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("val2 gt val1", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("val2 gte val1", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("true and false", ctx)).isEqualTo(false);
        assertThat(engine.evaluate("true or false", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("not true", ctx)).isEqualTo(false);
    }

    @Test
    void testObjectAndMapAccess() {
        assertThat(engine.evaluate("user.name", ctx)).isEqualTo("Thorsten");
        assertThat(engine.evaluate("user.age", ctx)).isEqualTo(42);
        assertThat(engine.evaluate("nested.inner.key", ctx)).isEqualTo("value");
        ctx.put("data", Map.of("users", Arrays.asList(Map.of("name", "John"), Map.of("name", "Jane"))));
        assertThat(engine.evaluate("data.users[1].name", ctx)).isEqualTo("Jane");
    }

    @Test
    void testListAccess() {
        assertThat(engine.evaluate("numbers[0]", ctx)).isEqualTo(1);
        assertThat(engine.evaluate("numbers[3]", ctx)).isEqualTo(4);
    }

    @Test
    void testGlobalMethods() {
        assertThat(engine.evaluate("contains(user.name, \"ors\")", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("contains(user.name, \"xyz\")", ctx)).isEqualTo(false);
    }

    @Test
    void testComplexExpression() {
        assertThat(engine.evaluate("(user.age gt 30) and contains(user.name, \"Thor\")", ctx)).isEqualTo(true);
        assertThat(engine.evaluate("not (user.age lt 20) or contains(user.name, \"xyz\")", ctx)).isEqualTo(true);
    }

    @Test
    void testExceptionHandling() {
        assertThatThrownBy(() -> engine.evaluate("user.nonexistent", ctx))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Could not resolve part 'nonexistent'");

        assertThatThrownBy(() -> engine.evaluate("numbers[99]", ctx))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Index 99 out of bounds");

        assertThatThrownBy(() -> engine.evaluate("user.name[0]", ctx))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Cannot access by index on non-list object");

        assertThatThrownBy(() -> engine.evaluate("10 eq ", ctx))
            .isInstanceOf(ExpressionParseException.class)
            .hasMessageContaining("Missing right operand for operator: eq");

        assertThatThrownBy(() -> engine.evaluate("nonexistent.value", ctx))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Cannot resolve part 'value' on null object");
    }
}
