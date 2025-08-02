package com.condation.cms.media;

/*-
 * #%L
 * cms-media
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

import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class ImageSizeTest {
	
	public ImageSizeTest() {
	}

	@Test
	public void image_does_not_exists() {
		var size = ImageSize.getSize(Path.of("no.jpg"));
		
		Assertions.assertThat(size.width()).isEqualTo(-1);
		Assertions.assertThat(size.height()).isEqualTo(-1);
	}
	
	@Test
	public void image_with_correct_size () {
		var size = ImageSize.getSize(Path.of("src/test/resources/assets/test.jpg"));
		Assertions.assertThat(size.width()).isEqualTo(689);
		Assertions.assertThat(size.height()).isEqualTo(689);
	}
}
