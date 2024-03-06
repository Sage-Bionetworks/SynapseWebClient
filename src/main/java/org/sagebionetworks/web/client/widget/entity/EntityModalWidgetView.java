package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.EntityModalProps;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;

public interface EntityModalWidgetView extends IsWidget {
  void renderComponent(EntityModalProps props);
}
