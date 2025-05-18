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
    public @interface TestMarker {}

    static class TestTarget {

        @TestMarker
        public String greet(String name) {
			return "Hello " + name;
		}

        @TestMarker
        public String wrongParameterType(Integer i) {
            return "Number " + i;
        }

        @TestMarker
        public void wrongReturnType(String name) {}

        @TestMarker
        private String privateMethod(String name) {
            return "Private " + name;
        }

        public String notAnnotated(String name) {
            return "Ignored";
        }
    }

    @Test
    void shouldReturnOnlyPublicAnnotatedMethodsWithCorrectSignature() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> results =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        assertThat(results).hasSize(1);
        String result = results.get(0).invoke("World");
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void shouldIgnorePrivateMethods() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> results =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        boolean includesPrivate = results.stream()
                .anyMatch(ann -> ann.annotation().annotationType().equals(TestMarker.class)
                        && ann.invoke("test").startsWith("Private"));

        assertThat(includesPrivate).isFalse();
    }

    @Test
    void shouldIgnoreWrongParameterTypes() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> results =
                AnnotationsUtil.process(target, TestMarker.class, List.of(Double.class), String.class);

        assertThat(results).isEmpty();
    }

    @Test
    void shouldIgnoreMethodsWithWrongReturnType() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> results =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        assertThat(results).noneMatch(ann -> ann.annotation().annotationType().equals(TestMarker.class)
                && ann.invoke("Test") == null);
    }


    @Test
    void shouldIgnoreNonAnnotatedMethods() {
        TestTarget target = new TestTarget();

        List<CMSAnnotation<TestMarker, String>> results =
                AnnotationsUtil.process(target, TestMarker.class, List.of(String.class), String.class);

        assertThat(results).allSatisfy(ann -> assertThat(ann.annotation()).isInstanceOf(TestMarker.class));
    }
}
