package com.condation.cms.core.messaging;

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

import com.condation.cms.api.messaging.Messaging;
import com.condation.cms.api.messaging.Topic;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class DefaultMessagingTest {
	
	Messaging messaging;
	
	@BeforeEach
	void setup () {
		messaging = new DefaultMessaging("test-site");
	}

	@Test
	public void testSomeMethod() {
		
		AtomicBoolean received = new AtomicBoolean(false);
		messaging.topic("test/test").subscribe((data) -> {
			System.out.println(data.value);
			received.set(true);
		}, AnotherObject.class);
		
		messaging.topic("test/test").publish(new DataObject("Hello CondationCMS!"), Topic.Mode.SYNC);
		
		Awaitility.await()
				.atMost(Duration.ofSeconds(1))
				.until(received::get);
	}
	
	public static record DataObject (String value){}
	public static record AnotherObject (String value){}
}
