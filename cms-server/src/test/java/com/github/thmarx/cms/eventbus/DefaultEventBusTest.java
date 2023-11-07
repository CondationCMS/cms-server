/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.github.thmarx.cms.eventbus;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class DefaultEventBusTest {
	
	static EventBus eventBus;
	
	@BeforeAll
	static void setup () {
		eventBus = new DefaultEventBus();
	}

	@Test
	public void testSomeMethod() {
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
