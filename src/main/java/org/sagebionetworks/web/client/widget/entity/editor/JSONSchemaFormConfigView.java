package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.WidgetEditorView;

public interface JSONSchemaFormConfigView extends IsWidget, WidgetEditorView {
  void setSchemaUrl(String schemaUrl);

  String getSchemaUrl();

  void setUiSchemaUrl(String uiSchemaUrl);

  String getUiSchemaUrl();

  void setPostUrl(String postUrl);

  String getPostUrl();
}
