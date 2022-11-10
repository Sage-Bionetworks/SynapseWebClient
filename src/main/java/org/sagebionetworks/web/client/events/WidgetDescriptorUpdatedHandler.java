package org.sagebionetworks.web.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface WidgetDescriptorUpdatedHandler extends EventHandler {
  void onUpdate(WidgetDescriptorUpdatedEvent event);
}
