package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface BookmarkWidgetView extends IsWidget {
  void configure(String bookmarkID, String bookmarkLinkText);
}
