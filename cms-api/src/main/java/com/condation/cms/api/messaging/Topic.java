package com.condation.cms.api.messaging;

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

/**
 *
 * @author t.marx
 */
public interface Topic {

	public static enum Mode {
		ASYNC, SYNC;
	}
	
	/**
	 * Subscribe to a topic.
	 * 
	 * @param <RT>
	 * @param listener
	 * @param dataType 
	 */
	<RT> void subscribe(Listener<RT> listener, Class<RT> dataType);
	
	/**
	 * Publish a message on the topic with mode.
	 * 
	 * @param object
	 * @param mode 
	 */
	void publish(Object object, Mode mode);
	
	/**
	 * Publish a message on the topic with default mode: Mode.ASYNC
	 * 
	 * @param object
	 */
	default void publish (Object object) {
		publish(object, Topic.Mode.ASYNC);
	}
}
