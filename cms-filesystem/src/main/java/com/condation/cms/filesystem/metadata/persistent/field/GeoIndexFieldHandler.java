package com.condation.cms.filesystem.metadata.persistent.field;

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

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LatLonDocValuesField;
import org.apache.lucene.document.LatLonPoint;

@Slf4j
public class GeoIndexFieldHandler implements IndexFieldHandler<GeoIndexFieldDefinition> {

	@Override
	public String type() {
		return GeoIndexFieldDefinition.FIELD_TYPE;
	}

	@Override
	public Class<GeoIndexFieldDefinition> definitionType() {
		return GeoIndexFieldDefinition.class;
	}

	@Override
	public void add(Document document, String field, Object value, GeoIndexFieldDefinition definition) {
		if (!(value instanceof Map<?, ?> location)) {
			log.warn("geo index field {} must contain a map", field);
			return;
		}

		var latitude = location.get(definition.latitude());
		var longitude = location.get(definition.longitude());
		if (!(latitude instanceof Number latitudeNumber)
				|| !(longitude instanceof Number longitudeNumber)) {
			log.warn(
					"geo index field {} must contain numeric {} and {} values",
					field,
					definition.latitude(),
					definition.longitude());
			return;
		}

		double latitudeValue = latitudeNumber.doubleValue();
		double longitudeValue = longitudeNumber.doubleValue();
		document.add(new LatLonPoint(field, latitudeValue, longitudeValue));
		document.add(new LatLonDocValuesField(field, latitudeValue, longitudeValue));
	}
}
