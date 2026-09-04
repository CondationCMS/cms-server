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

import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.filesystem.metadata.persistent.lucene.TitleAnalyzer;
import com.condation.cms.filesystem.metadata.persistent.lucene.TitlePrefixAnalyzer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSelector;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSelector;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author t.marx
 */
@Slf4j
public class LuceneIndex implements AutoCloseable {

	private static final int CURSOR_VERSION = 1;

	@FunctionalInterface
	interface UriVisitor {
		boolean visit(String uri) throws IOException;
	}

	record CursorResult(List<String> uris, String nextCursor) {
	}

	record SeekPageResult(long totalItems, List<String> uris) {
	}

	
	public static final Analyzer INDEX_ANALYZER = new TitlePrefixAnalyzer();
	public static final Analyzer SEARCH_ANALYZER = new TitleAnalyzer();
	
	private Directory directory;
	private IndexWriter writer = null;

	private SearcherManager nrt_manager;
	private NRTCachingDirectory nrt_index;

	private boolean batchMode = false;
	
	public void setBatchMode (boolean mode) {
		batchMode = mode;
	}
	
	@Override
	public void close() throws Exception {
		if (nrt_manager != null) {
			nrt_manager.close();

			writer.commit();
			writer.close();
			directory.close();
		}
	}

	public void commit() throws IOException {
		writer.flush();
		writer.commit();
		nrt_manager.maybeRefresh();
	}

	void add(Document document) throws IOException {
		writer.addDocument(document);
		if (!batchMode) {
			commit();
		}
	}

	void update(Term term, Document document) throws IOException {
		writer.updateDocument(term, document);
		if (!batchMode) {
			commit();
		}
	}

	void delete(Query query) throws IOException {
		writer.deleteDocuments(query);
		if (!batchMode) {
			commit();
		}
	}

	void scanUris(Query query, Sort sort, int batchSize, UriVisitor visitor) throws IOException {
		if (batchSize < 1) {
			throw new IllegalArgumentException("batchSize must be greater than zero");
		}

		IndexSearcher searcher = nrt_manager.acquire();
		try {
			var storedFields = searcher.storedFields();
			org.apache.lucene.search.ScoreDoc after = null;

			while (true) {
				var hits = sort == null
						? searcher.searchAfter(after, query, batchSize)
						: searcher.searchAfter(after, query, batchSize, sort);
				if (hits.scoreDocs.length == 0) {
					return;
				}

				for (var hit : hits.scoreDocs) {
					var document = storedFields.document(hit.doc, Set.of("_uri"));
					if (!visitor.visit(document.get("_uri"))) {
						return;
					}
				}
				after = hits.scoreDocs[hits.scoreDocs.length - 1];
			}
		} finally {
			nrt_manager.release(searcher);
		}
	}

	CursorResult cursorPage(
			Query query,
			Sort sort,
			int pageSize,
			String cursor,
			String cursorKey) throws IOException {
		if (pageSize < 1 || pageSize > 10_000) {
			throw new IllegalArgumentException("cursor page size must be between 1 and 10000");
		}

		IndexSearcher searcher = nrt_manager.acquire();
		try {
			long generation = ((DirectoryReader) searcher.getIndexReader()).getVersion();
			var queryKey = cursorKey;
			var sortKey = sort == null ? "" : sort.toString();
			ScoreDoc after = decodeCursor(cursor, generation, queryKey, sortKey, sort);
			var hits = sort == null
					? searcher.searchAfter(after, query, pageSize + 1)
					: searcher.searchAfter(after, query, pageSize + 1, sort);
			var storedFields = searcher.storedFields();
			var uris = new ArrayList<String>(Math.min(pageSize, hits.scoreDocs.length));
			int returned = Math.min(pageSize, hits.scoreDocs.length);
			for (int index = 0; index < returned; index++) {
				uris.add(storedFields.document(hits.scoreDocs[index].doc, Set.of("_uri")).get("_uri"));
			}
			var nextCursor = hits.scoreDocs.length > pageSize
					? encodeCursor(hits.scoreDocs[returned - 1], generation, queryKey, sortKey)
					: null;
			return new CursorResult(List.copyOf(uris), nextCursor);
		} finally {
			nrt_manager.release(searcher);
		}
	}

