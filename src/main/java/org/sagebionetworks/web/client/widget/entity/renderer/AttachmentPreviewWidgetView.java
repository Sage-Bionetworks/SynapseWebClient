package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface AttachmentPreviewWidgetView extends IsWidget {
  void configure(WikiPageKey wikiKey, String fileName);
}
