package com.condation.cms.media;

/*-
 * #%L
 * cms-media
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

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class CropTest {

	@Test
	public void test_500x500() throws Exception {
		var input = ImageIO.read(new File("testdata/pexels-pixabay-147411.jpg"));

		BufferedImage output = Thumbnails.of(input)
//				.sourceRegion(Positions.CENTER, 500, 500)
				.crop(Positions.CENTER)
				.size(500, 500)
				.asBufferedImage();

		ImageIO.write(output, "jpg", new File("target/pexels-pixabay-147411-500x500.jpg"));
	}

	@Test
	public void test_1300x500() throws Exception {
		var input = ImageIO.read(new File("testdata/pexels-pixabay-147411.jpg"));

		BufferedImage output = Thumbnails.of(input)
//				.sourceRegion(Positions.CENTER, 1300, 500)
				.crop(Positions.CENTER)
				.size(1300, 500)
				.asBufferedImage();

		ImageIO.write(output, "jpg", new File("target/pexels-pixabay-147411-1300x500.jpg"));
	}

	@Test
	public void test_800x300() throws Exception {
		// 2200 x 1440
		var input = ImageIO.read(new File("testdata/pexels-pixabay-147411.jpg"));

		BufferedImage output = Thumbnails.of(input)
//				.sourceRegion(Positions.CENTER, 800, 300)
				.crop(Positions.CENTER)
				.size(800, 300)
				.asBufferedImage();

		ImageIO.write(output, "jpg", new File("target/pexels-pixabay-147411-800x300.jpg"));
	}

	@Test
	public void test_2_500x500() throws Exception {
		var input = ImageIO.read(new File("testdata/pexels-pixabay-36717.jpg"));

		BufferedImage output = Thumbnails.of(input)
//				.sourceRegion(Positions.CENTER, 500, 500)
				.crop(Positions.CENTER)
				.size(500, 500)
				.asBufferedImage();

		ImageIO.write(output, "jpg", new File("target/pexels-pixabay-36717-500x500.jpg"));
	}

	@Test
	public void test_2_1300x500() throws Exception {
		var input = ImageIO.read(new File("testdata/pexels-pixabay-36717.jpg"));

		BufferedImage output = Thumbnails.of(input)
//				.sourceRegion(Positions.CENTER, 1300, 500)
				.crop(Positions.CENTER)
				.size(1300, 500)
				.asBufferedImage();

		ImageIO.write(output, "jpg", new File("target/pexels-pixabay-36717-1300x500.jpg"));
	}

	@Test
	public void test_2_800x300() throws Exception {
		// 2200 x 1440
		var input = ImageIO.read(new File("testdata/pexels-pixabay-36717.jpg"));

		BufferedImage output = Thumbnails.of(input)
				//.sourceRegion(Positions.CENTER, 800, 300)
				.crop(Positions.CENTER)
				.size(800, 300)
				.asBufferedImage();

		ImageIO.write(output, "jpg", new File("target/pexels-pixabay-36717-800x300.jpg"));
	}
	
}
