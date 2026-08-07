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
import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.VariantSearchMode;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.workflow.DefaultWFStatusProvider;
import com.condation.cms.api.workflow.WFStatusQueryProvider;
import com.condation.cms.api.workflow.WFStatusProvider;
import com.condation.cms.filesystem.MetaData;
import com.condation.cms.filesystem.metadata.PageMetaData;
import com.condation.cms.filesystem.metadata.query.ExcerptMapperFunction;
import com.condation.cms.filesystem.metadata.query.ExtendableQuery;
import com.condation.cms.filesystem.metadata.query.Queries;
import com.condation.cms.filesystem.metadata.query.parser.Parser;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Expression;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 *
 * @author t.marx
 * @param <T>
 */
@Slf4j
@RequiredArgsConstructor
public class LuceneQuery<T> extends ExtendableQuery<T> implements ContentQuery.Sort<T> {

	private static final int SCAN_BATCH_SIZE = 128;
	private static final WFStatusProvider DEFAULT_STATUS_PROVIDER = new DefaultWFStatusProvider();

    private final LuceneIndex index;
    private final MetaData metaData;
    private final ExcerptMapperFunction<T> nodeMapper;

    private String contentType = Constants.DEFAULT_CONTENT_TYPE;

    private final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

    enum Order {
        ASC, DESC;
    }

    private Order sortOrder = Order.ASC;
    private Optional<String> orderByField = Optional.empty();

    private Optional<String> startUri = Optional.empty();

    private List<Predicate<ContentNode>> extensionOperations = new ArrayList<>();

    private final Parser expressionsParser = new Parser();

    public LuceneQuery(
            final String startUri,
            final LuceneIndex index,
            final MetaData metaData,
            final ExcerptMapperFunction<T> nodeMapper) {
        this(index, metaData, nodeMapper);
        this.startUri = Optional.ofNullable(startUri);
    }

    @Override
    public ContentQuery<T> excerpt(long excerptLength) {
        nodeMapper.setExcerpt((int) excerptLength);
        return this;
    }

    public Page<T> page(final Object page, final Object size) {
        int i_page = Constants.DEFAULT_PAGE;
        int i_size = Constants.DEFAULT_PAGE_SIZE;
        if (page instanceof Integer || page instanceof Long) {
            i_page = ((Number) page).intValue();
        } else if (page instanceof String string) {
            i_page = Integer.parseInt(string);
        }
        if (size instanceof Integer || size instanceof Long) {
            i_size = ((Number) size).intValue();
        } else if (size instanceof String string) {
            i_size = Integer.parseInt(string);
        }
        return page((int) i_page, (int) i_size);
    }

    @Override
    public Page<T> page(long page, long size) {
		validatePage(page, size);
		long offset;
		try {
			offset = Math.multiplyExact(page - 1, size);
		} catch (ArithmeticException ex) {
			throw new IllegalArgumentException("page offset is too large", ex);
		}

		try {
			Query structuralQuery = structuralVisibilityQuery(buildBaseQuery());
			org.apache.lucene.search.Sort luceneSort = null;
			if (orderByField.isPresent()) {
				var resolvedSort = index.resolveSort(
						orderByField.get(), Order.DESC.equals(sortOrder));
				if (resolvedSort.isEmpty()) {
					return inMemorySortedPage(page, size, offset);
				}
				luceneSort = resolvedSort.get();
			}

			var completeVisibilityQuery = completeVisibilityQuery(structuralQuery);
			if (completeVisibilityQuery.isPresent() && extensionOperations.isEmpty()) {
				return fastPage(completeVisibilityQuery.get(), luceneSort, page, size, offset);
			}

			return filteredPage(structuralQuery, luceneSort, page, size, offset);
		} catch (IOException ex) {
			log.error("error paging lucene query", ex);
			return Page.EMPTY;
		}
    }

    @Override
    public List<T> get() {

        var contentNodes = queryContentNodes();
        // sorting
        if (orderByField.isPresent()) {
            contentNodes = QueryHelper.sorted(contentNodes, orderByField.get(), Order.ASC.equals(sortOrder));
        }
        // mapping
        var result = mapContentNodes(contentNodes);

        return result.nodes;
    }

