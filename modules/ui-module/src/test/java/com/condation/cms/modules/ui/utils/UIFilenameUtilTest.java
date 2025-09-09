package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
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

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author thorstenmarx
 */
public class UIFilenameUtilTest {
	
 @Test
    void testCreateSectionFileName() {
        // einfache Datei
        assertThat(UIFileNameUtil.createSectionFileName("index.md", "section", "item"))
                .isEqualTo("index.section.item.md");

        // andere Datei
        assertThat(UIFileNameUtil.createSectionFileName("about.md", "sec", "bla"))
                .isEqualTo("about.sec.bla.md");

        // Datei in Unterordner
        assertThat(UIFileNameUtil.createSectionFileName("ordner/file.md", "new", "one"))
                .isEqualTo("ordner/file.new.one.md");

        // Datei ohne Endung
        assertThat(UIFileNameUtil.createSectionFileName("ordner/sub/file", "new", "two"))
                .isEqualTo("ordner/sub/file.new.two");

        // nur Dateiname
        assertThat(UIFileNameUtil.createSectionFileName("file.txt", "v2", "2025"))
                .isEqualTo("file.v2.2025.txt");
    }
	
}
