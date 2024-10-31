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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import com.condation.cms.api.messaging.Listener;
import com.condation.cms.api.messaging.Topic;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DefaultTopicClassLoaderTest {

    private URLClassLoader loader1;
    private URLClassLoader loader2;
    private DefaultTopic topic;

    @BeforeEach
    void setUp() throws Exception {
        URL[] urls = {};  // Leere URL für einen separaten Classloader-Kontext

        // Zwei separate Classloader instanziieren
        loader1 = new URLClassLoader(urls, getClass().getClassLoader());
        loader2 = new URLClassLoader(urls, getClass().getClassLoader());

        // DefaultTopic-Instanz in loader1 erstellen
        Class<?> topicClass = loader1.loadClass("com.condation.cms.core.messaging.DefaultTopic");
        Constructor<?> constructor = topicClass.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        topic = (DefaultTopic) constructor.newInstance("TestTopic");
    }

    @Test
    void testPublishWithDifferentClassLoaders() throws Exception {
        // Listener im ersten Classloader hinzufügen
        Listener<String> listener1 = createListener(loader1, "Listener1");
        topic.subscribe(listener1, String.class);

        // Listener im zweiten Classloader hinzufügen
        Listener<String> listener2 = createListener(loader2, "Listener2");
        topic.subscribe(listener2, String.class);

        // Nachricht senden
        topic.publish("Test Message", Topic.Mode.SYNC);

        // Überprüfen, ob Listener die Nachricht empfangen haben
        // Dies hängt von einer Implementierung des Listeners ab, die das Empfangen der Nachrichten prüfbar macht
        // Eventuell mit Logging oder Zählvariablen in jedem Listener
    }

    private Listener<String> createListener(URLClassLoader loader, String listenerName) throws Exception {
        Class<?> listenerClass = loader.loadClass("com.condation.cms.api.messaging.Listener");
        return new Listener<String>() {
            @Override
            public void receive(String message) {
                log.info("Message received in " + listenerName + ": " + message);
            }
        };
    }
}
