package com.condation.cms.templates.filter;

@FunctionalInterface
public interface Filter {
    String apply(String input, String... params);
}
