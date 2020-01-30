package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.web.client.place.Synapse;
import com.google.gwt.user.client.ui.Widget;

public interface TabView {
	void setPresenter(Presenter presenter);

	void updateHref(Synapse place);

	void configure(String tabTitle, String helpMarkdown, String helpLink);

	void setContent(Widget content);

	Widget getTabListItem();

	void setTabListItemVisible(boolean visible);

	boolean isTabListItemVisible();

	void addTabListItemStyle(String style);

	TabPane getTabPane();

	void setActive(boolean isActive);

	boolean isActive();

	public interface Presenter {
		void onTabClicked();
	}
}
