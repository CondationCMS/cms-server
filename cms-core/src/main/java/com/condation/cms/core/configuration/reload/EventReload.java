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

import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventReload<T extends Event> implements ReloadStrategy {

	private final EventBus eventBus;
	private final Class<T> eventClass;

	@Override
	public void register(IConfiguration configuration) {
		if (eventBus == null) {
			return;
		}
		eventBus.register(eventClass, event -> {
			log.trace("reload of config %s triggered by event %s", configuration.id(), eventClass.getSimpleName());
			configuration.reload();
		});
	}
}
