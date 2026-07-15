package com.condation.cms.core.configuration.reload;

/*-
 * #%L
 * CMS Core
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

import com.condation.cms.api.eventbus.events.ReloadTaxonomyConfig;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import com.condation.cms.core.eventbus.DefaultEventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReloadStrategyTest {

	@Mock
	private IConfiguration configuration;

	@Test
	public void eventReloadReloadsConfigurationOnEvent() {
		var eventBus = new DefaultEventBus();
		Mockito.when(configuration.id()).thenReturn("taxonomy");

		new EventReload<>(eventBus, ReloadTaxonomyConfig.class).register(configuration);
		eventBus.syncPublish(new ReloadTaxonomyConfig());

		Mockito.verify(configuration).reload();
	}

	@Test
	public void compositeReloadRegistersAllStrategies() {
		ReloadStrategy first = Mockito.mock(ReloadStrategy.class);
		ReloadStrategy second = Mockito.mock(ReloadStrategy.class);

		new CompositeReload(first, second).register(configuration);

		Mockito.verify(first).register(configuration);
		Mockito.verify(second).register(configuration);
	}
}
