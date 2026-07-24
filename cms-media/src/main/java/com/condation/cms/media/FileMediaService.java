package com.condation.cms.media;

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
import com.condation.cms.api.media.Media;
import com.condation.cms.api.media.MediaService;
import com.condation.cms.api.media.meta.Meta;
import com.condation.cms.api.utils.PathUtil;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class FileMediaService implements MediaService {

	private final Path assetBase;

	public boolean hasMetaData(final String media) {
		var metaFile = getMetaFile(media);
		return metaFile.isPresent() ? Files.exists(metaFile.get()) : false;
	}

	private Optional<Path> getMetaFile(final String media) {
		String mediaPath = media;
		if (mediaPath.startsWith("/")) {
			mediaPath = mediaPath.substring(1);
		}
		var mediaFile = assetBase.resolve(mediaPath);

		if (!PathUtil.isChild(assetBase, mediaFile)) {
			return Optional.empty();
		}
		var metaFileName = mediaFile.getFileName().toString() + ".meta.yaml";
		var metaFile = mediaFile.getParent().resolve(metaFileName);

		return Optional.of(metaFile);
	}

	private Meta loadMeta(final String media) {

		var metaFile = getMetaFile(media);
		if (metaFile.isPresent() && Files.exists(metaFile.get())) {
			try {
				var content = Files.readString(metaFile.get(), StandardCharsets.UTF_8);
				return new Yaml().loadAs(content, Meta.class);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}

		return new Meta();
	}

	@Override
	public Media get(final String media) {
		if (Strings.isNullOrEmpty(media)) {
			var meta = new Meta();
			return new Media("", meta, false);
		}
		String mediaPath = media;
		if (mediaPath.startsWith("/")) {
			mediaPath = mediaPath.substring(1);
		}
		var mediaFile = assetBase.resolve(mediaPath);
		if (!PathUtil.isChild(assetBase, mediaFile)) {
			return new Media(mediaPath, new Meta(), false);
		}
		var meta = loadMeta(media);

		var size = ImageSize.getSize(mediaFile);

		return new Media(mediaPath, meta, Files.exists(mediaFile), size);
	}
}
