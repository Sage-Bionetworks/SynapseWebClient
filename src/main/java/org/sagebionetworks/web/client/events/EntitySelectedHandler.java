package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface EntitySelectedHandler extends EventHandler {

	void onSelection(EntitySelectedEvent event);
}
