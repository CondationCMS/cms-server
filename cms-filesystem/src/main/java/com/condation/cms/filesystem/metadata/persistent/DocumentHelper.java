package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import com.condation.cms.filesystem.metadata.persistent.utils.FlattenMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author t.marx
 */
public class DocumentHelper {

	public static void addAvailableFields (Document document) {
		List<String> fieldNames = new ArrayList<>();
		document.getFields().forEach(field -> fieldNames.add(field.name()));

		fieldNames.forEach(fieldName -> {
			document.add(new StringField("_fields", new BytesRef(fieldName), Store.NO));
		});
	}

	public static void addData(final Document document, Map<String, Object> data) {
		var flatten = FlattenMap.flattenMap(data);

		flatten.entrySet().forEach(entry -> {

			switch (entry.getValue()) {
				case List listValue ->
					handleList(document, entry.getKey(), listValue);
				default -> {
					addValue(document, entry.getKey(), entry.getValue());
				}
			}
		});
	}

	private static void handleList(Document document, String name, List<?> list) {
		list.forEach(item -> addValue(document, name, item));
	}

	private static void addValue(Document document, String name, Object value) {
		switch (value) {
			case String stringValue -> {
				document.add(new StringField(name, stringValue, Field.Store.NO));
//				document.add(new SortedSetDocValuesField(name, new BytesRef(stringValue)));
			}
			case Integer intValue -> {
				document.add(new IntField(name, intValue, Field.Store.NO));
//				document.add(new SortedNumericDocValuesField(name, intValue));
			}
			case Long longValue -> {
				document.add(new LongField(name, longValue, Field.Store.NO));
//				document.add(new SortedNumericDocValuesField(name, longValue));
			}
			case Float floatValue -> {
				document.add(new FloatField(name, floatValue, Field.Store.NO));
//				document.add(new SortedNumericDocValuesField(name, NumericUtils.floatToSortableInt(floatValue)));
			}
			case Double doubleValue -> {
				document.add(new DoubleField(name, doubleValue, Field.Store.NO));
//				document.add(new SortedNumericDocValuesField(name, NumericUtils.doubleToSortableLong(doubleValue)));
			}
			case Boolean booleanValue -> {
				var intValue = booleanValue ? 1 : 0;
				document.add(
						new IntField(
								name,
								intValue,
								Field.Store.NO
						)
				);
//				document.add(new SortedNumericDocValuesField(name, intValue));
			}
			case List<?> listValue ->
				handleList(document, name, listValue);
			case Date dateValue -> {
				document.add(new LongField(name, dateValue.getTime(), Field.Store.NO));
			}
			default -> {
			}
		}
	}
}
