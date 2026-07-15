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
