package com.github.thmarx.cms.media;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.github.thmarx.cms.api.Media;
import com.luciad.imageio.webp.WebPWriteParam;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author ThorstenMarx
 */
@Log4j2
public class Scale {

	public static ScaleResult scaleWithAspectIfTooLarge(byte[] fileData, int maxWidth,
			int maxHeight, boolean uncompressed, final Media.Format format) {
		ScaleResult result = new ScaleResult();

		try (ByteArrayInputStream in = new ByteArrayInputStream(fileData)) {
			BufferedImage img = ImageIO.read(in);
			int origWidth = img.getWidth();
			int origHeight = img.getHeight();
			result = calcSizes(origWidth, origHeight, maxWidth, maxHeight);
			boolean dontScale = !((origWidth > maxWidth) || (origHeight > maxHeight));
			if (dontScale) {
				result.data = fileData;
			} else {
				// resize
				Image scaledImage = img.getScaledInstance(result.width,
						result.height,
						Image.SCALE_SMOOTH);
				BufferedImage imageBuff = new BufferedImage(result.width,
						result.height,
						BufferedImage.TYPE_INT_RGB);
				imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);
				// output
				if (Media.Format.JPEG.equals(format)) {
					result.data = toJPG(imageBuff, uncompressed);
				} else if (Media.Format.WEBP.equals(format)) {
					result.data = toWEBP(imageBuff, uncompressed);
				} else if (Media.Format.PNG.equals(format)) {
					result.data = toPNG(imageBuff, uncompressed);
				}
			}
		} catch (IOException e) {
			log.error("scaleWithAspectIfTooLarge(): IOexception ", e);
		}
		return result;
	}

	private static byte[] toPNG(final BufferedImage imageBuff, final boolean uncompressed) throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			if (uncompressed) {
				final ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
				writer.setOutput(new MemoryCacheImageOutputStream(buffer));

				var writeParam = writer.getDefaultWriteParam();
				if (writeParam.canWriteCompressed()) {
					writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					writeParam.setCompressionQuality(1f);
				}

				writer.write(null, new IIOImage(imageBuff, null, null), writeParam);
				return buffer.toByteArray();
			} else {
				ImageIO.write(imageBuff, "png", buffer);
				return buffer.toByteArray();
			}
		}
	}

	private static byte[] toJPG(final BufferedImage imageBuff, final boolean uncompressed) throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			if (uncompressed) {
				JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
				jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegParams.setCompressionQuality(1f);
				final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				writer.setOutput(new MemoryCacheImageOutputStream(buffer));
				writer.write(null, new IIOImage(imageBuff, null, null), jpegParams);
				return buffer.toByteArray();
			} else {
				ImageIO.write(imageBuff, "jpg", buffer);
				return buffer.toByteArray();
			}
		}
	}

	private static byte[] toWEBP(final BufferedImage imageBuff, final boolean uncompressed) throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			if (uncompressed) {
				WebPWriteParam writeParam = new WebPWriteParam(Locale.getDefault());
				writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);
				final ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
				final MemoryCacheImageOutputStream memoryCacheImageOutputStream = new MemoryCacheImageOutputStream(buffer);
				writer.setOutput(memoryCacheImageOutputStream);
				writer.write(null, new IIOImage(imageBuff, null, null), writeParam);
				memoryCacheImageOutputStream.close();
				return buffer.toByteArray();
			} else {
				ImageIO.write(imageBuff, "webp", buffer);
				return buffer.toByteArray();
			}
		}

	}

	public static ScaleResult calcSizes(int origWidth, int origHeight, int maxWidth,
			int maxHeight) {
		ScaleResult result = new ScaleResult();
		int useWidth = origWidth;
		int useHeight = origHeight;
		if ((maxWidth < 1 && maxHeight < 1)) {
			// keep original
		} else if (maxWidth > origWidth && maxHeight > origHeight) {
			// keep original
		} else {
			if (maxWidth > 0 && useWidth > maxWidth) {
				useWidth = maxWidth;
				useHeight = maxWidth * origHeight / origWidth;
			}
			if (maxHeight > 0 && useHeight > maxHeight) {
				useHeight = maxHeight;
				useWidth = (maxHeight * origWidth) / origHeight;
			}
		}
		result.width = useWidth;
		result.height = useHeight;
		return result;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ScaleResult {

		public byte[] data;
		public int width;
		public int height;
	}

	public static void main(final String... args) throws IOException {
//		scale("/tmp/test.png");
//		scale("/tmp/test.jpg");

		scale("../../testdata/image.jpg");
	}

	public static void scale(final String file) throws IOException {
		byte[] fileData = Files.readAllBytes(Path.of(new File(file).toURI()));
		ScaleResult result = scaleWithAspectIfTooLarge(fileData, 200, 300, false, Media.Format.JPEG);
		Files.write(Path.of(file + ".jpg"), result.data);

		//result = scaleWithAspectIfTooLarge(fileData, 200, 300, true, Format.WEBP);
		//Files.write(Path.of(file + ".webp"), result.data);
		result = scaleWithAspectIfTooLarge(fileData, 200, 300, true, Media.Format.PNG);
		Files.write(Path.of(file + ".png"), result.data);
	}
}
