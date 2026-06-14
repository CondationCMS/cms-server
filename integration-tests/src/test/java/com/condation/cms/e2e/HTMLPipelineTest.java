package com.condation.cms.e2e;

/*-
 * #%L
 * integration-tests
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author thmar
 */
@ExtendWith({CMSServerExtension.class})
@UsePlaywright
public class HTMLPipelineTest {


    @Test
    void header(Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).contains("<!-- this comes into the header -->");
    }
    
    @Test
    void footer (Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).contains("<!-- this comes into the footer -->");
    }
    
    @Test
    void say_hello (Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).contains("<p>Hello, CondationCMS</p>");
    }
}