	SeekPageResult seekPage(
			Query query,
			Sort sort,
			long offset,
			int pageSize) throws IOException {
		if (offset < 0) {
			throw new IllegalArgumentException("offset must not be negative");
		}
		if (pageSize < 1) {
			throw new IllegalArgumentException("pageSize must be greater than zero");
		}

		IndexSearcher searcher = nrt_manager.acquire();
		try {
			long totalItems = searcher.count(query);
			if (offset >= totalItems) {
				return new SeekPageResult(totalItems, List.of());
			}

			ScoreDoc after = seekToOffset(searcher, query, sort, offset);
			var hits = sort == null
					? searcher.searchAfter(after, query, pageSize)
					: searcher.searchAfter(after, query, pageSize, sort);
			var storedFields = searcher.storedFields();
			var uris = new ArrayList<String>(hits.scoreDocs.length);
			for (var hit : hits.scoreDocs) {
				uris.add(storedFields.document(hit.doc, Set.of("_uri")).get("_uri"));
			}
			return new SeekPageResult(totalItems, List.copyOf(uris));
		} finally {
			nrt_manager.release(searcher);
		}
	}

	private ScoreDoc seekToOffset(
			IndexSearcher searcher,
			Query query,
			Sort sort,
			long offset) throws IOException {
		ScoreDoc after = null;
		long remaining = offset;
		while (remaining > 0) {
			int stepSize = (int) Math.min(remaining, 1024);
			var hits = sort == null
					? searcher.searchAfter(after, query, stepSize)
					: searcher.searchAfter(after, query, stepSize, sort);
			if (hits.scoreDocs.length == 0) {
				return after;
			}
			after = hits.scoreDocs[hits.scoreDocs.length - 1];
			remaining -= hits.scoreDocs.length;
		}
		return after;
	}

