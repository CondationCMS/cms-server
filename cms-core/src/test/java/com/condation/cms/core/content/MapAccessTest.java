package com.condation.cms.core.content;

/*-
 * #%L
 * cms-core
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

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MapAccessTest {

    static MapAccess sut;

    @BeforeAll
    static void setup () {
        sut = new MapAccess(Map.of(
            "title", "The title",
            "seo", Map.of(
                "title", "Seo title"
            )
        ));
    }
    
    @Test
    void test_containsKey_true () {
        Assertions.assertThat(sut.containsKey("title")).isTrue();
        Assertions.assertThat(sut.containsKey("seo.title")).isTrue();
    }

    @Test
    void test_containsKey_false() {
        Assertions.assertThat(sut.containsKey("desc")).isFalse();
        Assertions.assertThat(sut.containsKey("seo.desc")).isFalse();
    }

    @Test
    void test_get_with_value() {
        Assertions.assertThat(sut.get("title")).isEqualTo("The title");
        Assertions.assertThat(sut.get("seo.title")).isEqualTo("Seo title");
    }

    @Test
    void test_get_without_value() {
        Assertions.assertThat(sut.get("desc")).isNull();
        Assertions.assertThat(sut.get("seo.desc")).isNull();
    }

    @Test
    void test_getOrDefault() {
        Assertions.assertThat(sut.getOrDefault("desc", "default desc")).isEqualTo("default desc");
        Assertions.assertThat(sut.getOrDefault("seo.desc", "default seo desc")).isEqualTo("default seo desc");
    }
}
