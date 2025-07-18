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

import com.condation.cms.api.media.Media;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public class ImageSize {

	public static com.condation.cms.api.media.Media.Size getSize(Path image) {
		if (!Files.exists(image)) {
			return Media.NO_SIZE;
		}

		try (ImageInputStream in = ImageIO.createImageInputStream(image.toFile())) {
			if (in == null) {
				log.error("can not load image: {}", image.toString());
				return Media.NO_SIZE;
			}

			// passenden Reader für das Format finden
			Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (!readers.hasNext()) {
				log.error("no reader found for image: {}", image.toString());
				return Media.NO_SIZE;
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(in);
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);

				return new Media.Size(width, height);
			} finally {
				reader.dispose();
			}
		} catch (IOException ex) {
			log.error("error resolving image size", ex);
		}
		return Media.NO_SIZE;
	}
}
