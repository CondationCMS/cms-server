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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * code from: http://www.java2s.com/example/java-utility-method/bufferedimage-crop/cropscale-bufferedimage-source-int-width-int-height-63a10.html
 *
 * @author t.marx
 */
public class Crop {

	/**
	 * This function crops the image to the aspect ratio of the width and height and then scales the image to the given
	 * values
	 *
	 * @param source is the image to resize
	 * @param width is the wanted width
	 * @param height is the wanted height
	 * @return a new image with the specified width and height
	 */
	public static BufferedImage cropScale(BufferedImage source, int width, int height) {
		float aspect = width / height;
		BufferedImage tmp = cropToAspectRatio(source, aspect);
		tmp = scale(tmp, width, height, false);
		return tmp;
	}

	public static BufferedImage my_crop (final BufferedImage image, final int target_width, int target_height) {
		int width = target_width;
		int height = target_height;
		
		int x = (image.getWidth() / 2) - (width / 2);
		int y = (image.getHeight() / 2) - (height / 2);
		
		if (x < 0 || y < 0) {
			x = y = 0;
		}
		
		return image.getSubimage(x, y, width, height);
	}
	
	/**
	 * Crops a image to a specific aspect ration
	 *
	 * @param image is the actual image to crop
	 * @param aspect is the aspect ratio to convert the image to
	 * @return a new image with the specified aspect ratio
	 */
	public static BufferedImage cropToAspectRatio(BufferedImage image, float aspect) {
		int x = 0, y = 0;
		int width = image.getWidth();
		int height = image.getHeight();

		// Check if the width is larger than the height
		if (width > height) {
			width = (int) (height * aspect);
			x = image.getWidth() / 2 - width / 2;
		} else {
			height = (int) (width * aspect);
			y = image.getHeight() / 2 - height / 2;
		}
		
		if (x < 0 || y < 0) {
			x = y = 0;
		}
		
		return image.getSubimage(x, y, width, height);
	}

	/**
	 * Resizes a BufferedImage
	 *
	 * @param source is the image to resize
	 * @param width is the wanted width
	 * @param height is the wanted height
	 * @param keep_aspect is if the aspect ratio of the image should be kept
	 * @return the resized image
	 */
	public static BufferedImage scale(BufferedImage source, int width, int height, boolean keep_aspect) {
		double scale_width = (double) width / source.getWidth();
		double scale_height = (double) height / source.getHeight();

		// aspect calculation
		if (keep_aspect) {
			if (scale_width * source.getHeight() > height) {
				scale_width = scale_height;
			} else {
				scale_height = scale_width;
			}
		}

		BufferedImage tmp = new BufferedImage((int) (scale_width * source.getWidth()),
				(int) (scale_height * source.getHeight()), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = tmp.createGraphics();

		AffineTransform at = AffineTransform.getScaleInstance(scale_width, scale_height);
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return tmp;
	}
}
