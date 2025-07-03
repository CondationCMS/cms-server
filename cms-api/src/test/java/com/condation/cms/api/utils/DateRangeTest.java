package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeTest {

    @Test
    void should_return_true_when_both_dates_are_null() {
        assertThat(DateRange.isNowWithin(null, null)).isTrue();
    }

    @Test
    void should_return_true_when_now_is_after_from_and_to_is_null() {
        Date from = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        assertThat(DateRange.isNowWithin(from, null)).isTrue();
    }

    @Test
    void should_return_false_when_now_is_before_from_and_to_is_null() {
        Date from = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        assertThat(DateRange.isNowWithin(from, null)).isFalse();
    }

    @Test
    void should_return_true_when_now_is_before_to_and_from_is_null() {
        Date to = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        assertThat(DateRange.isNowWithin(null, to)).isTrue();
    }

    @Test
    void should_return_false_when_now_is_after_to_and_from_is_null() {
        Date to = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        assertThat(DateRange.isNowWithin(null, to)).isFalse();
    }

    @Test
    void should_return_true_when_now_is_between_from_and_to() {
        Date from = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
        Date to = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        assertThat(DateRange.isNowWithin(from, to)).isTrue();
    }

    @Test
    void should_return_false_when_now_is_before_from_even_if_to_is_in_future() {
        Date from = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));
        Date to = Date.from(Instant.now().plus(2, ChronoUnit.HOURS));
        assertThat(DateRange.isNowWithin(from, to)).isFalse();
    }

    @Test
    void should_return_false_when_now_is_after_to_even_if_from_is_in_past() {
        Date from = Date.from(Instant.now().minus(2, ChronoUnit.HOURS));
        Date to = Date.from(Instant.now().minus(30, ChronoUnit.MINUTES));
        assertThat(DateRange.isNowWithin(from, to)).isFalse();
    }

    @Test
    void should_return_true_when_now_equals_from_and_to_is_null() {
        Date now = Date.from(Instant.now());
        assertThat(DateRange.isNowWithin(now, null)).isTrue();
    }

    @Test
    void should_return_false_when_now_equals_to() {
        Date now = Date.from(Instant.now());
        assertThat(DateRange.isNowWithin(null, now)).isFalse();
    }
}
