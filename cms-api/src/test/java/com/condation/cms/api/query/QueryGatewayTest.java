package com.condation.cms.api.query;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import com.condation.cms.api.query.Query;
import com.condation.cms.api.query.QueryGateway;
import com.condation.cms.api.query.QueryHandler;
import com.condation.cms.api.query.QueryProvider;
import com.google.auto.service.AutoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 *
 * @author t.marx
 */
public class QueryGatewayTest {
	
	private static QueryGateway gateway = new QueryGateway();

	@BeforeAll
	public static void setup () {
		gateway.register(CustomQuery.class, new CustomQueryHandler());
		gateway.init();
	}
			
	@Test
	public void testSomeMethod() {
		String message = gateway.execute(new CustomQuery("cms"));
		System.out.println(message);
	}
	
	@Test
	public void testSecondQuery() {
		String message = gateway.execute(new SecondQueryProvider.SecondQuery("coder"));
		Assertions.assertThat(message).isEqualTo("hello coder");
	}

	public static record CustomQuery(String name) implements Query<String> {
		
	}
	
	public static class CustomQueryHandler implements QueryHandler<CustomQuery, String> {
		@Override
		public String handle(CustomQuery query) {
			return "hello " + query.name();
		}
		
	}
	
	@AutoService(QueryProvider.class)
	public static class SecondQueryProvider implements QueryProvider<SecondQueryProvider.SecondQuery, String> {

		@Override
		public Class<? extends Query<String>> queryClass() {
			return SecondQuery.class;
		}

		@Override
		public QueryHandler<SecondQuery, String> handler() {
			return (query) -> {
				return "hello " + query.name();
			};
		}
		
		public static record SecondQuery(String name) implements Query<String> {
			
		}
	}
}
