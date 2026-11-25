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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ExtensionTypesTest {

    @Test
    void should_find_type_by_id() {
        assertThat(ExtensionTypes.byId("shortcode")).isPresent();
    }

    @Test
    void should_return_empty_when_type_unknown() {
        assertThat(ExtensionTypes.byId("nope")).isEmpty();
    }

    @Test
    void should_have_unique_ids() {
        var ids = ExtensionTypes.all().stream().map(ExtensionType::id).toList();

        assertThat(ids).doesNotHaveDuplicates();
    }
}
