package com.github.thmarx.cms.server.jetty.handler;

/*-
 * #%L
 * cms-server
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
import com.github.thmarx.cms.Startup;
import com.github.thmarx.cms.api.Media;
import com.github.thmarx.cms.api.ThemeProperties;
import com.github.thmarx.cms.media.Scale;
import com.github.thmarx.cms.utils.HTTPUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyMediaHandler extends Handler.Abstract {

	private final Path assetBase;
	private final Map<String, ThemeProperties.MediaFormat> mediaFormats;

	private Path tempDirectory;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		try {
			final List<String> formatParameter = queryParameters.getOrDefault("format", List.of("##original##"));
			final String formatValue = formatParameter.getFirst();

			if ("##original##".equalsIgnoreCase(formatValue)) {
				var mediaPath = getRelativeMediaPath(request);
				Path assetPath = assetBase.resolve(mediaPath);
				if (Files.exists(assetPath)) {
					var bytes = Files.readAllBytes(assetPath);
					var mimetype = Files.probeContentType(assetPath);

					deliver(bytes, mimetype, response);
					
					callback.succeeded();
					return true;
				}
			} else {
				var format = mediaFormats.get(formatValue);
				if (format == null) {
					log.error("unknown format {}", formatParameter.getFirst());
					response.setStatus(404);
					callback.succeeded();
					return true;
				}

				var mediaPath = getRelativeMediaPath(request);

				var result = getScaledContent(mediaPath, format);
				if (result.isPresent()) {

					deliver(result.get(), Media.mime4Format(format.format()), response);
					
					callback.succeeded();
					return true;
				}
			}

		} catch (Exception e) {
			log.error(null, e);
			callback.failed(e);
		}
		response.setStatus(404);
		return true;
	}

	private void deliver(final byte[] bytes, final String mimetype, Response response) throws IOException {
		response.getHeaders().add("Content-Type", mimetype);
		response.getHeaders().add("Content-Length", bytes.length);
		if (!Startup.DEV_MODE) {
			response.getHeaders().add("Access-Control-Max-Age", Duration.ofDays(10).toSeconds());
			response.getHeaders().add("Cache-Control", "max-age=" + Duration.ofDays(10).toSeconds());
		}		
		response.setStatus(200);

		Content.Sink.write(response, true, ByteBuffer.wrap(bytes));
	}

	private Path getTempDirectory() throws IOException {
		if (tempDirectory == null) {
			tempDirectory = Files.createTempDirectory("cms-media-temp");
		}
		return tempDirectory;
	}

	private Optional<byte[]> getScaledContent(final String mediaPath, final ThemeProperties.MediaFormat mediaFormat) throws IOException {

		Path resolve = assetBase.resolve(mediaPath);

		if (Files.exists(resolve)) {
			Optional<byte[]> tempContent = getTempContent(mediaPath, mediaFormat);
			if (tempContent.isPresent()) {
				return tempContent;
			}
			byte[] bytes = Files.readAllBytes(resolve);

			var result = Scale.scaleWithAspectIfTooLarge(bytes, mediaFormat.width(), mediaFormat.height(), mediaFormat.compression(), mediaFormat.format());

			writeTempContent(mediaPath, mediaFormat, result.data);

			return Optional.of(result.data);
		}
		return Optional.empty();
	}

	public String getTempFilename(final String mediaPath, final ThemeProperties.MediaFormat mediaFormat) {
		var tempFilename = mediaPath.replace("/", "_").replace(".", "_");
		tempFilename += "-" + mediaFormat.name() + Media.fileending4Format(mediaFormat.format());

		return tempFilename;
	}

	private void writeTempContent(final String mediaPath, final ThemeProperties.MediaFormat mediaFormat, byte[] content) throws IOException {
		var tempFilename = getTempFilename(mediaPath, mediaFormat);

		var tempFile = getTempDirectory().resolve(tempFilename);
		Files.deleteIfExists(tempFile);
		Files.write(tempFile, content);
	}

	private Optional<byte[]> getTempContent(final String mediaPath, final ThemeProperties.MediaFormat mediaFormat) throws IOException {
		var tempFilename = getTempFilename(mediaPath, mediaFormat);

		var tempFile = getTempDirectory().resolve(tempFilename);
		if (Files.exists(tempFile)) {
			return Optional.of(Files.readAllBytes(tempFile));
		}

		return Optional.empty();
	}

	private String getRelativeMediaPath(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = request.getContext().getContextPath();
		if (!contextPath.endsWith("/")) {
			contextPath += "/";
		}
		var replacePath = contextPath + "media/";
		path = path.replace(replacePath, "");
		return path;
	}
}
