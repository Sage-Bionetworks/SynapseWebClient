package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface EntityDeletedHandler extends EventHandler {

	void onDeleteSuccess(EntityDeletedEvent event);
}
