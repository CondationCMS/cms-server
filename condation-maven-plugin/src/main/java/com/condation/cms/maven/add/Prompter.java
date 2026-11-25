package com.condation.cms.maven.add;

/*-
 * #%L
 * CondationCMS Maven Plugin
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

import java.util.List;
import java.util.function.Predicate;

/**
 * Asks the user for values. All methods throw {@link UserAbortException} if the user cancels
 * (Ctrl+C, end of input) and {@link PromptUnavailableException} if there is no way to ask (no terminal).
 */
public interface Prompter {

    /** One of the given choices, <code>defaultValue</code> when the user just presses enter. */
    String select(String label, List<String> choices, String defaultValue);

    /** Free text; asks again while the validator rejects the input. Empty input returns the default. */
    String ask(String label, String defaultValue, Predicate<String> validator, String errorMessage);

    boolean confirm(String label, boolean defaultValue);
}
