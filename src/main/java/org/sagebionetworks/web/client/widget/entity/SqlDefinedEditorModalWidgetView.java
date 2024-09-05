package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;

public interface SqlDefinedEditorModalWidgetView extends IsWidget {
  void renderComponent(SqlDefinedTableEditorModalProps props);
}
