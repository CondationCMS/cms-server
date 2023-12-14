package com.github.thmarx.cms.modules.search.index;

/*-
 * #%L
 * search-module
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.modules.search.FileUtils;
import com.github.thmarx.cms.modules.search.IndexDocument;
import com.github.thmarx.cms.modules.search.SearchField;
import com.github.thmarx.cms.modules.search.SearchRequest;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.bn.BengaliAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.et.EstonianAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.ne.NepaliAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.sr.SerbianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.ta.TamilAnalyzer;
import org.apache.lucene.analysis.te.TeluguAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;

/**
 *
 * @author thmar
 */
@Slf4j
@RequiredArgsConstructor
public class SearchIndex {

	private final Path path;
	private final String language;

	private Directory directory;
	private IndexWriter writer = null;

	private SearcherManager nrt_manager;
	private NRTCachingDirectory nrt_index;

	private boolean open;

	FacetsConfig facetConfig = new FacetsConfig();
	private StandardQueryParser queryParser;

	private FieldType FIELD_TYPE_TV;
	private Analyzer analyzer;

	{
		FieldType fieldType = new FieldType(TextField.TYPE_STORED);
		fieldType.setStoreTermVectors(true);
		fieldType.setStoreTermVectorPositions(true);
		fieldType.setStoreTermVectorPayloads(true);
		fieldType.setStoreTermVectorOffsets(true);
		fieldType.freeze();
		FIELD_TYPE_TV = fieldType;
	}

	private Analyzer analyzerForLanguage() {

		var analyzer = switch (language.toLowerCase()) {
			case "ar" ->
				new ArabicAnalyzer();
			case "bg" ->
				new BulgarianAnalyzer();
			case "bn" ->
				new BengaliAnalyzer();
			case "br" ->
				new BrazilianAnalyzer();
			case "ca" ->
				new CatalanAnalyzer();
			case "cjk" ->
				new CJKAnalyzer();
			case "ckb" ->
				new SoraniAnalyzer();
			case "de" ->
				new GermanAnalyzer();
			case "en" ->
				new EnglishAnalyzer();
			case "fr" ->
				new FrenchAnalyzer();
			case "cz" ->
				new CzechAnalyzer();
			case "da" ->
				new DanishAnalyzer();
			case "el" ->
				new GreekAnalyzer();
			case "es" ->
				new SpanishAnalyzer();
			case "et" ->
				new EstonianAnalyzer();
			case "eu" ->
				new BasqueAnalyzer();
			case "fa" ->
				new PersianAnalyzer();
			case "fi" ->
				new FinnishAnalyzer();
			case "ga" ->
				new IrishAnalyzer();
			case "gl" ->
				new GalicianAnalyzer();
			case "hi" ->
				new HindiAnalyzer();
			case "hu" ->
				new HungarianAnalyzer();
			case "hy" ->
				new ArmenianAnalyzer();
			case "id" ->
				new IndonesianAnalyzer();
			case "it" ->
				new ItalianAnalyzer();
			case "lv" ->
				new LatvianAnalyzer();
			case "ne" ->
				new NepaliAnalyzer();
			case "nl" ->
				new DutchAnalyzer();
			case "no" ->
				new NorwegianAnalyzer();
			case "pt" ->
				new PortugueseAnalyzer();
			case "ro" ->
				new RomanianAnalyzer();
			case "ru" ->
				new RussianAnalyzer();
			case "sr" ->
				new SerbianAnalyzer();
			case "sv" ->
				new SwedishAnalyzer();
			case "ta" ->
				new TamilAnalyzer();
			case "te" ->
				new TeluguAnalyzer();
			case "th" ->
				new ThaiAnalyzer();
			case "tr" ->
				new TurkishAnalyzer();
			default ->
				new StandardAnalyzer();
		};
		log.debug("analyzer {} for language {} selected", analyzer.getClass(), language);
		return analyzer;
	}

