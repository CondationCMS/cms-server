package com.github.thmarx.cms.extensions.http;

import com.github.thmarx.cms.server.jetty.JettyDefaultHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

/**
 *
 * @author t.marx
 */
public interface Request {
	public String getBody();

	public String getBody(final Charset charset);

	public List<String> getQueryParamter(final String name);
	/**
	 * Returns a list of query parameter names
	 * @return 
	 */
	public List<String> getQueryParamters();
}
