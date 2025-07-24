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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 * @author thorstenmarx
 */
public class PathUtilTest {
	
	public PathUtilTest() {
	}

	@Test
	public void test_filenames() {
		Assertions.assertThat(PathUtil.toValidFilename("das ist mein text")).isEqualTo("das-ist-mein-text");
		
		Assertions.assertThat(PathUtil.toValidFilename("das ist mein text.md")).isEqualTo("das-ist-mein-text.md");
	}
	
	
	@Test
	public void test_umlauts() {
		Assertions.assertThat(PathUtil.toValidFilename("ä")).isEqualTo("ae");
		Assertions.assertThat(PathUtil.toValidFilename("Ä")).isEqualTo("ae");
		Assertions.assertThat(PathUtil.toValidFilename("ü")).isEqualTo("ue");
		Assertions.assertThat(PathUtil.toValidFilename("Ü")).isEqualTo("ue");
		Assertions.assertThat(PathUtil.toValidFilename("Ö")).isEqualTo("oe");
		Assertions.assertThat(PathUtil.toValidFilename("Ö")).isEqualTo("oe");
		Assertions.assertThat(PathUtil.toValidFilename("ß")).isEqualTo("ss");
	}
	
}
