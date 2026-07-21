package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * CMS FileSystem
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

import com.condation.cms.api.db.ContentNode;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

final class UrlIndex {

	private static final String KEY_SEPARATOR = "\u0000";
	private static final String SEQUENCE_KEY = "sequence";

	private final ConcurrentMap<String, String> winners;
	private final MVMap<String, String> candidates;
	private final MVMap<String, String> candidateByPath;
	private final MVMap<String, Long> state;

	UrlIndex(MVStore store, ConcurrentMap<String, String> winners) {
		this.winners = winners;
		candidates = store.openMap("urlCandidates");
		candidateByPath = store.openMap("urlCandidateByPath");
		state = store.openMap("urlIndexState");
	}

	void put(ContentNode node) {
		remove(node.path());

		var sequence = state.getOrDefault(SEQUENCE_KEY, 0L) + 1;
		state.put(SEQUENCE_KEY, sequence);

		var candidateKey = candidateKey(node.url(), sequence, node.path());
		candidates.put(candidateKey, node.path());
		candidateByPath.put(node.path(), candidateKey);
		winners.put(node.url(), node.path());
	}

	void remove(String path) {
		var candidateKey = candidateByPath.remove(path);
		if (candidateKey == null) {
			return;
		}

		var url = urlFromCandidateKey(candidateKey);
		candidates.remove(candidateKey);
		if (winners.remove(url, path)) {
			findLatestCandidate(url).ifPresentOrElse(
					winner -> winners.put(url, winner),
					() -> winners.remove(url)
			);
		}
	}

	void clear() {
		winners.clear();
		candidates.clear();
		candidateByPath.clear();
		state.clear();
	}

	private Optional<String> findLatestCandidate(String url) {
		var prefix = url + KEY_SEPARATOR;
		var cursor = candidates.cursor(prefix + Character.MAX_VALUE, null, true);
		while (cursor.hasNext()) {
			var key = cursor.next();
			if (!key.startsWith(prefix)) {
				break;
			}
			return Optional.ofNullable(cursor.getValue());
		}
		return Optional.empty();
	}

	private static String candidateKey(String url, long sequence, String path) {
		return url + KEY_SEPARATOR + "%016x".formatted(sequence) + KEY_SEPARATOR + path;
	}

	private static String urlFromCandidateKey(String candidateKey) {
		return candidateKey.substring(0, candidateKey.indexOf(KEY_SEPARATOR));
	}
}
