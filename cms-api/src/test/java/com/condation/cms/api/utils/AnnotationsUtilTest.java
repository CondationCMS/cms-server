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

import com.condation.cms.api.exceptions.AnnotationExecutionException;
import com.condation.cms.api.utils.AnnotationsUtil.CMSAnnotation;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class AnnotationsUtilTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestMarker {}

    static class TestTarget {
        @TestMarker
        public String greet(String name) {
            return "Hello " + name;
        }

        @TestMarker
        public void wrongReturnType() {}

        public String notAnnotated(String name) {
            return "Ignored";
        }

        @TestMarker
        public String wrongParameters(int id) {
            return "Number " + id;
        }

        @TestMarker
        private String privateMethod(String name) {
            return "Private " + name;
        }

        @TestMarker
        public String throwsException(String name) {
            throw new RuntimeException("expected failure");
        }
    }

    @Test
    void shouldReturnSingleMatchingMethod() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> annotations =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        assertThat(annotations).hasSize(3);

        String result = annotations.get(0).invoke("World");
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void shouldReturnEmptyListIfNoValidSignatures() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> annotations =
                AnnotationsUtil.process(target, TestMarker.class, List.of(Integer.class), String.class);

        assertThat(annotations).isEmpty();
    }

    @Test
    void shouldIgnoreNonAnnotatedMethods() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> annotations =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        boolean anyMatch = annotations.stream()
                .anyMatch(ann -> ann.annotation().annotationType() != TestMarker.class);

        assertThat(anyMatch).isFalse();
    }

    @Test
    void shouldInvokePrivateMethodIfAccessible() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> annotations =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        boolean includesPrivate = annotations.stream()
                .anyMatch(ann -> ann.invoke("test").startsWith("Private"));

        // Expect false, since private methods aren't accessible by default
        assertThat(includesPrivate).isFalse();
    }

    @Test
    void shouldThrowExceptionOnMethodFailure() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> annotations =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        boolean found = false;
        for (CMSAnnotation<TestMarker, String> ann : annotations) {
            if (ann.annotation().annotationType() == TestMarker.class) {
                try {
                    ann.invoke("fail");
                } catch (AnnotationExecutionException e) {
                    found = true;
                    assertThat(e).hasMessageContaining("expected failure");
                }
            }
        }

        assertThat(found).isTrue();
    }

    @Test
    void shouldRejectMethodWithWrongReturnType() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> annotations =
                AnnotationsUtil.process(target, TestMarker.class, List.of(), String.class);

        boolean anyVoid = annotations.stream()
                .anyMatch(ann -> ann.annotation().annotationType() == TestMarker.class &&
                                 ann.invoke() == null);

        assertThat(anyVoid).isFalse();
    }
}
