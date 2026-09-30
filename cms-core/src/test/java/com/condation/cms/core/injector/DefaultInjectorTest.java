package com.condation.cms.core.injector;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultInjectorTest {

    @Test
    void duplicateBindingInSameInjectorFailsDuringModuleRegistration() {
        var exception = assertThrows(IllegalStateException.class, () -> DefaultInjector.create(
                bindings -> bindings.register(String.class, _ -> "first"),
                bindings -> bindings.register(String.class, _ -> "second")
        ));

        assertTrue(exception.getMessage().contains(String.class.getName()));
    }

    @Test
    void namedBindingsAreDistinctByNameAndTypeAndCannotBeOverwritten() {
        var injector = DefaultInjector.create(bindings -> {
            bindings.register("one", String.class, _ -> "first");
            bindings.register("two", String.class, _ -> "second");
            bindings.register("one", Integer.class, _ -> 42);
        });

        var exception = assertThrows(IllegalStateException.class,
                () -> injector.register("one", String.class, _ -> "replacement"));

        assertTrue(exception.getMessage().contains("one"));
        assertEquals("first", injector.getInstance("one", String.class));
        assertEquals("second", injector.getInstance("two", String.class));
        assertEquals(42, injector.getInstance("one", Integer.class));
    }

    @Test
    void parentSingletonUsesParentDependenciesWhenResolvedThroughChild() {
        var parent = DefaultInjector.create(bindings -> {
            bindings.register(String.class, _ -> "parent").singleton();
            bindings.register(Service.class, i -> new Service(i.getInstance(String.class))).singleton();
        });
        var child = DefaultInjector.create(parent,
                bindings -> bindings.register(String.class, _ -> "child").singleton());

        assertSame(parent.getInstance(Service.class), child.getInstance(Service.class));
        assertEquals("parent", child.getInstance(Service.class).value());
        assertEquals("child", child.getInstance(String.class));
    }

    private record Service(String value) {
    }
}
