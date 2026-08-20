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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DistanceUnit;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LuceneGeoQueryTest {

	private static final double BOCHUM_LATITUDE = 51.4818;
	private static final double BOCHUM_LONGITUDE = 7.2162;

	@TempDir
	Path tempDirectory;

	@Test
	void findsGeoPointsConfiguredAtArbitraryNestedPaths() throws Exception {
		var fields = Map.<String, Object>of(
				"location.points", Map.of("type", "geo"),
				"location.coords", Map.of("type", "geo"),
				"address.location", Map.of(
						"type", "geo",
						"latitude", "lat",
						"longitude", "lon"));

		try (var metadata = new PersistentMetaData(tempDirectory, fields)) {
			metadata.open();
			metadata.addFile(
					"points.md",
					page(Map.of("location", Map.of("points", location(51.4820, 7.2160)))),
					LocalDate.now());
			metadata.addFile(
					"coords.md",
					page(Map.of("location", Map.of("coords", location(51.4815, 7.2170)))),
					LocalDate.now());
			metadata.addFile(
					"address.md",
					page(Map.of("address", Map.of(
							"location", Map.of("lat", 51.4817, "lon", 7.2155)))),
					LocalDate.now());
			metadata.addFile(
					"far-away.md",
					page(Map.of("location", Map.of("points", location(48.1372, 11.5756)))),
					LocalDate.now());

			Assertions.assertThat(within(metadata, "location.points", DistanceUnit.KILOMETERS))
					.extracting(ContentNode::uri)
					.containsExactly("points.md");
			Assertions.assertThat(within(metadata, "location.coords", DistanceUnit.KILOMETERS))
					.extracting(ContentNode::uri)
					.containsExactly("coords.md");
			Assertions.assertThat(within(metadata, "address.location", DistanceUnit.KILOMETERS))
					.extracting(ContentNode::uri)
					.containsExactly("address.md");
		}
	}

	@Test
	void acceptsDistanceUnitAsStringForTemplateQueries() throws Exception {
		var fields = Map.<String, Object>of(
				"address.location", Map.of("type", "geo"));

		try (var metadata = new PersistentMetaData(tempDirectory, fields)) {
			metadata.open();
			metadata.addFile(
					"near.md",
					page(Map.of("address", Map.of("location", location(51.4820, 7.2160)))),
					LocalDate.now());
			metadata.addFile(
					"far.md",
					page(Map.of("address", Map.of("location", location(48.1372, 11.5756)))),
					LocalDate.now());

			var result = metadata.query((node, excerpt) -> node)
					.within(
							"address.location",
							BOCHUM_LATITUDE,
							BOCHUM_LONGITUDE,
							5,
							"KM")
					.get();

			Assertions.assertThat(result)
					.extracting(ContentNode::uri)
					.containsExactly("near.md");
		}
	}

	@Test
	void acceptsMilesForTemplateQueries() throws Exception {
		var fields = Map.<String, Object>of(
				"address.location", Map.of("type", "geo"));

		try (var metadata = new PersistentMetaData(tempDirectory, fields)) {
			metadata.open();
			metadata.addFile(
					"inside-five-miles.md",
					page(Map.of("address", Map.of("location", location(51.5200, 7.2162)))),
					LocalDate.now());
			metadata.addFile(
					"outside-five-miles.md",
					page(Map.of("address", Map.of("location", location(51.5800, 7.2162)))),
					LocalDate.now());

			var result = metadata.query((node, excerpt) -> node)
					.within(
							"address.location",
							BOCHUM_LATITUDE,
							BOCHUM_LONGITUDE,
							5,
							"miles")
					.get();

			Assertions.assertThat(result)
					.extracting(ContentNode::uri)
					.containsExactly("inside-five-miles.md");
		}
	}

	private static java.util.List<ContentNode> within(
			PersistentMetaData metadata,
			String field,
			DistanceUnit unit) {
		return metadata.query((node, excerpt) -> node)
				.within(field, BOCHUM_LATITUDE, BOCHUM_LONGITUDE, 5, unit)
				.get();
	}

	private static Map<String, Object> location(double latitude, double longitude) {
		return Map.of("latitude", latitude, "longitude", longitude);
	}

	private static Map<String, Object> page(Map<String, Object> values) {
		var data = new HashMap<>(values);
		data.put(Constants.MetaFields.STATUS, "published");
		return data;
	}
}
