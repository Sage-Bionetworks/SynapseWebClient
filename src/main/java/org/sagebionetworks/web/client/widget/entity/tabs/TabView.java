package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.TabPane;

import com.google.gwt.user.client.ui.Widget;

public interface TabView {
	void setPresenter(Presenter presenter);
	void configure(String tabTitle, Widget content, String helpMarkdown, String helpLink);
	Widget getTabListItem();
	void setTabListItemVisible(boolean visible);
	TabPane getTabPane();
	void setActive(boolean isActive);
	boolean isActive();
	public interface Presenter {
		void onTabClicked();
	}
}
