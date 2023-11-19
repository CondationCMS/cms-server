package com.github.thmarx.cms.server.jetty.extension;

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

import com.github.thmarx.cms.api.extensions.http.Response;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class JettyResponse implements Response {
	
	private final org.eclipse.jetty.server.Response original;
	private final Callback callback;

	@Override
	public void addHeader(String name, String value) {
		original.getHeaders().add(name, value);
	}

	@Override
	public void write(String content, Charset charset) {
		original.write(true, ByteBuffer.wrap(content.getBytes(charset)), callback);
	}
}
