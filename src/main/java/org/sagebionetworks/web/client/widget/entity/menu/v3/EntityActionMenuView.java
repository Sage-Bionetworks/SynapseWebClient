package org.sagebionetworks.web.client.widget.entity.menu.v3;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityActionMenuView extends IsWidget {
  void configure(EntityActionMenuProps props);

  void addControllerWidget(IsWidget w);

  void setIsLoading(boolean isLoading);
}
