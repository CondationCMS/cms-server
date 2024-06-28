package com.github.thmarx.cms.filesystem.metadata.persistent;

import com.github.thmarx.cms.api.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NRTCachingDirectory;

/**
 *
 * @author t.marx
 */
public class LuceneIndex implements AutoCloseable {
	
	private Directory directory;
	private IndexWriter writer = null;

	private SearcherManager nrt_manager;
	private NRTCachingDirectory nrt_index;

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
	
	void add (Document document) throws IOException {
		writer.addDocument(document);
		commit();
	}
	
	void update (Term term, Document document) throws IOException {
		writer.updateDocument(term, document);
		commit();
	}
	
	void delete (Query query) throws IOException {
		writer.deleteDocuments(query);
		commit();
	}
	
	Optional<Document> query (Query query) throws IOException {
		IndexSearcher searcher = nrt_manager.acquire();
		try {
			
		} finally {
			nrt_manager.release(searcher);
		}
		return Optional.empty();
	}
	
	public void open (Path path) throws IOException {
		if (Files.exists(path)) {
			FileUtils.deleteFolder(path);
		}
		Files.createDirectories(path);

		this.directory = FSDirectory.open(path);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new KeywordAnalyzer());
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		indexWriterConfig.setCommitOnClose(true);
		nrt_index = new NRTCachingDirectory(directory, 5.0, 60.0);
		writer = new IndexWriter(nrt_index, indexWriterConfig);

		final SearcherFactory sf = new SearcherFactory();
		nrt_manager = new SearcherManager(writer, true, true, sf);
	}
}
