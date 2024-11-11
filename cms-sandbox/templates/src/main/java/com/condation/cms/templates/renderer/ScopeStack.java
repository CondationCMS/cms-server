package com.condation.cms.templates.renderer;

import java.util.*;

public class ScopeStack {
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();

    public ScopeStack() {
        // Füge den globalen Scope hinzu, der immer verfügbar ist
        pushScope();
    }

    // Fügt einen neuen Scope hinzu
    public void pushScope() {
        scopes.push(new HashMap<>());
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
        return Optional.empty(); // Variable nicht gefunden
    }
}