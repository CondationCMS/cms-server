package com.condation.cms.templates.filter;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import java.util.ArrayList;
import java.util.List;

public class FilterPipeline {
    private final List<PipelineStep> steps = new ArrayList<>();
    private final FilterRegistry registry;

    public FilterPipeline(FilterRegistry registry) {
        this.registry = registry;
    }

    public FilterPipeline addStep(String filterName, Object... params) {
        if (!registry.exists(filterName)) {
            throw new IllegalArgumentException("Filter not found: " + filterName);
        }
        steps.add(new PipelineStep(filterName, params));

        return this;
    }

    public Object execute(Object input) {
        Object result = input;
        for (PipelineStep step : steps) {
            Filter filter = registry.get(step.filterName);
            result = filter.apply(result, step.params);
        }
        return result;
    }

    private static class PipelineStep {
        private final String filterName;
        private final Object[] params;

        public PipelineStep(String filterName, Object... params) {
            this.filterName = filterName;
            this.params = params;
        }
    }
}
