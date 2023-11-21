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

import com.github.thmarx.cms.api.Media;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.media.MediaFormat;
import com.github.thmarx.cms.api.theme.Theme;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class MediaManager {

	private final Path assetBase;
	private final Path tempFolder;
	private final Theme theme;
	private final SiteProperties siteProperties;

	private Map<String, MediaFormat> mediaFormats;
	private Path tempDirectory;

	public Path resolve (String uri) {
		return assetBase.resolve(uri);
	}
	
	public boolean hasMediaFormat (String format) {
		return getMediaFormats().containsKey(format);
	}
	public MediaFormat getMediaFormat (String format) {
		return getMediaFormats().get(format);
	}
	
	private Path getTempDirectory() throws IOException {
		if (tempDirectory == null) {
			tempDirectory = tempFolder.resolve("media");
			if (!Files.exists(tempDirectory)) {
				Files.createDirectories(tempDirectory);
			}
		}
		return tempDirectory;
	}

	public Optional<byte[]> getScaledContent(final String mediaPath, final MediaFormat mediaFormat) throws IOException {

		Path resolve = assetBase.resolve(mediaPath);

		if (Files.exists(resolve)) {
			Optional<byte[]> tempContent = getTempContent(mediaPath, mediaFormat);
			if (tempContent.isPresent()) {
				return tempContent;
			}

			Thumbnails.Builder<File> scaleBuilder = Thumbnails
					.of(resolve.toFile())
					.size(mediaFormat.width(), mediaFormat.height())
					;
			
			if (mediaFormat.cropped()) {
				scaleBuilder.crop(Positions.CENTER);
			}
			
			byte[] data = Scale.toFormat(scaleBuilder.asBufferedImage(), mediaFormat);

			writeTempContent(mediaPath, mediaFormat, data);

			return Optional.of(data);
		}
		return Optional.empty();
	}

	public String getTempFilename(final String mediaPath, final MediaFormat mediaFormat) {
		var tempFilename = mediaPath.replace("/", "_").replace(".", "_");
		tempFilename += "-" + mediaFormat.name() + Media.fileending4Format(mediaFormat.format());

		return tempFilename;
	}

	private Path writeTempContent(final String mediaPath, final MediaFormat mediaFormat, byte[] content) throws IOException {
		var tempFilename = getTempFilename(mediaPath, mediaFormat);

		var tempFile = getTempDirectory().resolve(tempFilename);
		Files.deleteIfExists(tempFile);
		Files.write(tempFile, content);
		
		return tempFile;
	}

	private Optional<byte[]> getTempContent(final String mediaPath, final MediaFormat mediaFormat) throws IOException {
		var tempFilename = getTempFilename(mediaPath, mediaFormat);

		var tempFile = getTempDirectory().resolve(tempFilename);
		if (Files.exists(tempFile)) {
			return Optional.of(Files.readAllBytes(tempFile));
		}

		return Optional.empty();
	}
	
	private Map<String, MediaFormat> getMediaFormats() {

		if (mediaFormats == null) {
			mediaFormats = new HashMap<>();

			if (!theme.empty()) {
				mediaFormats.putAll(theme.properties().getMediaFormats());
			}
			mediaFormats.putAll(siteProperties.getMediaFormats());
		}

		return mediaFormats;
	}
}
