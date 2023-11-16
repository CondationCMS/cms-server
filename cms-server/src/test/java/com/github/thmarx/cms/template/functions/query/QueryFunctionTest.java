package com.github.thmarx.cms.template.functions.query;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 * @author t.marx
 */
public class QueryFunctionTest {
	

	@Test
	public void testSomeMethod() {
		
		QueryFunction query = new QueryFunction(null);
		
		Assertions.assertThat(query.toUrl("index.md")).isEqualTo("/");
		Assertions.assertThat(query.toUrl("test.md")).isEqualTo("/test");
		Assertions.assertThat(query.toUrl("demo/test.md")).isEqualTo("/demo/test");
		Assertions.assertThat(query.toUrl("demo/index.md")).isEqualTo("/demo");
		
	}
	
}
