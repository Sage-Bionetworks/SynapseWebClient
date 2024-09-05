package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.UIObject;
import org.sagebionetworks.repo.model.EntityType;

public interface EntityTypeIcon extends HasVisibility, IsWidget {
  void configure(EntityType type);
  com.google.gwt.user.client.Element getElement();
}
