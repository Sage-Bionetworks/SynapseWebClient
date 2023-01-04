package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseView;

public interface ProjectTitleBarView extends IsWidget, SynapseView {
  void setFavoritesWidget(Widget favoritesWidget);

  void setFavoritesWidgetVisible(boolean visible);

  void setTitle(String name);

  void setEntityType(EntityType entityType);

  void setActionMenu(IsWidget w);
}
