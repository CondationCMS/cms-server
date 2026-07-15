package com.condation.cms.filesystem.metadata.persistent;

import java.util.Objects;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

public final class TitleQueryFactory {

    public static final String FIELD_SEARCH_TITLE =
            "_search.title";

    private final Analyzer searchAnalyzer;

    public TitleQueryFactory(Analyzer searchAnalyzer) {
        this.searchAnalyzer =
                Objects.requireNonNull(searchAnalyzer);
    }

    public Query createQuery(String input)
            throws QueryNodeException {

        if (input == null || input.isBlank()) {
            return MatchAllDocsQuery.INSTANCE;
        }

        StandardQueryParser parser =
                new StandardQueryParser(searchAnalyzer);

        parser.setDefaultOperator(
            StandardQueryConfigHandler.Operator.AND
        );

        parser.setAllowLeadingWildcard(false);

        String escapedInput =
                QueryParserUtil.escape(input.strip());

        return parser.parse(
            escapedInput,
            FIELD_SEARCH_TITLE
        );
    }
}