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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.DB;
import static com.condation.cms.core.configuration.GSONProvider.GSON;
import java.util.List;
import java.util.Map;

public class QueryParser {

    public Object parse(DB db, String jsonBody) {
        Map<String, Object> queryMap = GSON.fromJson(jsonBody, Map.class);
        ContentQuery query = db.getContent().query((node, a) -> node);

        if (queryMap.containsKey("contentType")) {
            query.contentType((String) queryMap.get("contentType"));
        }

        if (queryMap.containsKey("where")) {
            List<Map<String, Object>> whereClauses = (List<Map<String, Object>>) queryMap.get("where");
            for (Map<String, Object> clause : whereClauses) {
                String field = (String) clause.get("field");
                Object value = clause.get("value");
                String operator = (String) clause.getOrDefault("operator", "=");
                query.where(field, operator, value);
            }
        }

        if (queryMap.containsKey("expression")) {
            query.expression((String) queryMap.get("expression"));
        }

        if (queryMap.containsKey("orderby")) {
            Map<String, String> orderbyMap = (Map<String, String>) queryMap.get("orderby");
            String field = orderbyMap.get("field");
            String direction = orderbyMap.getOrDefault("direction", "asc");
            if ("desc".equalsIgnoreCase(direction)) {
                query.orderby(field).desc();
            } else {
                query.orderby(field).asc();
            }
        }

        if (queryMap.containsKey("page")) {
            Map<String, Double> pageMap = (Map<String, Double>) queryMap.get("page");
            long page = pageMap.get("number").longValue();
            long size = pageMap.get("size").longValue();
            return query.page(page, size);
        } else {
            return query.get();
        }
    }
}
