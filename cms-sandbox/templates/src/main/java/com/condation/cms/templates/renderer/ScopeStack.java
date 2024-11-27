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

import java.util.*;

public class ScopeStack {
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();

	public ScopeStack parent;
	
	public ScopeStack ( ScopeStack parent ) {
		pushScope();
		this.parent = parent;
	}
	
    public ScopeStack() {
        // Füge den globalen Scope hinzu, der immer verfügbar ist
        pushScope();
    }
	
	public ScopeStack(Map<String, Object> root) {
        pushScope(root);
    }

    // Fügt einen neuen Scope hinzu
    public void pushScope() {
        pushScope(new HashMap<>());
    }
	
	public void pushScope(Map<String, Object> values) {
        scopes.push(new HashMap<>(values));
    }

    // Entfernt den obersten Scope und alle Variablen darin
    public void popScope() {
        if (scopes.size() > 1) { // Der globale Scope darf nicht entfernt werden
            scopes.pop();
        } else {
            throw new IllegalStateException("Globaler Scope darf nicht entfernt werden.");
        }
    }

    // Setzt eine Variable im entsprechenden Scope
    public void setVariable(String name, Object value) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, value); // Überschreibt die Variable, wenn sie existiert
                return;
            }
        }
        // Wenn die Variable nicht existiert, wird sie im aktuellen Scope gesetzt
        scopes.peek().put(name, value);
    }

    // Ruft eine Variable ab, beginnend im obersten Scope
    public Optional<Object> getVariable(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return Optional.of(scope.get(name));
            }
        }
		if (parent != null) {
			return parent.getVariable(name);
		}
        return Optional.empty(); // Variable nicht gefunden
    }
}
