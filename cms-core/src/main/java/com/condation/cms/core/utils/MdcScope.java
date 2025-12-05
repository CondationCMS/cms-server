package com.condation.cms.core.utils;

/*-
 * #%L
 * cms-core
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

import org.slf4j.MDC;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for scoped MDC (Mapped Diagnostic Context) management.
 * Similar to Java's ScopedValue API but for SLF4J MDC.
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Single value
 * MdcScope.where("site", "demo")
 *     .run(() -> {
 *         log.info("Logging with site context");
 *     });
 * 
 * // Multiple values
 * MdcScope.where("site", "demo")
 *     .where("user", "admin")
 *     .run(() -> {
 *         log.info("Logging with multiple contexts");
 *     });
 * 
 * // With return value
 * String result = MdcScope.where("site", "demo")
 *     .call(() -> {
 *         log.info("Computing with context");
 *         return "result";
 *     });
 * }</pre>
 */
public final class MdcScope {
    
    private final Map<String, String> contextToSet;
    
    private MdcScope(Map<String, String> contextToSet) {
        this.contextToSet = new HashMap<>(contextToSet);
    }
    
    /**
     * Creates a new scope with a single key-value pair.
     * 
     * @param key the context key
     * @param value the context value
     * @return a new MdcScope
     */
    public static MdcScope where(String key, String value) {
        Map<String, String> context = new HashMap<>();
        context.put(key, value);
        return new MdcScope(context);
    }
    
    /**
     * Creates a new scope with multiple key-value pairs.
     * 
     * @param contextMap the context entries to set
     * @return a new MdcScope
     */
    public static MdcScope whereAll(Map<String, String> contextMap) {
        return new MdcScope(contextMap);
    }
       
    /**
     * Executes the given runnable within this context scope.
     * The context is automatically restored to its previous state after execution.
     * 
     * @param runnable the code to execute
     */
    public void run(Runnable runnable) {
        call(() -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * Executes the given supplier within this context scope and returns its result.
     * The context is automatically restored to its previous state after execution.
     * 
     * @param <T> the return type
     * @param supplier the code to execute
     * @return the result of the supplier
     */
    public <T> T call(Supplier<T> supplier) {
        // Save current state for keys we're about to set
        Map<String, String> previousValues = new HashMap<>();
        Map<String, Boolean> wasPresent = new HashMap<>();
        
        for (String key : contextToSet.keySet()) {
            String currentValue = MDC.get(key);
            if (currentValue != null) {
                previousValues.put(key, currentValue);
                wasPresent.put(key, true);
            } else {
                wasPresent.put(key, false);
            }
        }
        
        try {
            // Set new context values
            contextToSet.forEach(MDC::put);
            
            // Execute the user code
            return supplier.get();
            
        } finally {
            // Restore previous state
            for (String key : contextToSet.keySet()) {
                if (wasPresent.get(key)) {
                    // Key existed before, restore old value
                    MDC.put(key, previousValues.get(key));
                } else {
                    // Key didn't exist before, remove it
                    MDC.remove(key);
                }
            }
        }
    }
    
    /**
     * Copies the current MDC context and returns a scope that will restore it.
     * Useful for passing context to virtual threads.
     * 
     * @return a scope containing the current context
     */
    public static MdcScope current() {
        Map<String, String> currentContext = MDC.getCopyOfContextMap();
        if (currentContext == null) {
            currentContext = new HashMap<>();
        }
        return new MdcScope(currentContext);
    }
    
    /**
     * Creates a scope for a site context.
     * Convenience method for the common use case.
     * 
     * @param siteId the site identifier
     * @return a new MdcScope with site context
     */
    public static MdcScope forSite(String siteId) {
        return where("site", siteId);
    }
}
