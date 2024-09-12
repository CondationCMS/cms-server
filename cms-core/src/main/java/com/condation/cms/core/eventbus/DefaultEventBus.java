package com.condation.cms.core.eventbus;

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

import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.EventListener;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class DefaultEventBus implements EventBus {

	public final Multimap<Class<? extends Event>, EventListener> listeners;

	public DefaultEventBus() {
		listeners = ArrayListMultimap.create();
	}

	private Multimap<Class<? extends Event>, EventListener> listeners () {
		return ArrayListMultimap.create(listeners);
	}
	
	@Override
	public <T extends Event> void register(Class<T> eventClass, EventListener<T> listener) {
		listeners.put(eventClass, listener);
	}
	
	@Override
	public <T extends Event> void unregister(Class<T> eventClass, EventListener<T> listener) {
		if (listeners.containsKey(eventClass)) {
			listeners.get(eventClass).remove(listener);
		}
	}
	@Override
	public void unregister(EventListener listener) {
		listeners().keySet().forEach(eventClass -> unregister(eventClass, listener));
	}

	@Override
	public <T extends Event> void publish(final T event) {
		listeners().get(event.getClass()).forEach(listener -> {
			Thread.startVirtualThread(() -> {
				try {
					listener.consum(event);
				} catch (Exception e) {
					log.error(null, e);
				}
			});
		});
	}

	@Override
	public <T extends Event> void syncPublish(T event) {
		listeners().get(event.getClass()).forEach(listener -> {
			try {
				listener.consum(event);
			} catch (Exception e) {
				log.error(null, e);
			}
		});
	}
}
