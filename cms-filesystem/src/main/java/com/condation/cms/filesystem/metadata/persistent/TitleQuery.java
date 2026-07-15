package com.condation.cms.filesystem.metadata.persistent;

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.filesystem.MetaData;
import com.condation.cms.filesystem.metadata.PageMetaData;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@RequiredArgsConstructor
public class TitleQuery {

	private final TitleQueryFactory titleQueryFactory;
	private final String input;
	private final LuceneIndex index;
	private final MetaData metaData;

	private String contentType = Constants.DEFAULT_CONTENT_TYPE;

	public List<ContentNode> list() {
		return queryContentNodes();
	}

	private List<ContentNode> queryContentNodes() {

		try {
			BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
			queryBuilder.add(new TermQuery(new Term("content.type", contentType)), BooleanClause.Occur.MUST);
			queryBuilder.add(titleQueryFactory.createQuery(input), BooleanClause.Occur.MUST);
			List<Document> result = index.query(queryBuilder.build());

			var contentNodes = result.stream()
					.map(document -> document.get("_uri"))
					.map(metaData::byUri)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(node -> !node.isDirectory())
					.filter(PageMetaData::isPage)
					.filter(PageMetaData::isVisible)
					.toList();

			return contentNodes;
		} catch (IOException ex) {
			log.error("", ex);
		} catch (QueryNodeException ex) {
			log.error("", ex);
		}
		return Collections.emptyList();

	}
}
