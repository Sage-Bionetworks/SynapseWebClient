package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabContent;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class TabsViewImpl implements TabsView {
	@UiField
	NavTabs navTabs;
	@UiField
	TabContent tabContent;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, TabsViewImpl> {
	}

	Widget widget;

	public TabsViewImpl() {
		// empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void addTab(Tab tab) {
		navTabs.add(tab.getTabListItem());
		tabContent.add(tab.getTabPane());
	}

	@Override
	public void clear() {
		navTabs.clear();
		tabContent.clear();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setNavTabsVisible(boolean visible) {
		navTabs.setVisible(visible);
	}
}
