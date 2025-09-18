package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface GridPageView extends IsWidget {
  public void setTitle(String title);

  public void render(String sessionId);
}
