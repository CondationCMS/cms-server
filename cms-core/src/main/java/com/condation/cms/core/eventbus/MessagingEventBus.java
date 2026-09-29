package com.condation.cms.core.eventbus;

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
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.messaging.Messaging;
import com.condation.cms.api.messaging.Topic;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class MessagingEventBus implements EventBus {

	private final Messaging messaging;

	public MessagingEventBus(final Messaging messaging) {
		this.messaging = messaging;
	}

	@Override
	public <T extends Event> void register(Class<T> eventClass, EventListener<T> listener) {
		messaging.topic(eventClass.getName()).subscribe((data) -> {
			listener.consum(data);
		}, eventClass);
	}

	@Override
	public <T extends Event> void publish(final T event) {
		messaging.topic(event.getClass().getName()).publish(event);
	}

	@Override
	public <T extends Event> void syncPublish(T event) {
		messaging.topic(event.getClass().getName()).publish(event, Topic.Mode.SYNC);
	}
}
