package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.inject.Inject;

public class Tabs {
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
		tab.setTabClickedCallback(onTabClickedCallback);
	}
	
	public void onTabClicked(Tab tabClicked) {
		//hide all tabs
		for (Tab tab : allTabs) {
			tab.hideTab();
		}
		//and show the tab that was clicked
		tabClicked.showTab();
	}
	
}
