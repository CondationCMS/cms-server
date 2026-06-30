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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author thmar
 */
@ExtendWith(CMSServerExtension.class)
public class RoutesTest {
	
	@Test
    void test_extension_route() throws Exception {
        var response = HttpUtil.fetchText("http://localhost:2020/ext-route");
		Assertions.assertThat(response).isEqualTo("Hello from an extension");
    }
	
	@Test
	void test_module_route () throws Exception {
		var response = HttpUtil.fetchText("http://localhost:2020/world1");
		Assertions.assertThat(response).isEqualTo("Hello world1!");
	}
}