	private List<ContentNode> queryContentNodes() {
		try {
			var contentNodes = new ArrayList<ContentNode>();
			var query = structuralVisibilityQuery(buildBaseQuery());
			index.scanUris(query, null, SCAN_BATCH_SIZE, uri -> {
				metaData.byPath(uri)
						.filter(this::isAcceptedNode)
						.ifPresent(contentNodes::add);
				return true;
			});
			return contentNodes;
		} catch (IOException ex) {
			log.error("error scanning lucene query", ex);
			return Collections.emptyList();
		}
	}

	private Page<T> fastPage(
			Query query,
			org.apache.lucene.search.Sort sort,
			long page,
			long size,
			long offset) throws IOException {
		long totalItems = index.count(query);
		if (offset >= totalItems) {
			return new Page<>(totalItems, size, totalPages(totalItems, size), (int) page, List.of());
		}

		var nodes = new ArrayList<ContentNode>((int) Math.min(size, Integer.MAX_VALUE));
		var hitIndex = new long[]{0};
		index.scanUris(query, sort, batchSize(size), uri -> {
			long currentIndex = hitIndex[0]++;
			if (currentIndex < offset) {
				return true;
			}
			metaData.byPath(uri).ifPresent(nodes::add);
			return nodes.size() < size;
		});

		return new Page<>(
				totalItems,
				size,
				totalPages(totalItems, size),
				(int) page,
				mapContentNodes(nodes).nodes);
	}

	private Page<T> filteredPage(
			Query query,
			org.apache.lucene.search.Sort sort,
			long page,
			long size,
			long offset) throws IOException {
		var pageNodes = new ArrayList<ContentNode>((int) Math.min(size, Integer.MAX_VALUE));
		var acceptedCount = new long[]{0};

		index.scanUris(query, sort, batchSize(size), uri -> {
			var node = metaData.byPath(uri);
			if (node.isEmpty() || !isAcceptedNode(node.get())) {
				return true;
			}

			long currentIndex = acceptedCount[0]++;
			if (currentIndex >= offset && pageNodes.size() < size) {
				pageNodes.add(node.get());
			}
			// Continue to preserve exact totalItems/totalPages for node-based filters.
			return true;
		});

		long totalItems = acceptedCount[0];
		return new Page<>(
				totalItems,
				size,
				totalPages(totalItems, size),
				(int) page,
				mapContentNodes(pageNodes).nodes);
	}

	private Page<T> inMemorySortedPage(long page, long size, long offset) {
		var contentNodes = QueryHelper.sorted(
				queryContentNodes(),
				orderByField.orElseThrow(),
				Order.ASC.equals(sortOrder));
		var pageNodes = contentNodes.stream().skip(offset).limit(size).toList();
		long totalItems = contentNodes.size();
		return new Page<>(
				totalItems,
				size,
				totalPages(totalItems, size),
				(int) page,
				mapContentNodes(pageNodes).nodes);
	}

	private Query buildBaseQuery() {
		var baseQuery = new BooleanQuery.Builder();
		queryBuilder.build().clauses().forEach(baseQuery::add);
		baseQuery.add(
				new TermQuery(new Term("content.type", contentType)),
				BooleanClause.Occur.MUST);
		startUri.ifPresent(uri -> baseQuery.add(
				new PrefixQuery(new Term("_uri", uri)),
				BooleanClause.Occur.FILTER));
		return baseQuery.build();
	}

	private Query structuralVisibilityQuery(Query query) {
		var visiblePages = new BooleanQuery.Builder();
		visiblePages.add(query, BooleanClause.Occur.MUST);
		visiblePages.add(
				new TermQuery(new Term(DocumentHelper.FIELD_IS_PAGE, Boolean.TRUE.toString())),
				BooleanClause.Occur.FILTER);
		if (!isManagerPreview()) {
			visiblePages.add(
					new TermQuery(new Term(DocumentHelper.FIELD_HIDDEN_PATH, Boolean.FALSE.toString())),
					BooleanClause.Occur.FILTER);
		}
		return visiblePages.build();
	}

	private Optional<Query> completeVisibilityQuery(Query structuralQuery) {
		if (hasPreview()) {
			return Optional.of(structuralQuery);
		}

		var publishedQuery = indexedPublishedQuery();
		if (publishedQuery.isEmpty()) {
			return Optional.empty();
		}

		long now = System.currentTimeMillis();
		var visible = new BooleanQuery.Builder();
		visible.add(structuralQuery, BooleanClause.Occur.MUST);
		visible.add(publishedQuery.get(), BooleanClause.Occur.FILTER);
		visible.add(scheduleStartQuery(now), BooleanClause.Occur.FILTER);
		visible.add(scheduleEndQuery(now), BooleanClause.Occur.FILTER);
		return Optional.of(visible.build());
	}

