package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Tab implements TabView.Presenter {
	TabView view;
	GlobalApplicationState globalAppState;
	Place place;
	List<CallbackP<Tab>> onClickCallbacks;
	
	@Inject
	public Tab(TabView view, GlobalApplicationState globalAppState) {
		this.view = view;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
	}
	
	public void configure(String tabTitle, Widget content) {
		view.configure(tabTitle, content);
		onClickCallbacks = new ArrayList<CallbackP<Tab>>();
	}
	
	public TabListItem getTabListItem() {
		return view.getTabListItem();
	}
	
	public TabPane getTabPane() {
		return view.getTabPane();
	}
	
	public void setPlace(Place place) {
		this.place = place;
	}
	
	public void showTab() {
		globalAppState.pushCurrentPlace(place);
		view.setActive(true);
	}
	
	public void hideTab() {
		view.setActive(false);
	}
	
	public void addTabClickedCallback(CallbackP<Tab> onClickCallback) {
		onClickCallbacks.add(onClickCallback);
	}
	
	@Override
	public void onTabClicked() {
		for (CallbackP<Tab> callbackP : onClickCallbacks) {
			callbackP.invoke(this);
		}
	}
}
