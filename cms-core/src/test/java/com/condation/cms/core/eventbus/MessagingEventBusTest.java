package com.condation.cms.core.eventbus;

/*-
 * #%L
 * cms-server
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



import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.eventbus.events.GenericEvent;
import com.condation.cms.core.messaging.DefaultMessaging;
import com.condation.cms.api.messaging.Messaging;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MessagingEventBusTest {
	
	EventBus eventBus;
	Messaging messaging;
	
	@BeforeEach
	public void setup () {
		messaging = new DefaultMessaging();
		eventBus = new MessagingEventBus(messaging);
	}

	@Test
	public void register_event() {
		var genericEvent = new GenericEvent("bla", Map.of("message", "Hello world!"));
		var testListener = new TestListener();
		
		eventBus.register(GenericEvent.class, testListener);
		
		eventBus.publish(genericEvent);
		
		Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> 
			testListener.counter.get() == 1
		);
	}
	
	public static class TestListener implements EventListener<GenericEvent> {

		AtomicInteger counter = new AtomicInteger(0);
		@Override
		public void consum(GenericEvent event) {
			counter.incrementAndGet();
		}
	}
}
