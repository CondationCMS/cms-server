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

import com.condation.cms.api.messaging.Topic;
import com.condation.cms.api.messaging.Messaging;
import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.core.messaging.converters.PathConverter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class DefaultMessaging implements Messaging {

	static final Gson GSON;
	static {
		GSON = new GsonBuilder()
				.registerTypeHierarchyAdapter(Path.class, new PathConverter())
				.enableComplexMapKeySerialization()
				.create();
	}
	
	public final Multimap<Class<? extends Event>, EventListener> listeners;
	
	Map<String, DefaultTopic> topics;
	
	@Inject
	public DefaultMessaging () {
		listeners = ArrayListMultimap.create();
		topics = new HashMap<>();
	}

	@Override
	public Topic topic(String name) {
		return topics.computeIfAbsent(name, (topic) -> new DefaultTopic(topic));
	}
}
