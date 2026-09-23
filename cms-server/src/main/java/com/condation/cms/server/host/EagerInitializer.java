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
import com.google.inject.Injector;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProvidesMethodBinding;

/** Creates bindings marked with {@link Eager} before the first request. */
final class EagerInitializer {

    private EagerInitializer() {
    }

    static void initialize(Injector injector) {
        injector.getAllBindings().values().forEach(binding -> {
            boolean eagerType = binding.getKey().getTypeLiteral().getRawType().isAnnotationPresent(Eager.class);
            boolean eagerProvider = binding instanceof ProviderInstanceBinding<?> providerBinding
                    && providerBinding.getProviderInstance() instanceof ProvidesMethodBinding<?> methodBinding
                    && methodBinding.getMethod().isAnnotationPresent(Eager.class);

            if (eagerType || eagerProvider) {
                injector.getInstance(binding.getKey());
            }
        });
    }
}
