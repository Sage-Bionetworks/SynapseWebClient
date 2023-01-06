package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface HtmlPreviewView extends IsWidget {
  void setSynAlert(IsWidget w);

  void setLoadingVisible(boolean visible);

  void configure(String createdBy, String rawHtml);
}
