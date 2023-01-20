package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.CreatedByModifiedByProps;
import org.sagebionetworks.web.client.jsinterop.ReferenceJsObject;

public interface ModifiedCreatedByWidgetView extends IsWidget {
  void setProps(CreatedByModifiedByProps props);

  void setVisible(boolean b);
}
