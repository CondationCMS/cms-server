package com.github.thmarx.cms.media;

/*-
 * #%L
 * cms-media
 * %%
 * Copyright (C) 2023 Marx-Software
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

import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MediaServiceTest {
	
	static FileMediaService mediaService;
	
	@BeforeAll
	public static void setup () throws IOException {
		mediaService = new FileMediaService(Path.of("src/test/resources/assets"));
	}

	@Test
	public void media_not_exists() {
		Assertions.assertThat(mediaService.hasMetaData("no-media.jpg")).isFalse();
		
		var media = mediaService.get("no-media.jpg");
		Assertions.assertThat(media.exists()).isFalse();
	}
	
	@Test
	public void media_exists_without_meta() {
		Assertions.assertThat(mediaService.hasMetaData("demo.jpg")).isFalse();
		var media = mediaService.get("demo.jpg");
		Assertions.assertThat(media.exists()).isTrue();
		Assertions.assertThat(media.meta()).isEmpty();
	}
	
	@Test
	public void media_exists_with_meta() {
		Assertions.assertThat(mediaService.hasMetaData("test.jpg")).isTrue();
		var media = mediaService.get("test.jpg");
		Assertions.assertThat(media.exists()).isTrue();
		Assertions.assertThat(media.meta()).isNotEmpty();
	}
	
	@Test
	public void subfolder_media_exists_without_meta() {
		Assertions.assertThat(mediaService.hasMetaData("images/demo.jpg")).isFalse();
	}
	
	@Test
	public void subfolder_media_exists_with_meta() {
		Assertions.assertThat(mediaService.hasMetaData("images/test.jpg")).isTrue();
	}
}
