package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface AttachmentSelectedHandler extends EventHandler {

	void onAttachmentSelected(AttachmentSelectedEvent event);
}
