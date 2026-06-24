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
import com.condation.cms.test.e2e.HttpUtil;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author thmar
 */
@UsePlaywright
@ExtendWith(CMSServerExtension.class)
public class ExtensionsTest {
	
	
	
	@Test
	void test_template_function_node_paramter (Page page) throws Exception {
		page.navigate("http://localhost:2020");
        Assertions.assertThat(page.locator("body").innerHTML()).contains("<div>TITLE: Startpage</div>");
	}
}
