package com.condation.cms.modules.system.api.helpers;

/*-
 * #%L
 * api-module
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

import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.DB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class QueryParserTest {

    @Mock
    private DB db;

    @Mock
    private Content content;

    @Mock
    private ContentQuery contentQuery;

    @Mock
    private ContentQuery.Sort sort;

    private QueryParser queryParser;

    @BeforeEach
    void setUp() {
        queryParser = new QueryParser();
        when(db.getContent()).thenReturn(content);
        when(content.query(any())).thenReturn(contentQuery);
    }

    @Test
    void testSimpleQuery() {
        String json = "{\"contentType\": \"post\"}";
        queryParser.parse(db, json);
        verify(contentQuery).contentType("post");
        verify(contentQuery).get();
    }

    @Test
    void testWhereQuery() {
        String json = "{\"where\": [{\"field\": \"author\", \"value\": \"John Doe\"}]}";
        queryParser.parse(db, json);
        verify(contentQuery).where("author", "=", "John Doe");
        verify(contentQuery).get();
    }

    @Test
    void testOrderByQuery() {
        String json = "{\"orderby\": {\"field\": \"date\", \"direction\": \"desc\"}}";
        when(contentQuery.orderby("date")).thenReturn(sort);
        queryParser.parse(db, json);
        verify(sort).desc();
        verify(contentQuery).get();
    }

    @Test
    void testPageQuery() {
        String json = "{\"page\": {\"number\": 2, \"size\": 10}}";
        queryParser.parse(db, json);
        verify(contentQuery).page(2, 10);
    }

    @Test
    void testComplexQuery() {
        String json = "{\"contentType\": \"article\", \"where\": [{\"field\": \"category\", \"value\": \"tech\"}], \"orderby\": {\"field\": \"views\", \"direction\": \"desc\"}}";
        when(contentQuery.orderby("views")).thenReturn(sort);
        queryParser.parse(db, json);
        verify(contentQuery).contentType("article");
        verify(contentQuery).where("category", "=", "tech");
        verify(sort).desc();
        verify(contentQuery).get();
    }
}
