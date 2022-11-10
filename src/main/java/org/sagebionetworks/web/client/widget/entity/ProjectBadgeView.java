package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import java.util.Date;

public interface ProjectBadgeView extends IsWidget {
  void setLastActivityText(String text);

  void setLastActivityVisible(boolean isVisible);

  String getSimpleDateString(Date date);

  void setFavoritesWidget(Widget widget);

  boolean isAttached();

  void addStyleName(String style);

  void configure(String projectName, String projectId);

  void setTooltip(String tooltip);

  void addClickHandler(ClickHandler clickHandler);
}
