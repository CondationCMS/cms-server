package com.condation.cms.templates.filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilterTest {

    FilterRegistry registry = new FilterRegistry();
    FilterPipeline pipeline = new FilterPipeline(registry);

    @BeforeEach
    public void setup() {
        // Register filters
        registry.register("raw", (input, params) -> input); // Raw does nothing
        registry.register("truncate", (input, params) -> {
            int length = params.length > 0 ? Integer.parseInt(params[0]) : input.length();
            return input.length() > length ? input.substring(0, length) + "..." : input;
        });

        pipeline.addStep("raw");
        pipeline.addStep("truncate", "20");
    }

    @Test
    void test() {
        String result = pipeline.execute("Dies ist ein langer Text, der abgeschnitten werden sollte.");
        Assertions.assertThat(result).isEqualTo("Dies ist ein langer ...");
    }
}
