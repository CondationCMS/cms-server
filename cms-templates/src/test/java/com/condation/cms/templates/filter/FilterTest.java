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

import com.condation.cms.templates.filter.impl.DateFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            if (input instanceof String stringValue) {
                int length = params.length > 0 ? (Integer)params[0] : stringValue.length();
                return stringValue.length() > length ? stringValue.substring(0, length) + "..." : input;
            }
            return input;
        });

        pipeline.addStep("raw");
        pipeline.addStep("truncate", 20);
    }

    @Test
    void test() {
        Object result = pipeline.execute("Dies ist ein langer Text, der abgeschnitten werden sollte.");
        Assertions.assertThat(result).isEqualTo("Dies ist ein langer ...");
    }
	
	@Test
    void date() {
		
		FilterRegistry registry = new FilterRegistry();
		FilterPipeline pipeline = new FilterPipeline(registry);
		registry.register(DateFilter.NAME, new DateFilter());
		
		pipeline.addStep("date");
		
		
		var date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		var formatted = format.format(date);
		
        Object result = pipeline.execute(date);
        Assertions.assertThat(result).isEqualTo(formatted);
    }
	
	@Test
    void date_custom_format() {
		
		FilterRegistry registry = new FilterRegistry();
		FilterPipeline pipeline = new FilterPipeline(registry);
		registry.register(DateFilter.NAME, new DateFilter());
		
		pipeline.addStep("date", "MM/yyyy");
		
		
		var date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MM/yyyy");
		var formatted = format.format(date);
		
        Object result = pipeline.execute(date);
        Assertions.assertThat(result).isEqualTo(formatted);
    }
}
