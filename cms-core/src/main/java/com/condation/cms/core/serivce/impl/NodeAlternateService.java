package com.condation.cms.core.serivce.impl;

/*-
 * #%L
 * CMS Core
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.repository.MutableContentRepository;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.serivce.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/** Maintains outgoing alternate links in a site's content repository. */
@Slf4j
public class NodeAlternateService implements Service {

	private final MutableContentRepository contentRepository;

	public NodeAlternateService(MutableContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public boolean removeAlternate(String uri, String targetSite) {
		var node = contentRepository.findByUrl(uri).or(() -> contentRepository.get(uri));
		if (node.isEmpty()) {
			return false;
		}

		try {
			var document = contentRepository.load(node.get()).orElseThrow();
			Map<String, Object> meta = new HashMap<>(node.get().data());
			var alternates = alternates(meta);
			alternates.remove(targetSite);
			if (alternates.isEmpty()) {
				meta.remove(Constants.MetaFields.ALTERNATES);
			} else {
				meta.put(Constants.MetaFields.ALTERNATES, alternates);
			}
			contentRepository.save(node.get().path(), meta, document.content());
			return true;
		} catch (IOException ex) {
			log.error("could not remove alternate from {}", uri, ex);
			return false;
		}
	}

	public boolean addAlternate(String uri, String targetSite, String targetUri) {
		var node = contentRepository.findByUrl(uri).or(() -> contentRepository.get(uri));
		if (node.isEmpty()) {
			return false;
		}

		try {
			var document = contentRepository.load(node.get()).orElseThrow();
			Map<String, Object> meta = new HashMap<>(node.get().data());
			var alternates = alternates(meta);
			alternates.put(targetSite, PathUtil.toURL(targetUri));
			meta.put(Constants.MetaFields.ALTERNATES, alternates);
			contentRepository.save(node.get().path(), meta, document.content());
			return true;
		} catch (IOException ex) {
			log.error("could not add alternate to {}", uri, ex);
			return false;
		}
	}

	private Map<String, Object> alternates(Map<String, Object> meta) {
		var value = meta.get(Constants.MetaFields.ALTERNATES);
		if (!(value instanceof Map<?, ?> map)) {
			return new HashMap<>();
		}
		Map<String, Object> result = new HashMap<>();
		map.forEach((key, entry) -> result.put(String.valueOf(key), entry));
		return result;
	}
}
