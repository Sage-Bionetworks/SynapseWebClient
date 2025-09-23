package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityCitation extends IsWidget {
  void configure(String projectId, String entityId, Double version);
}
