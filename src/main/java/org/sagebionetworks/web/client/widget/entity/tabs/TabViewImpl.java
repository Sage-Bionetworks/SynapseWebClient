package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class TabViewImpl implements TabView {
	
	@UiField
	TabListItem tabItem;
	@UiField
	TabPane tabPane;
	public interface TabViewImplUiBinder extends UiBinder<Widget, TabViewImpl> {}
	Presenter presenter;
	Widget widget;
	
	public TabViewImpl() {
		//empty constructor, you can include this widget in the ui xml
		TabViewImplUiBinder binder = GWT.create(TabViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		tabItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onTabClicked();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void configure(String tabTitle, Widget content) {
		tabItem.setText(tabTitle);
		tabPane.clear();
		tabPane.add(content);
	}
	
	@Override
	public TabListItem getTabListItem() {
		return tabItem;
	}
	
	@Override
	public TabPane getTabPane() {
		return tabPane;
	}
	
	@Override
	public void setActive(boolean isActive) {
		tabItem.setActive(isActive);
		tabPane.setVisible(isActive);
	}
	
}
