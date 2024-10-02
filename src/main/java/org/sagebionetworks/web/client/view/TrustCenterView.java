package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface TrustCenterView extends IsWidget {
  void render(String repoOwner, String repoName, String filePath);

  void refresh();

  void scrollToTop();
}
