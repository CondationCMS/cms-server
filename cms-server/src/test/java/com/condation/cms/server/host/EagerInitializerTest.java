package com.condation.cms.server.host;

/*-
 * #%L
 * CMS Server
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

import com.condation.cms.core.injector.DefaultInjector;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EagerInitializerTest {

    @Test
    void initializesMarkedProviderAndTypeOnlyOnce() {
        var providerCalls = new AtomicInteger();
        var typeCalls = new AtomicInteger();
        var lazyCalls = new AtomicInteger();

        var injector = DefaultInjector.create(bindings -> {
            bindings.register(EagerType.class, _ -> new EagerType(new CounterHolder(typeCalls, lazyCalls))).eager();
            bindings.register(LazyType.class, _ -> new LazyType(new CounterHolder(typeCalls, lazyCalls))).singleton();
            bindings.register(EagerService.class, _ -> {
                providerCalls.incrementAndGet();
                return new EagerService();
            }).eager();
        });

        assertEquals(0, providerCalls.get());
        EagerInitializer.initialize(injector);
        EagerInitializer.initialize(injector);

        assertEquals(1, providerCalls.get());
        assertEquals(1, typeCalls.get());
        assertEquals(0, lazyCalls.get());
    }

    private record CounterHolder(AtomicInteger eager, AtomicInteger lazy) {
    }

    private static class EagerService {
    }

    private static class EagerType {
        EagerType(CounterHolder counters) {
            counters.eager().incrementAndGet();
        }
    }

    private static class LazyType {
        LazyType(CounterHolder counters) {
            counters.lazy().incrementAndGet();
        }
    }
}
