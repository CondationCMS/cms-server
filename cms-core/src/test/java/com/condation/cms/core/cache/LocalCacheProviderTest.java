package com.condation.cms.core.cache;

/*-
 * #%L
 * cms-core
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


import com.condation.cms.core.cache.LocalCacheProvider;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class LocalCacheProviderTest {
	
	CacheManager cachManager = new CacheManager(new LocalCacheProvider());
	
	@Test
	public void cache() {
		ICache<String, String> test1Cache = cachManager.get("test1", 
				new CacheManager.CacheConfig(Long.MAX_VALUE, Duration.ofDays(1)));
		
		test1Cache.put("name", "mr winter");
		Assertions.assertThat(test1Cache.get("name")).isEqualTo("mr winter");
		Assertions.assertThat(test1Cache.contains("name")).isTrue();
	}
	
	@Test
	public void cache_loader() {
		ICache<String, String> test1Cache = cachManager.get("test2", 
				new CacheManager.CacheConfig(Long.MAX_VALUE, Duration.ofDays(1)),
				(key) -> "mr winter");
		
		Assertions.assertThat(test1Cache.get("name")).isEqualTo("mr winter");
		Assertions.assertThat(test1Cache.contains("name")).isTrue();
	}
	
	@Test
	public void test_lifetime() throws InterruptedException {
		ICache<String, String> test1Cache = cachManager.get("test1", 
				new CacheManager.CacheConfig(Long.MAX_VALUE, Duration.ofSeconds(1)));
		
		test1Cache.put("name", "mr winter");
		Assertions.assertThat(test1Cache.get("name")).isEqualTo("mr winter");
		Assertions.assertThat(test1Cache.contains("name")).isTrue();
		Thread.sleep(2000);
		Assertions.assertThat(test1Cache.contains("name")).isFalse();
	}
}
