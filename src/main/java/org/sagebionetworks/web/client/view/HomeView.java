package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface HomeView extends IsWidget {
  void render();

  void refresh();

  void scrollToTop();
}
