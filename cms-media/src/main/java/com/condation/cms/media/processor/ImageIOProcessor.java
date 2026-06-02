package com.condation.cms.media.processor;

/*-
 * #%L
 * CMS Media
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

import com.condation.cms.api.media.MediaFormat;
import com.condation.cms.api.media.MediaUtils;
import com.condation.cms.media.CropCalculator.CropArea;
import com.luciad.imageio.webp.WebPWriteParam;
import java.awt.image.BufferedImage;
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
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Fallback processor using Thumbnailator + Java ImageIO.
 * Always available — no external tools required.
 */
@Slf4j
public class ImageIOProcessor implements ImageProcessor {

    @Override
    public String name() {
        return "imageio";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void process(Path source, Path target, MediaFormat format, CropArea crop) throws IOException {
        Thumbnails.Builder<File> builder = Thumbnails
                .of(source.toFile())
                .size(format.width(), format.height());

        if (crop != null) {
            builder.sourceRegion(crop.toRectangle());
        }

        byte[] data = toFormat(builder.asBufferedImage(), format);
        Files.write(target, data);
    }

    private static byte[] toFormat(BufferedImage imageBuff, MediaFormat mediaFormat) throws IOException {
        if (mediaFormat.format() == null) {
            throw new IllegalArgumentException("unknown media format");
        }
        return switch (mediaFormat.format()) {
            case JPEG -> toJPG(imageBuff, !mediaFormat.compression());
            case WEBP -> toWEBP(imageBuff, !mediaFormat.compression());
            case PNG  -> toPNG(imageBuff, !mediaFormat.compression());
			case AVIF -> throw new UnsupportedOperationException("avif format not supported by imageio processor");
        };
    }

    private static byte[] toPNG(BufferedImage imageBuff, boolean uncompressed) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (uncompressed) {
                ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
                try {
                    ImageWriteParam writeParam = writer.getDefaultWriteParam();
                    if (writeParam.canWriteCompressed()) {
                        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        writeParam.setCompressionQuality(1f);
                    }
                    try (MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(buffer)) {
                        writer.setOutput(out);
                        writer.write(null, new IIOImage(imageBuff, null, null), writeParam);
                    }
                    return buffer.toByteArray();
                } finally {
                    writer.dispose();
                }
            } else {
                ImageIO.write(imageBuff, "png", buffer);
                return buffer.toByteArray();
            }
        }
    }

    private static byte[] toJPG(BufferedImage imageBuff, boolean uncompressed) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (uncompressed) {
                JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
                jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpegParams.setCompressionQuality(1f);
                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                try (MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(buffer)) {
                    writer.setOutput(out);
                    writer.write(null, new IIOImage(imageBuff, null, null), jpegParams);
                    return buffer.toByteArray();
                } finally {
                    writer.dispose();
                }
            } else {
                ImageIO.write(imageBuff, "jpg", buffer);
                return buffer.toByteArray();
            }
        }
    }

    private static byte[] toWEBP(BufferedImage imageBuff, boolean uncompressed) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (uncompressed) {
                WebPWriteParam writeParam = new WebPWriteParam(Locale.getDefault());
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);
                ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
                try (MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(buffer)) {
                    writer.setOutput(out);
                    writer.write(null, new IIOImage(imageBuff, null, null), writeParam);
                    return buffer.toByteArray();
                } finally {
                    writer.dispose();
                }
            } else {
                ImageIO.write(imageBuff, "webp", buffer);
                return buffer.toByteArray();
            }
        }
    }
}
