package com.condation.cms.core.messaging;

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
import com.condation.cms.api.messaging.Topic;
import com.condation.cms.api.messaging.Listener;
import com.condation.cms.core.utils.MdcScope;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public final class DefaultTopic implements Topic {

	private final List<ListenerHolder> listeners;

	final String name;
	final String siteId;

	record ListenerHolder<T>(Listener<T> listener, Class<T> dataType) {

	}

	;
	
	public DefaultTopic(String name, String siteId) {
		this.name = name;
		this.siteId = siteId;
		listeners = new CopyOnWriteArrayList<>();
	}

	@Override
	public <RT> void subscribe(Listener<RT> listener, Class<RT> dataType) {
		listeners.add(new ListenerHolder<>(listener, dataType));
	}

	@Override
	public <RT> com.condation.cms.api.messaging.Subscription subscribeWithHandle(
			Listener<RT> listener, Class<RT> dataType) {
		var holder = new ListenerHolder<>(listener, dataType);
		listeners.add(holder);
		return () -> listeners.remove(holder);
	}

	@Override
	public void publish(final Object data, Mode mode) {

		listeners.forEach((listener) -> {
			if (Mode.SYNC.equals(mode)) {
				sendMessage(data, listener);
			} else {
				Thread.startVirtualThread(() -> {
					MdcScope.forSite(siteId).run(() -> {
						sendMessage(data, listener);
					});
				});
			}
		});
	}

	private void sendMessage(final Object data, ListenerHolder listener) {
		try {
			String dataString = DefaultMessaging.GSON.toJson(data);
			Object payload = DefaultMessaging.GSON.fromJson(dataString, listener.dataType);
			listener.listener.receive(payload);
		} catch (Exception e) {
			log.error(null, e);
		}
	}
}