	public void open() throws IOException {
		if (Files.exists(path)) {
			FileUtils.deleteFolder(path);
		}
		Files.createDirectories(path);

		this.directory = FSDirectory.open(this.path);
		this.analyzer = analyzerForLanguage();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		indexWriterConfig.setCommitOnClose(true);
		nrt_index = new NRTCachingDirectory(directory, 5.0, 60.0);
		writer = new IndexWriter(nrt_index, indexWriterConfig);

		final SearcherFactory sf = new SearcherFactory();
		nrt_manager = new SearcherManager(writer, true, true, sf);

		this.facetConfig.setMultiValued(SearchField.TAGS.getFieldName(), true);

		this.queryParser = new StandardQueryParser(analyzer);
	}

	public void index(final IndexDocument indexDocument) throws IOException {
		Document document = new Document();
		document.add(new StringField("uri", indexDocument.uri(), Field.Store.YES));
		document.add(new StringField("title", indexDocument.title(), Field.Store.YES));
		document.add(new TextField("content", indexDocument.content(), Field.Store.YES));
		document.add(new Field("content_tv", indexDocument.content(), FIELD_TYPE_TV));

		indexDocument.tags().forEach(tag -> {
			document.add(new StringField(SearchField.TAGS.getFieldName(), tag, Field.Store.YES));
			document.add(new SortedSetDocValuesFacetField(SearchField.TAGS.getFieldName(), tag));
		});

		if (indexDocument.tags().isEmpty()) {
			writer.addDocument(document);
		} else {
			writer.addDocument(facetConfig.build(document));
		}
	}

	public void delete(final String uri) throws IOException {
		writer.deleteDocuments(new Term("uri", uri));
	}

	public void commit() throws IOException {
		writer.flush();
		writer.commit();
		nrt_manager.maybeRefresh();
	}

	public void close() throws IOException {
		if (!open) {
			return;
		}
		if (writer != null) {
			writer.close();
			nrt_manager.close();

			writer = null;
		}

		if (directory != null) {
			directory.close();
		}
		this.open = false;
	}

	public SearchResult search(SearchRequest request) throws IOException {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

		IndexSearcher searcher = nrt_manager.acquire();
		try {

			if (!Strings.isNullOrEmpty(request.query())) {
				Query searchQuery = queryParser.parse(request.query(), "content");
				queryBuilder.add(searchQuery, BooleanClause.Occur.MUST);
			}

			Query query = queryBuilder.build();

			SearchResult result = new SearchResult();

			Optional<ScoreDoc> lastScoreDoc = getLastScoreDoc(request.page(), request.size(), query, searcher);

			TopDocs topDocs;
			if (lastScoreDoc.isPresent()) {
				topDocs = searcher.searchAfter(lastScoreDoc.get(), query, request.size());
			} else {
				topDocs = searcher.search(query, request.size());
			}
			result.setTotal(topDocs.totalHits.value);

			Formatter formatter = new SimpleHTMLFormatter();
			QueryScorer scorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(formatter, scorer);
			Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);

			for (ScoreDoc sdoc : topDocs.scoreDocs) {
				Document doc = searcher.storedFields().document(sdoc.doc);

				var item = new SearchResult.Item();
				item.setUri(doc.get("uri"));
				item.setTitle(doc.get("title"));
				
				final String content = doc.get("content");
				TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), sdoc.doc, "content", analyzer);
				item.setContent(highlighter.getBestFragment(tokenStream, content));
				

				result.getItems().add(item);
			}
			return result;
		} catch (IOException | QueryNodeException ex) {
			log.error("", ex);
		} catch (InvalidTokenOffsetsException ex) {
			log.error("", ex);
		} finally {
			nrt_manager.release(searcher);
		}

		return new SearchResult();
	}

	private Optional<ScoreDoc> getLastScoreDoc(final Integer pageNumber, final Integer pageSize, final Query query, final IndexSearcher searcher)
			throws IOException {
		if (pageNumber == 1) {
			return Optional.empty();
		}
		int total = pageSize * (pageNumber - 1);
		TopDocs topDocs = searcher.search(query, total);
		return Optional.of(topDocs.scoreDocs[total - 1]);
	}

	public void clear() throws IOException {
		writer.deleteAll();
		commit();
	}
}
