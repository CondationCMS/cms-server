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
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.MediaConfiguration;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.eventbus.events.InvalidateMediaCache;
import com.condation.cms.api.media.MediaFormat;
import com.condation.cms.api.media.MediaUtils;
import com.condation.cms.api.media.meta.Meta;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.api.utils.PathUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public abstract class MediaManager implements EventListener<ConfigurationReloadEvent> {

	protected List<Path> assetBase;
	protected Path tempFolder;
	protected Theme theme;
	protected Configuration configuration;

	protected Map<String, MediaFormat> mediaFormats;
	protected Path tempDirectory;

	protected MediaManager(List<Path> assetPath, Path tempFolder, Theme theme, Configuration configuration) {
		this.assetBase = assetPath;
		this.tempFolder = tempFolder;
		this.theme = theme;
		this.configuration = configuration;
	}

	public abstract void reloadTheme(Theme updateTheme);

	public Optional<Path> resolve(String uri) {
		for (Path assets : assetBase) {
			var resolved = assets.resolve(uri);
			if (Files.exists(resolved)) {
				return Optional.of(resolved);
			}
		}
		return Optional.empty();
	}

	public boolean hasMediaFormat(String format) {
		return getMediaFormats().containsKey(format);
	}

	public MediaFormat getMediaFormat(String format) {
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

	public void clearTempDirectory () {
		try {
			FileUtils.deleteDirectoryContents(getTempDirectory());
		} catch (IOException e) {
			log.error("error clearing media tempfolder", e);
		}
	}
	
	public void deleteTempFile(final Path mediaPath) {
		var baseDir = assetBase.stream().filter((base) -> PathUtil.isChild(base, mediaPath)).findFirst();

		if (baseDir.isEmpty()) {
			log.warn("could not find asset base director for {}", mediaPath);
			return;
		}

		mediaFormats.values().forEach(mediaFormat -> {
			try {
				var mediaUri = PathUtil.toRelativeFile(mediaPath, baseDir.get());
				var tempFilename = getTempFilename(mediaUri, mediaFormat);
				var tempFile = getTempDirectory().resolve(tempFilename);
				Files.deleteIfExists(tempFile);
			} catch (IOException ex) {
				log.error("error deleting file {} for format {}", mediaPath, mediaFormat, ex);
			}
		});
	}

	public Optional<byte[]> getScaledContent(final String mediaPath, final MediaFormat mediaFormat) throws IOException {

		Optional<Path> resolve = resolve(mediaPath);

		if (resolve.isPresent()) {
			Optional<byte[]> tempContent = getTempContent(mediaPath, mediaFormat);
			if (tempContent.isPresent()) {
				return tempContent;
			}

			Thumbnails.Builder<File> scaleBuilder = Thumbnails
					.of(resolve.get().toFile())
					.size(mediaFormat.width(), mediaFormat.height());

			if (mediaFormat.cropped()) {
				setupImageBuilder(scaleBuilder, resolve.get(), mediaFormat);
			}

			byte[] data = Scale.toFormat(scaleBuilder.asBufferedImage(), mediaFormat);

			writeTempContent(mediaPath, mediaFormat, data);

			return Optional.of(data);
		}
		return Optional.empty();
	}

	private void setupImageBuilder(Thumbnails.Builder<File> builder, Path media, MediaFormat format) {
		var metaFileName = media.getFileName().toString() + ".meta.yaml";
		var metaFile = media.getParent().resolve(metaFileName);
		var size = ImageSize.getSize(media);
		double focal_x = 0.5;
		double focal_y = 0.5;
		if (Files.exists(metaFile)) {
			try {
				final Meta meta = new Yaml().loadAs(Files.readString(metaFile, StandardCharsets.UTF_8), Meta.class);
				focal_x = meta.getFocalPoint_x();
				focal_y = meta.getFocalPoint_y();
			} catch (IOException ex) {
				log.warn("Could not read meta file: {}", metaFile, ex);
			}
		}
		CropCalculator.CropArea crop = CropCalculator.calculateCrop(
				size.width(), size.height(),
				focal_x, focal_y,
				format.width(), format.height());
		builder.sourceRegion(crop.toRectangle());
	}

	public String getTempFilename(final String mediaPath, final MediaFormat mediaFormat) {
		var tempFilename = mediaPath.replace("/", "_").replace(".", "_");
		tempFilename += "-" + mediaFormat.name() + MediaUtils.fileending4Format(mediaFormat.format());

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
			Map<String, MediaFormat> tempFormats = new HashMap<>();

			configuration.get(MediaConfiguration.class).getFormats().forEach(format -> {
				tempFormats.put(format.name(), format);
			});
			mediaFormats = tempFormats;
		}

		return mediaFormats;
	}

	@Override
	public void consum(ConfigurationReloadEvent event) {
		if (!"media".equals(event.name())) {
			return;
		}
		this.mediaFormats = null;
		getMediaFormats();
	}
}
