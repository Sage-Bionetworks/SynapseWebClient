package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.HelpWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TabViewImpl implements TabView {
	
	@UiField
	Anchor tabItem;
	@UiField
	TabPane tabPane;
	
	HelpWidget helpWidget;
	@UiField
	ListItem tabListItem;
	@UiField
	Div contentDiv;
	boolean isActive = false;
	public interface TabViewImplUiBinder extends UiBinder<Widget, TabViewImpl> {}
	Presenter presenter;
	Widget widget;
	ClickHandler tabClickedHandler;
	
	@Inject
	public TabViewImpl(HelpWidget helpWidget) {
		//empty constructor, you can include this widget in the ui xml
		TabViewImplUiBinder binder = GWT.create(TabViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
		this.helpWidget = helpWidget;
		tabClickedHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onTabClicked();
			}
		};
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void configure(String tabTitle, Widget content, String helpMarkdown, String helpLink) {
		contentDiv.clear();
		contentDiv.add(content);
		helpWidget.setHelpMarkdown(helpMarkdown);
		helpWidget.setHref(helpLink);
		helpWidget.setPlacement(Placement.BOTTOM);
		tabItem.clear();
		FocusPanel panel = new FocusPanel();
		panel.add(new InlineHTML(tabTitle));
		panel.addClickHandler(tabClickedHandler);
		panel.addStyleName("margin-right-5 displayInline");
		tabItem.add(panel);
		tabItem.add(helpWidget.asWidget());
	}
	
	@Override
	public Widget getTabListItem() {
		return tabListItem;
	}
	@Override
	public void setTabListItemVisible(boolean visible) {
		tabListItem.setVisible(visible);
	}
	
	@Override
	public TabPane getTabPane() {
		return tabPane;
	}
	
	@Override
	public void setActive(boolean isActive) {
		this.isActive = isActive;
		if (isActive) {
			tabListItem.addStyleName("active");	
		} else {
			tabListItem.removeStyleName("active");
		}
		
		tabPane.setVisible(isActive);
	}
	@Override
	public boolean isActive() {
		return isActive;
	}
}