	private Optional<Query> indexedPublishedQuery() {
		if (!(statusProvider() instanceof WFStatusQueryProvider queryProvider)) {
			return Optional.empty();
		}

		var workflowFilter = new LuceneQuery<T>(index, metaData, nodeMapper);
		if (queryProvider.published(workflowFilter).isEmpty()) {
			return Optional.empty();
		}

		var query = workflowFilter.queryBuilder.build();
		return query.clauses().isEmpty() ? Optional.empty() : Optional.of(query);
	}

	private Query scheduleStartQuery(long now) {
		return missingOr(
				Constants.MetaFields.PUBLISH_DATE,
				LongField.newRangeQuery(
						Constants.MetaFields.PUBLISH_DATE + "_date",
						Long.MIN_VALUE,
						now));
	}

	private Query scheduleEndQuery(long now) {
		return missingOr(
				Constants.MetaFields.UNPUBLISH_DATE,
				LongField.newRangeQuery(
						Constants.MetaFields.UNPUBLISH_DATE + "_date",
						now == Long.MAX_VALUE ? Long.MAX_VALUE : now + 1,
						Long.MAX_VALUE));
	}

	private Query missingOr(String field, Query presentValueQuery) {
		var missing = new BooleanQuery.Builder();
		missing.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
		missing.add(
				new TermQuery(new Term("_fields", field)),
				BooleanClause.Occur.MUST_NOT);

		var result = new BooleanQuery.Builder();
		result.add(missing.build(), BooleanClause.Occur.SHOULD);
		result.add(presentValueQuery, BooleanClause.Occur.SHOULD);
		result.setMinimumNumberShouldMatch(1);
		return result.build();
	}

