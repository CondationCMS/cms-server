package com.condation.cms.templates.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterPipeline {
    private final List<PipelineStep> steps = new ArrayList<>();
    private final FilterRegistry registry;

    public FilterPipeline(FilterRegistry registry) {
        this.registry = registry;
    }

    public void addStep(String filterName, String... params) {
        if (!registry.exists(filterName)) {
            throw new IllegalArgumentException("Filter not found: " + filterName);
        }
        steps.add(new PipelineStep(filterName, params));
    }

    public String execute(String input) {
        String result = input;
        for (PipelineStep step : steps) {
            Filter filter = registry.get(step.filterName);
            result = filter.apply(result, step.params);
        }
        return result;
    }

    private static class PipelineStep {
        private final String filterName;
        private final String[] params;

        public PipelineStep(String filterName, String... params) {
            this.filterName = filterName;
            this.params = params;
        }
    }
}
