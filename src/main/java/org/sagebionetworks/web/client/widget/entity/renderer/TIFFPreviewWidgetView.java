package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface TIFFPreviewWidgetView extends IsWidget {
  void configure(String url);
  void showError(String error);
}
