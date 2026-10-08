package com.condation.cms.server.configs;

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

import java.io.IOException;

/** Adapts providers that can throw checked exceptions to injector factories. */
final class ProviderSupport {

    private ProviderSupport() {
    }

    @FunctionalInterface
    interface Factory<T> {
        T create() throws IOException;
    }

    static <T> T provide(Factory<T> factory) {
        try {
            return factory.create();
        } catch (IOException | RuntimeException exception) {
            throw new ProviderSupportException("Unable to create injector binding", exception);
        }
    }
    
    public static class ProviderSupportException extends RuntimeException {
        public ProviderSupportException (String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