	private String encodeCursor(ScoreDoc scoreDoc, long generation, String queryKey, String sortKey) {
		try {
			var bytes = new ByteArrayOutputStream();
			try (var output = new DataOutputStream(bytes)) {
				output.writeInt(CURSOR_VERSION);
				output.writeLong(generation);
				output.writeUTF(queryKey);
				output.writeUTF(sortKey);
				output.writeInt(scoreDoc.doc);
				output.writeFloat(scoreDoc.score);
				if (scoreDoc instanceof FieldDoc fieldDoc) {
					output.writeInt(fieldDoc.fields.length);
					for (var field : fieldDoc.fields) {
						writeCursorValue(output, field);
					}
				} else {
					output.writeInt(-1);
				}
			}
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());
		} catch (IOException ex) {
			throw new IllegalStateException("could not encode cursor", ex);
		}
	}

	private ScoreDoc decodeCursor(
			String cursor,
			long generation,
			String queryKey,
			String sortKey,
			Sort sort) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}
		try {
			var bytes = Base64.getUrlDecoder().decode(cursor);
			try (var input = new DataInputStream(new ByteArrayInputStream(bytes))) {
				if (input.readInt() != CURSOR_VERSION
						|| input.readLong() != generation
						|| !input.readUTF().equals(queryKey)
						|| !input.readUTF().equals(sortKey)) {
					throw new IllegalArgumentException("cursor has expired or belongs to another query");
				}
				int doc = input.readInt();
				float score = input.readFloat();
				int fieldCount = input.readInt();
				if (fieldCount < 0) {
					if (sort != null) {
						throw new IllegalArgumentException("cursor sort does not match query sort");
					}
					return new ScoreDoc(doc, score);
				}
				if (sort == null || fieldCount != sort.getSort().length) {
					throw new IllegalArgumentException("cursor sort does not match query sort");
				}
				var fields = new Object[fieldCount];
				for (int index = 0; index < fieldCount; index++) {
					fields[index] = readCursorValue(input);
				}
				return new FieldDoc(doc, score, fields);
			}
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (IOException ex) {
			throw new IllegalArgumentException("invalid cursor", ex);
		}
	}

	private void writeCursorValue(DataOutputStream output, Object value) throws IOException {
		switch (value) {
			case null -> output.writeByte(0);
			case BytesRef bytes -> {
				output.writeByte(1);
				output.writeInt(bytes.length);
				output.write(bytes.bytes, bytes.offset, bytes.length);
			}
			case Long longValue -> {
				output.writeByte(2);
				output.writeLong(longValue);
			}
			case Integer intValue -> {
				output.writeByte(3);
				output.writeInt(intValue);
			}
			case Double doubleValue -> {
				output.writeByte(4);
				output.writeDouble(doubleValue);
			}
			case Float floatValue -> {
				output.writeByte(5);
				output.writeFloat(floatValue);
			}
			case String stringValue -> {
				output.writeByte(6);
				output.writeUTF(stringValue);
			}
			default -> throw new IllegalArgumentException(
					"unsupported cursor sort value: " + value.getClass().getName());
		}
	}

	private Object readCursorValue(DataInputStream input) throws IOException {
		return switch (input.readByte()) {
			case 0 -> null;
			case 1 -> {
				int length = input.readInt();
				if (length < 0 || length > 1_000_000) {
					throw new IllegalArgumentException("invalid cursor value length");
				}
				yield new BytesRef(input.readNBytes(length));
			}
			case 2 -> input.readLong();
			case 3 -> input.readInt();
			case 4 -> input.readDouble();
			case 5 -> input.readFloat();
			case 6 -> input.readUTF();
			default -> throw new IllegalArgumentException("invalid cursor value type");
		};
	}

	Optional<Sort> resolveSort(String field, boolean reverse) throws IOException {
		IndexSearcher searcher = nrt_manager.acquire();
		try {
			var fieldInfos = FieldInfos.getMergedFieldInfos(searcher.getIndexReader());
			var types = EnumSet.noneOf(DocumentHelper.SortValueType.class);
			for (var type : DocumentHelper.SortValueType.values()) {
				var fieldInfo = fieldInfos.fieldInfo(DocumentHelper.sortField(field, type));
				if (fieldInfo != null && hasExpectedDocValuesType(fieldInfo.getDocValuesType(), type)) {
					types.add(type);
				}
			}

			if (types.size() != 1) {
				return Optional.empty();
			}
			return Optional.of(new Sort(createSortField(field, types.iterator().next(), reverse)));
		} finally {
			nrt_manager.release(searcher);
		}
	}

	private boolean hasExpectedDocValuesType(
			DocValuesType docValuesType,
			DocumentHelper.SortValueType type) {
		return switch (type) {
			case STRING -> docValuesType == DocValuesType.SORTED_SET;
			case NUMBER, BOOLEAN, DATE -> docValuesType == DocValuesType.SORTED_NUMERIC;
		};
	}

	private SortField createSortField(
			String field,
			DocumentHelper.SortValueType type,
			boolean reverse) {
		return switch (type) {
			case STRING -> stringSortField(field, reverse);
			case NUMBER -> numericSortField(
					field, type, SortField.Type.DOUBLE, reverse, Double.NEGATIVE_INFINITY);
			case BOOLEAN -> numericSortField(
					field, type, SortField.Type.LONG, reverse, Long.MIN_VALUE);
			case DATE -> numericSortField(
					field, type, SortField.Type.LONG, reverse, Long.MIN_VALUE);
		};
	}

	private SortField stringSortField(String field, boolean reverse) {
		var sortField = new SortedSetSortField(
				DocumentHelper.sortField(field, DocumentHelper.SortValueType.STRING),
				reverse,
				SortedSetSelector.Type.MIN);
		sortField.setMissingValue(SortField.STRING_FIRST);
		return sortField;
	}

	private SortField numericSortField(
			String field,
			DocumentHelper.SortValueType valueType,
			SortField.Type sortType,
			boolean reverse,
			Object missingValue) {
		var sortField = new SortedNumericSortField(
				DocumentHelper.sortField(field, valueType),
				sortType,
				reverse,
				SortedNumericSelector.Type.MIN);
		sortField.setMissingValue(missingValue);
		return sortField;
	}

	public void open(Path path) throws IOException {
		open(path, true);
	}

	public static boolean exists(Path path) throws IOException {
		if (!Files.isDirectory(path)) {
			return false;
		}
		try (var directory = FSDirectory.open(path)) {
			return DirectoryReader.indexExists(directory);
		}
	}

	public void open(Path path, boolean recreate) throws IOException {
		if (recreate && Files.exists(path)) {
			FileUtils.deleteFolder(path);
		}
		Files.createDirectories(path);

		this.directory = FSDirectory.open(path);
		
		
		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(
				new KeywordAnalyzer(),
				Map.of(
					TitleQueryFactory.FIELD_SEARCH_TITLE, INDEX_ANALYZER
				)
		);
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(recreate
				? IndexWriterConfig.OpenMode.CREATE
				: IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		indexWriterConfig.setCommitOnClose(true);
		nrt_index = new NRTCachingDirectory(directory, 5.0, 60.0);
		writer = new IndexWriter(nrt_index, indexWriterConfig);

		final SearcherFactory sf = new SearcherFactory();
		nrt_manager = new SearcherManager(writer, true, true, sf);
	}

}
