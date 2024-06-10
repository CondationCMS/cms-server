package com.github.thmarx.cms.eventbus;

/*-
 * #%L
 * cms-server
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

import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.EventListener;
import com.github.thmarx.cms.api.eventbus.events.GenericEvent;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class DefaultEventBusTest {
	
	EventBus eventBus;
	
	@BeforeEach
	public void setup () {
		eventBus = new DefaultEventBus();
	}

	@Test
	public void register_unregister_event() {
		var genericEvent = new GenericEvent("bla", Map.of("message", "Hello world!"));
		var testListener = new TestListener();
		
		eventBus.register(GenericEvent.class, testListener);
		
		eventBus.publish(genericEvent);
		
		Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> 
			testListener.counter.get() == 1
		);
		
		eventBus.unregister(GenericEvent.class, testListener);
		
		eventBus.publish(genericEvent);
		
		Awaitility.await().atLeast(Duration.ofSeconds(2));
		
		Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> 
			testListener.counter.get() == 1
		);
	}
	
	@Test
	public void register_unregister_listener() {
		var genericEvent = new GenericEvent("bla", Map.of("message", "Hello world!"));
		var testListener = new TestListener();
		
		eventBus.register(GenericEvent.class, testListener);
		
		eventBus.publish(genericEvent);
		
		Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> 
			testListener.counter.get() == 1
		);
		
		eventBus.unregister(testListener);
		
		eventBus.publish(genericEvent);
		
		Awaitility.await().atLeast(Duration.ofSeconds(2));
		
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
