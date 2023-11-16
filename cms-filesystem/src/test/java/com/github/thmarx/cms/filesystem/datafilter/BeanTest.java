package com.github.thmarx.cms.filesystem.datafilter;

/*-
 * #%L
 * cms-filesystem
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

import com.github.thmarx.cms.filesystem.datafilter.dimension.Dimension;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;


public class BeanTest extends AbstractTest {

	@Test
	public void noneUniqueName () {
		DataFilter<Person> persons = DataFilter.builder(Person.class).build();
		
		persons.add(new Person("thorsten", 25));
		persons.add(new Person("thorsten", 26));
		
		Collection<Person> namedThorsten = persons.filter((Person target) -> {
			if ("thorsten".equals(target.name)) {
				return true;
			}
			return false;
		});
		
		assertThat(namedThorsten.size()).isEqualTo(2);
		
		
		Dimension<String, Person> byName = persons.dimension("test", (Person type) -> type.name, String.class);
		
		assertThat(byName.filterExact("thorsten").size()).isEqualTo(2);
	}
}
