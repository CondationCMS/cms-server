package com.condation.cms.templates.renderer;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ScopeStackTest {
    private ScopeStack scopeStack;

    @BeforeEach
    void setUp() {
        scopeStack = new ScopeStack();
    }

    @Test
    void testGlobalScopeVariable() {
        scopeStack.setVariable("globalVar", "Global Value");
        assertEquals(Optional.of("Global Value"), scopeStack.getVariable("globalVar"));
    }

    @Test
    void testVariableInNewScope() {
        scopeStack.setVariable("globalVar", "Global Value");

        scopeStack.pushScope();
        scopeStack.setVariable("localVar", "Local Value");

        assertEquals(Optional.of("Global Value"), scopeStack.getVariable("globalVar"));
        assertEquals(Optional.of("Local Value"), scopeStack.getVariable("localVar"));

        scopeStack.popScope();
        assertEquals(Optional.of("Global Value"), scopeStack.getVariable("globalVar"));
        assertEquals(Optional.empty(), scopeStack.getVariable("localVar")); // localVar sollte entfernt sein
    }

    @Test
    void testOverwriteVariableInLowerScope() {
        scopeStack.setVariable("var", "Initial Value");

        scopeStack.pushScope();
        scopeStack.setVariable("var", "Overwritten Value");

        assertEquals(Optional.of("Overwritten Value"), scopeStack.getVariable("var"));

        scopeStack.popScope();
        assertEquals(Optional.of("Overwritten Value"), scopeStack.getVariable("var"));
    }

    @Test
    void testAddAndRemoveScopes() {
        scopeStack.setVariable("var", "Global Value");

        scopeStack.pushScope();
        scopeStack.setVariable("newVar", "Value in New Scope");

        assertEquals(Optional.of("Global Value"), scopeStack.getVariable("var"));
        assertEquals(Optional.of("Value in New Scope"), scopeStack.getVariable("newVar"));

        scopeStack.popScope();
        assertEquals(Optional.of("Global Value"), scopeStack.getVariable("var"));
        assertEquals(Optional.empty(), scopeStack.getVariable("newVar")); // newVar sollte entfernt sein
    }

    @Test
    void testMultipleScopeVariableResolution() {
        scopeStack.setVariable("var", "Global Value");

        scopeStack.pushScope();
        scopeStack.setVariable("var", "Value in First Scope");

        scopeStack.pushScope();
        assertEquals(Optional.of("Value in First Scope"), scopeStack.getVariable("var"));

        scopeStack.setVariable("var", "Value in Second Scope");
        assertEquals(Optional.of("Value in Second Scope"), scopeStack.getVariable("var"));

        scopeStack.popScope();
        assertEquals(Optional.of("Value in Second Scope"), scopeStack.getVariable("var"));

        scopeStack.popScope();
        assertEquals(Optional.of("Value in Second Scope"), scopeStack.getVariable("var"));
    }

    @Test
    void testPopGlobalScopeThrowsException() {
        assertThrows(IllegalStateException.class, () -> scopeStack.popScope(), "Globaler Scope darf nicht entfernt werden.");
    }
}
