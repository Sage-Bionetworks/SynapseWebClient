package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;

public interface TabsView extends IsWidget {
	void addTab(Tab tab);
	void clear();
}
