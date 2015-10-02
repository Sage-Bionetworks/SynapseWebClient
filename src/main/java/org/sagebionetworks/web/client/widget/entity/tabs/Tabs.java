package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Tabs implements IsWidget {
	TabsView view;
	CallbackP<Tab> onTabClickedCallback;
	List<Tab> allTabs;
	@Inject
	public Tabs(TabsView view) {
		this.view = view;
		onTabClickedCallback = new CallbackP<Tab>() {
			@Override
			public void invoke(Tab tab) {
				onTabClicked(tab);
			}
		};
		allTabs = new ArrayList<Tab>();
	}
	
	public void clear() {
		view.clear();
		allTabs.clear();
	}
	
	public void addTab(Tab tab) {
		view.addTab(tab);
		allTabs.add(tab);
		//this handles Tab click events
		tab.addTabClickedCallback(onTabClickedCallback);
	}
	
	private void hideAllTabs() {
		for (Tab tab : allTabs) {
			tab.hideTab();
		}
	}
	
	public void showTab(Tab tab) {
		hideAllTabs();
		//and show the tab
		tab.showTab();
	}
	
	public void onTabClicked(Tab tabClicked) {
		showTab(tabClicked);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
