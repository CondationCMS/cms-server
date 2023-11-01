package com.github.thmarx.cms.modules.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.print.attribute.standard.Finishings;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class SearchEngine {

	private final Path path;
	private final String language;

	private Directory directory;
	private IndexWriter writer = null;

	private SearcherManager nrt_manager;
	private NRTCachingDirectory nrt_index;

	private boolean open;

	FacetsConfig facetConfig = new FacetsConfig();

	private Analyzer analyzerForLanguage() {
		return switch (language.toLowerCase()) {
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
	}

	public void open() throws IOException {
		if (Files.exists(path)) {
			FileUtils.deleteFolder(path);
		}
		Files.createDirectories(path);

		this.directory = FSDirectory.open(this.path);
		Analyzer analyzer = analyzerForLanguage();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		indexWriterConfig.setCommitOnClose(true);
		nrt_index = new NRTCachingDirectory(directory, 5.0, 60.0);
		writer = new IndexWriter(nrt_index, indexWriterConfig);

		final SearcherFactory sf = new SearcherFactory();
		nrt_manager = new SearcherManager(writer, true, true, sf);

		this.facetConfig.setMultiValued(SearchField.TAGS.getFieldName(), true);
	}

	public void index(final IndexDocument indexDocument) throws IOException {
		Document document = new Document();
		document.add(new StringField("uri", indexDocument.uri(), Field.Store.YES));
		document.add(new StringField("title", indexDocument.title(), Field.Store.YES));
		document.add(new StringField("content", indexDocument.content(), Field.Store.YES));

		indexDocument.tags().forEach(tag -> {
			document.add(new StringField(SearchField.TAGS.getFieldName(), tag, Field.Store.YES));
			document.add(new SortedSetDocValuesFacetField(SearchField.TAGS.getFieldName(), tag));
		});
		
		if (indexDocument.tags().isEmpty()){
			writer.addDocument(document);
		} else {
			writer.addDocument(facetConfig.build(document));
		}
	}
	
	public void delete(final String uri) throws IOException {
		writer.deleteDocuments(new Term("uri", uri));
	}
	
	public void commit () throws IOException {
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
}
