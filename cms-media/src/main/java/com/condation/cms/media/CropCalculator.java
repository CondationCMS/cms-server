package com.condation.cms.media;

import java.awt.Rectangle;

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

public class CropCalculator {

	public static record CropArea (int x, int y, int width, int height) {
		@Override
		public String toString() {
			return "CropArea{x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "}";
		}
		
		public Rectangle toRectangle () {
			return new Rectangle(x, y, width, height);
		}
	}

	/**
	 * Calculates the crop area for an image based on the focal point and the
	 * target aspect ratio.
	 *
	 * @param imageWidth Width of the image in pixels
	 * @param imageHeight Height of the image in pixels
	 * @param focalX Focal point X coordinate (between 0.0 and 1.0)
	 * @param focalY Focal point Y coordinate (between 0.0 and 1.0)
	 * @param targetWidth Target aspect ratio width component
	 * @param targetHeight Target aspect ratio height component
	 * @return Calculated crop area
	 */
	public static CropArea calculateCrop(int imageWidth, int imageHeight,
			double focalX, double focalY,
			int targetWidth, int targetHeight) {

		double targetAspect = (double) targetWidth / targetHeight;
		double imageAspect = (double) imageWidth / imageHeight;

		int cropW, cropH;

		if (imageAspect > targetAspect) {
			// Bild ist breiter als Zielverhältnis – Breite beschneiden
			cropH = imageHeight;
			cropW = (int) Math.round(targetAspect * cropH);
		} else {
			// Bild ist höher als Zielverhältnis – Höhe beschneiden
			cropW = imageWidth;
			cropH = (int) Math.round(cropW / targetAspect);
		}

		// Focal Point in Pixelkoordinaten
		int focalPixelX = (int) Math.round(focalX * imageWidth);
		int focalPixelY = (int) Math.round(focalY * imageHeight);

		// Crop-Startposition um den Focal Point herum
		int cropX = focalPixelX - cropW / 2;
		int cropY = focalPixelY - cropH / 2;

		// Grenzen prüfen und anpassen
		cropX = Math.max(0, Math.min(cropX, imageWidth - cropW));
		cropY = Math.max(0, Math.min(cropY, imageHeight - cropH));

		return new CropArea(cropX, cropY, cropW, cropH);
	}

	// Test
	public static void main(String[] args) {
		CropArea crop = calculateCrop(1200, 800, 0.5, 0.5, 200, 300);
		System.out.println(crop);  // Beispielausgabe
	}
}
