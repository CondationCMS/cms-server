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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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

/**
 *
 * @author t.marx
 */
@Slf4j
public class LuceneIndex implements AutoCloseable {

	@FunctionalInterface
	interface UriVisitor {
		boolean visit(String uri) throws IOException;
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

	List<Document> query(Query query, Sort sort) throws IOException {
		IndexSearcher searcher = nrt_manager.acquire();
		try {
			var topDocs = searcher.search(query, Integer.MAX_VALUE, sort);

			List<Document> result = new ArrayList<>();
			for (var scoreDoc : topDocs.scoreDocs) {
				result.add(searcher.storedFields().document(scoreDoc.doc));
			}

			return result;
		} catch (IOException e) {
			log.error("", e);
		} finally {
			nrt_manager.release(searcher);
		}
		return Collections.emptyList();
	}

	List<Document> query(Query query) throws IOException {
		IndexSearcher searcher = nrt_manager.acquire();
		try {
			var topDocs = searcher.search(query, Integer.MAX_VALUE);

			List<Document> result = new ArrayList<>();
			for (var scoreDoc : topDocs.scoreDocs) {
				result.add(searcher.storedFields().document(scoreDoc.doc));
			}

			return result;
		} catch (IOException e) {
			log.error("", e);
		} finally {
			nrt_manager.release(searcher);
		}
		return Collections.emptyList();
	}

	int count(Query query) throws IOException {
		IndexSearcher searcher = nrt_manager.acquire();
		try {
			return searcher.count(query);
		} finally {
			nrt_manager.release(searcher);
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
		if (Files.exists(path)) {
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
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		indexWriterConfig.setCommitOnClose(true);
		nrt_index = new NRTCachingDirectory(directory, 5.0, 60.0);
		writer = new IndexWriter(nrt_index, indexWriterConfig);

		final SearcherFactory sf = new SearcherFactory();
		nrt_manager = new SearcherManager(writer, true, true, sf);
	}

}
