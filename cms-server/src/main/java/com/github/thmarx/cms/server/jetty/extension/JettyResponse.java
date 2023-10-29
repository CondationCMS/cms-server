package com.github.thmarx.cms.server.jetty.extension;

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
