package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface ButtonLinkConfigView extends IsWidget, WidgetEditorView {
  void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor);

  String getLinkUrl();

  String getName();

  void setIsHighlightButtonStyle(boolean isHighlight);

  boolean isHighlightButtonStyle();

  String getAlignment();
}
