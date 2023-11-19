package com.github.thmarx.cms.filesystem.datafilter;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
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
