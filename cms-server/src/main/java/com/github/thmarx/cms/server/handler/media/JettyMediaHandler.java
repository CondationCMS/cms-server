package com.github.thmarx.cms.server.handler.media;

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
import com.github.thmarx.cms.api.ServerContext;
import com.github.thmarx.cms.api.media.MediaUtils;
import com.github.thmarx.cms.media.MediaManager;
import com.github.thmarx.cms.api.utils.HTTPUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
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

	@Getter
	private final MediaManager mediaManager;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		try {
			final List<String> formatParameter = queryParameters.getOrDefault("format", List.of("##original##"));
			final String formatValue = formatParameter.getFirst();

			if ("##original##".equalsIgnoreCase(formatValue)) {
				var mediaPath = getRelativeMediaPath(request);
				Path assetPath = mediaManager.resolve(mediaPath);
				if (Files.exists(assetPath)) {
					var bytes = Files.readAllBytes(assetPath);
					var mimetype = Files.probeContentType(assetPath);

					deliver(bytes, mimetype, response);
					
					callback.succeeded();
					return true;
				}
			} else {
				if (!mediaManager.hasMediaFormat(formatValue)) {
					log.error("unknown format {}", formatParameter.getFirst());
					response.setStatus(404);
					callback.succeeded();
					return true;
				}
				var format = mediaManager.getMediaFormat(formatValue);

				var mediaPath = getRelativeMediaPath(request);

				var result = mediaManager.getScaledContent(mediaPath, format);
				if (result.isPresent()) {

					deliver(result.get(), MediaUtils.mime4Format(format.format()), response);
					
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
		if (!ServerContext.IS_DEV) {
			response.getHeaders().add("Access-Control-Max-Age", Duration.ofDays(10).toSeconds());
			response.getHeaders().add("Cache-Control", "max-age=" + Duration.ofDays(10).toSeconds());
		}		
		response.setStatus(200);

		Content.Sink.write(response, true, ByteBuffer.wrap(bytes));
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
