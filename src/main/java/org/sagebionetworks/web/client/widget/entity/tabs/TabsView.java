package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;

public interface TabsView extends IsWidget {
	void addTab(Tab tab);

	void clear();

	void setNavTabsVisible(boolean visible);
}
