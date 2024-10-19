package com.condation.cms.configuration.reload;

import com.condation.cms.api.eventbus.Event;

/**
 *
 * @author t.marx
 */
public record ReloadEvent(String name) implements Event {

}
