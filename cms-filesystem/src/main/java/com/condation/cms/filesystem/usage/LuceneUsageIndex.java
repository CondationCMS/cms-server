package com.condation.cms.filesystem.usage;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;

/**
 *
 * @author thmarx
 */
@Log4j2
public class LuceneUsageIndex implements UsageIndex {

	public static class Fields {

		public static final String SOURCE_ID = "source_id";
		public static final String SOURCE_TYPE = "source_type";
		public static final String TARGET_ID = "target_id";
		public static final String TARGET_TYPE = "target_type";
		public static final String REFERENCE_TYPE = "reference_type";
	}

	private final Directory directory;
	private final IndexWriter writer;

	private final SearcherManager searchManager;
	private final NRTCachingDirectory nrtCacheDirectory;

	public LuceneUsageIndex(final Path parent) throws IOException {
		Path path = parent.resolve("usage_index");
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}

		directory = FSDirectory.open(path);
		nrtCacheDirectory = new NRTCachingDirectory(directory, 5.0, 60.0);

		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// CREATE_OR_APPEND
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		writer = new IndexWriter(nrtCacheDirectory, config);

		searchManager = new SearcherManager(writer, true, true, null);
	}

	@Override
	public void clearAll() {
		try {
			writer.deleteAll();
			writer.commit();
		} catch (Exception e) {
			log.error(e);
			try {
				writer.rollback();
			} catch (IOException ex) {
				log.error("", ex);
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the used references.
	 *
	 * @param source
	 * @param sourceType
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<Reference> getUses(final String source, final String sourceType) throws IOException {
		UsageQuery query = new UsageQuery();
		query.setDirection(UsageQuery.Direction.OUT);
		query.setId(source);
		query.setType(sourceType);

		return query_usage(query);
	}

	@Override
	public List<Reference> getUses(final String source, final String sourceType, final String referenceType) throws IOException {
		UsageQuery query = new UsageQuery();
		query.setDirection(UsageQuery.Direction.OUT);
		query.setId(source);
		query.setType(sourceType);
		query.setReferenceType(referenceType);

		return query_usage(query);
	}

	/**
	 * returns References who use the Target.
	 *
	 * @param target
	 * @param targetType
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<Reference> getUsedBy(final String target, final String targetType) throws IOException {
		UsageQuery query = new UsageQuery();
		query.setDirection(UsageQuery.Direction.IN);
		query.setId(target);
		query.setType(targetType);

		return query_usage(query);
	}

	@Override
	public List<Reference> getUsedBy(final String target, final String targetType, final String referenceType) throws IOException {

		UsageQuery query = new UsageQuery();
		query.setDirection(UsageQuery.Direction.IN);
		query.setId(target);
		query.setType(targetType);
		query.setReferenceType(referenceType);

		return query_usage(query);
	}

	@Override
	public boolean isUsing(String source, String sourceType, String referenceType, String target, String targetType) throws IOException {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(new TermQuery(new Term(Fields.SOURCE_ID, source)), BooleanClause.Occur.MUST);
		builder.add(new TermQuery(new Term(Fields.SOURCE_TYPE, sourceType)), BooleanClause.Occur.MUST);
		builder.add(new TermQuery(new Term(Fields.TARGET_ID, target)), BooleanClause.Occur.MUST);
		builder.add(new TermQuery(new Term(Fields.TARGET_TYPE, targetType)), BooleanClause.Occur.MUST);
		builder.add(new TermQuery(new Term(Fields.REFERENCE_TYPE, referenceType)), BooleanClause.Occur.MUST);

		IndexSearcher searcher = searchManager.acquire();
		try {
			TopDocs topDocs = searcher.search(builder.build(), 1);
			return topDocs.totalHits.value() > 0;
		} finally {
			searchManager.release(searcher);
		}
	}

	@Data
	public static class UsageQuery {

		public enum Direction {
			IN, OUT
		}

		private String id;
		private String type;
		private String referenceType;
		private Direction direction;
	}

	public List<Reference> query_usage(final UsageQuery usageQuery) throws IOException {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();

		if (UsageQuery.Direction.OUT.equals(usageQuery.direction)) {
			builder.add(new TermQuery(new Term(Fields.SOURCE_ID, usageQuery.getId())), BooleanClause.Occur.MUST);
			builder.add(new TermQuery(new Term(Fields.SOURCE_TYPE, usageQuery.getType())), BooleanClause.Occur.MUST);
		} else if (UsageQuery.Direction.IN.equals(usageQuery.direction)) {
			builder.add(new TermQuery(new Term(Fields.TARGET_ID, usageQuery.getId())), BooleanClause.Occur.MUST);
			builder.add(new TermQuery(new Term(Fields.TARGET_TYPE, usageQuery.getType())), BooleanClause.Occur.MUST);
		}

		if (!Strings.isNullOrEmpty(usageQuery.getReferenceType())) {
			builder.add(new TermQuery(new Term(Fields.REFERENCE_TYPE, usageQuery.getReferenceType())), BooleanClause.Occur.MUST);
		}

		IndexSearcher searcher = searchManager.acquire();
		Document doc = null;
		try {
			List<Reference> result = new ArrayList<>();
			TopDocs topDocs = searcher.search(builder.build(), Integer.MAX_VALUE);
			for (ScoreDoc sdoc : topDocs.scoreDocs) {
				doc = searcher.storedFields().document(sdoc.doc);
				final String sid = doc.get(Fields.SOURCE_ID);
				final String styp = doc.get(Fields.SOURCE_TYPE);
				final String tid = doc.get(Fields.TARGET_ID);
				final String ttype = doc.get(Fields.TARGET_TYPE);
				final String rtype = doc.get(Fields.REFERENCE_TYPE);
				result.add(new Reference(sid, styp, tid, ttype, rtype));
			}
			return result;
		} finally {
			searchManager.release(searcher);
		}
	}

	@Override
	public void addUsage(final Reference reference) throws IOException {
		deleteUsage(reference);

		Document usage = new Document();
		usage.add(new StringField(Fields.SOURCE_ID, reference.source, Field.Store.YES));
		usage.add(new StringField(Fields.SOURCE_TYPE, reference.sourceType, Field.Store.YES));
		usage.add(new StringField(Fields.TARGET_ID, reference.target, Field.Store.YES));
		usage.add(new StringField(Fields.TARGET_TYPE, reference.targetType, Field.Store.YES));
		usage.add(new StringField(Fields.REFERENCE_TYPE, reference.referenceType, Field.Store.YES));

		writer.addDocument(usage);
		writer.commit();

		searchManager.maybeRefresh();
	}

	/**
	 * deletes a usage object.
	 *
	 * @param source
	 * @param sourceType
	 * @param target
	 * @param targetType
	 * @throws IOException
	 */
	private void deleteUsage(final Reference reference) throws IOException {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_ID, reference.source)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_TYPE, reference.sourceType)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.TARGET_ID, reference.target)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.TARGET_TYPE, reference.targetType)), BooleanClause.Occur.MUST);

		writer.deleteDocuments(queryBuilder.build());

		searchManager.maybeRefresh();
	}

	/**
	 * Call this after a instance is deleted, source and targets for this
	 * instance will be deleted.
	 *
	 * @param id
	 * @param type
	 * @throws IOException
	 */
	@Override
	public void clearUsage(final String id, final String type) throws IOException {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_ID, id)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_TYPE, type)), BooleanClause.Occur.MUST);

		writer.deleteDocuments(queryBuilder.build());

		queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(new TermQuery(new Term(Fields.TARGET_ID, id)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.TARGET_TYPE, type)), BooleanClause.Occur.MUST);

		writer.deleteDocuments(queryBuilder.build());

		searchManager.maybeRefresh();
	}

	@Override
	public void removeTargets(String source, String sourceType, String referenceType) throws IOException {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_ID, source)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_TYPE, sourceType)), BooleanClause.Occur.MUST);
		queryBuilder.add(new TermQuery(new Term(Fields.SOURCE_TYPE, referenceType)), BooleanClause.Occur.MUST);

		writer.deleteDocuments(queryBuilder.build());

		searchManager.maybeRefresh();
	}

	@Override
	public void close() throws IOException {
		try {
			searchManager.close();   // zuerst Reader-Manager schließen
		} finally {
			writer.commit();
			writer.close();
			nrtCacheDirectory.close(); // auch diesen schließen
			directory.close();
		}
	}

}
