package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * UI Module
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
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author thorstenmarx
 */
public class UIFilenameUtilTest {
	
 @Test
    void testCreateSectionFileName() {
        // einfache Datei
        assertThat(UIFileNameUtil.createSectionEntryFileName("index.md", "section", "item"))
                .isEqualTo("index.section.item.md");

        // andere Datei
        assertThat(UIFileNameUtil.createSectionEntryFileName("about.md", "sec", "bla"))
                .isEqualTo("about.sec.bla.md");

        // Datei in Unterordner
        assertThat(UIFileNameUtil.createSectionEntryFileName("ordner/file.md", "new", "one"))
                .isEqualTo("ordner/file.new.one.md");

        // Datei ohne Endung
        assertThat(UIFileNameUtil.createSectionEntryFileName("ordner/sub/file", "new", "two"))
                .isEqualTo("ordner/sub/file.new.two");

        // nur Dateiname
        assertThat(UIFileNameUtil.createSectionEntryFileName("file.txt", "v2", "2025"))
                .isEqualTo("file.v2.2025.txt");
    }
	
}
