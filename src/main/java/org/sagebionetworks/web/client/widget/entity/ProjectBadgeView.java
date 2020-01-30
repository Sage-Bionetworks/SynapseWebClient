package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

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
