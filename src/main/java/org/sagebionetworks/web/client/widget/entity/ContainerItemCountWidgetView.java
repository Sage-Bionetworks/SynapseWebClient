package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface ContainerItemCountWidgetView extends IsWidget {
	void showCount(Long count);

	void hide();

	void clear();
}
