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
import com.condation.cms.test.e2e.CMSServerExtension;
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
public class TemplateTest {


    @Test
    void function_fn_message(Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).contains("<div style=\"color: red\">MESSAGE: Hello ConditionCMS</div>");
    }
    
    @Test
    void component_colored (Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).containsIgnoringWhitespaces("<div style=\"color: red\">COMPONENT(content): This content should be red!</div>");
    }
    
    @Test
    void component_component (Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).contains("<div style=\"color: red\">COMPONENT: its a component</div>");
    }
    
    @Test
    void component_temp_comp (Page page) {
        page.navigate("http://localhost:2020");
        Assertions.assertThat(page.content()).containsIgnoringWhitespaces("""
                                                       rendered: 
                                                       <div>
                                                       	<h5>CondationCMS</h5>
                                                       	<p>
                                                       		a compontent rendered by a template
                                                       	</p>
                                                       </div>
                                                       """);
    }
}