	private WFStatusProvider statusProvider() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()) {
			var context = RequestContextScope.REQUEST_CONTEXT.get();
			if (context.has(WorkflowFeature.class)) {
				return context.get(WorkflowFeature.class).workflow().getStatusProvider();
			}
		}
		return DEFAULT_STATUS_PROVIDER;
	}

	private boolean hasPreview() {
		return RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class);
	}

	private boolean isManagerPreview() {
		return hasPreview()
				&& RequestContextScope.REQUEST_CONTEXT.get()
						.get(IsPreviewFeature.class).mode() == IsPreviewFeature.Mode.MANAGER;
	}

	private boolean isAcceptedNode(ContentNode node) {
		return !node.isDirectory()
				&& PageMetaData.isPage(node)
				&& PageMetaData.isVisible(node)
				&& extensionOperations.stream().allMatch(predicate -> predicate.test(node));
	}

	private void validatePage(long page, long size) {
		if (page < 1) {
			throw new IllegalArgumentException("page must be greater than zero");
		}
		if (size < 1 || size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("size must be between 1 and " + Integer.MAX_VALUE);
		}
	}

	private int batchSize(long size) {
		return Math.clamp(size * 2, SCAN_BATCH_SIZE, 1024);
	}

	private int totalPages(long totalItems, long size) {
		return Math.clamp((totalItems + size - 1) / size, 0, Integer.MAX_VALUE);
	}

    private NodeResult<T> mapContentNodes(List<ContentNode> contentNodes) {
        var mappedContentNodes = contentNodes.stream()
                .map(nodeMapper)
                .toList();

        var total = contentNodes.size();

        return new NodeResult<>(total, mappedContentNodes);
    }

    @Override
    public Map<Object, List<ContentNode>> groupby(String field) {
        var nodes = queryContentNodes();
        return QueryUtil.groupby(nodes.stream(), field);
    }

    @Override
    public Sort<T> orderby(String field) {
        this.orderByField = Optional.ofNullable(field);
        return this;
    }

    @Override
    public ContentQuery<T> json() {
        this.contentType = Constants.ContentTypes.JSON;
        return this;
    }

    @Override
    public ContentQuery<T> html() {
        this.contentType = Constants.ContentTypes.HTML;
        return this;
    }

    @Override
    public ContentQuery<T> contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public ContentQuery<T> variants(VariantSearchMode mode) {
        Objects.requireNonNull(mode, "mode must not be null");
        if (mode != VariantSearchMode.ALL) {
            queryBuilder.add(
                    new TermQuery(new Term("_variant", Boolean.toString(mode == VariantSearchMode.VARIANT))),
                    BooleanClause.Occur.FILTER
            );
        }
        return this;
    }

    @Override
    public ContentQuery<T> where(String field, Object value) {
        return where(field, Queries.Operator.EQ, value);
    }

    @Override
    public ContentQuery<T> where(String field, String operator, Object value) {
        if (Queries.isDefaultOperation(operator)) {
            return where(field, Queries.operator4String(operator), value);
        } else if (getContext().getQueryOperations().containsKey(operator)) {
            extensionOperations.add(
                    (Predicate<ContentNode>) Queries.createExtensionPredicate(
                            field,
                            value,
                            getContext().getQueryOperations().get(operator)
                    ));
            return this;
        }
        throw new IllegalArgumentException("unknown operator " + operator);
    }

    @Override
    public ContentQuery<T> whereContains(String field, Object value) {
        return where(field, Queries.Operator.CONTAINS, value);
    }

    @Override
    public ContentQuery<T> whereNotContains(String field, Object value) {
        return where(field, Queries.Operator.CONTAINS_NOT, value);
    }

    @Override
    public ContentQuery<T> whereIn(String field, Object... value) {
        return where(field, Queries.Operator.IN, value);
    }

    @Override
    public ContentQuery<T> whereIn(String field, List<Object> value) {
        return where(field, Queries.Operator.IN, value);
    }

    @Override
    public ContentQuery<T> whereNotIn(String field, Object... value) {
        return where(field, Queries.Operator.NOT_IN, value);
    }

    @Override
    public ContentQuery<T> whereNotIn(String field, List<Object> value) {
        return where(field, Queries.Operator.NOT_IN, value);
    }

    @Override
    public ContentQuery<T> whereExists(String field) {
        QueryHelper.exists(queryBuilder, field);
        return this;
    }

    @Override
    public ContentQuery<T> expression(final String expression) {
        Expression expAst = expressionsParser.parse(expression);

        ExpressionQueryHelper.buildFromExpression(queryBuilder, expAst);

        return this;
    }

    private ContentQuery<T> where(final String field, final Queries.Operator operator, final Object value) {

        // 1. Ermittle dynamisch den korrekten Feldnamen anhand des Datentyps und Operators
        final String targetField = QueryHelper.resolveFieldName(field, operator, value);

        // Wichtig: Die Existenzprüfung muss ggf. auf das korrekte Zielfeld oder das Hauptfeld gehen.
        // Wenn QueryHelper.exists prüft, ob das Feld im Dokument existiert, ist das Hauptfeld "field" sicherer,
        // da Suffix-Felder (wie _double) bei ungültigen Eingaben (z.B. "2020-2025") fehlen können.
        QueryHelper.exists(queryBuilder, field);

        // 2. Erzeuge die Query mit dem aufgelösten 'targetField'
        switch (operator) {
            case EQ ->
                QueryHelper.eq(queryBuilder, targetField, value, BooleanClause.Occur.MUST);
            case NOT_EQ ->
                QueryHelper.eq(queryBuilder, targetField, value, BooleanClause.Occur.MUST_NOT);
            case CONTAINS ->
                QueryHelper.contains(queryBuilder, targetField, value, BooleanClause.Occur.MUST);
            case CONTAINS_NOT ->
                QueryHelper.contains(queryBuilder, targetField, value, BooleanClause.Occur.MUST_NOT);
            case IN ->
                QueryHelper.in(queryBuilder, targetField, value, BooleanClause.Occur.MUST);
            case NOT_IN ->
                QueryHelper.in(queryBuilder, targetField, value, BooleanClause.Occur.MUST_NOT);
            case LT ->
                QueryHelper.lt(queryBuilder, targetField, value);
            case LTE ->
                QueryHelper.lte(queryBuilder, targetField, value);
            case GT ->
                QueryHelper.gt(queryBuilder, targetField, value);
            case GTE ->
                QueryHelper.gte(queryBuilder, targetField, value);
        }

        return this;
    }

    @Override
    public ContentQuery<T> asc() {
        this.sortOrder = Order.ASC;
        return this;
    }

    @Override
    public ContentQuery<T> desc() {
        this.sortOrder = Order.DESC;
        return this;
    }

    private record NodeResult<T>(int total, List<T> nodes) {

    }
}
