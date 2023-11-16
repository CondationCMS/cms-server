package com.github.thmarx.cms.template.functions.query;

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
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.filesystem.query.Query;
import com.github.thmarx.cms.template.functions.list.Node;
import com.github.thmarx.cms.utils.NodeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class QueryFunction {

	private final FileSystem fileSystem;

	public Query create() {
		return fileSystem.query(node -> {
			var name = NodeUtil.getName(node);
			var url = toUrl(node.uri());
			final Node navNode = new Node(name, url, "", node.data());

			return navNode;
		});
	}

	public Query create(final String startUri) {
		return fileSystem.query(startUri, node -> {
			var name = NodeUtil.getName(node);
			var url = toUrl(node.uri());
			final Node navNode = new Node(name, url, "", node.data());

			return navNode;
		});
	}

	protected String toUrl(String uri) {
		if (uri.endsWith("index.md")) {
			uri = uri.replace("index.md", "");
		}
		if (uri.endsWith(".md")) {
			uri = uri.substring(0, uri.lastIndexOf(".md"));
		}

		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		if (uri.length() > 1 && uri.endsWith("/") ) {
			uri = uri.substring(0, uri.length() - 1);
		}
		
		return uri;
	}
}
