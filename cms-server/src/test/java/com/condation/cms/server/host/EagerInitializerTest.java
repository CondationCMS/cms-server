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

import com.condation.cms.server.annotations.Eager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EagerInitializerTest {

    @Test
    void initializesMarkedProviderAndTypeOnlyOnce() {
        var providerCalls = new AtomicInteger();
        var typeCalls = new AtomicInteger();
        var lazyCalls = new AtomicInteger();

        var injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(EagerType.class).in(Singleton.class);
                bind(LazyType.class).in(Singleton.class);
            }

            @Provides
            @Singleton
            @Eager
            EagerService eagerService() {
                providerCalls.incrementAndGet();
                return new EagerService();
            }

            @Provides
            @Singleton
            CounterHolder counters() {
                return new CounterHolder(typeCalls, lazyCalls);
            }
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

    @Eager
    private static class EagerType {
        @com.google.inject.Inject
        EagerType(CounterHolder counters) {
            counters.eager().incrementAndGet();
        }
    }

    private static class LazyType {
        @com.google.inject.Inject
        LazyType(CounterHolder counters) {
            counters.lazy().incrementAndGet();
        }
    }
}
