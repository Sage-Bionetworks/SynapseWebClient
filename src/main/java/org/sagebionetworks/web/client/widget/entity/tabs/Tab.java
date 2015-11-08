package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Tab implements TabView.Presenter {
	TabView view;
	GlobalApplicationState globalAppState;
	SynapseJSNIUtils synapseJSNIUtils;
	
	Synapse place;
	String entityName;
	List<CallbackP<Tab>> onClickCallbacks;
	
	@Inject
	public Tab(TabView view, GlobalApplicationState globalAppState, SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
	}
	
	public void configure(String tabTitle, Widget content) {
		view.configure(tabTitle, content);
		onClickCallbacks = new ArrayList<CallbackP<Tab>>();
	}
	
	public TabListItem getTabListItem() {
		return view.getTabListItem();
	}
	
	public void setTabListItemVisible(boolean visible) {
		view.setTabListItemVisible(visible);
	}
	
	public TabPane getTabPane() {
		return view.getTabPane();
	}
	
	public void setEntityNameAndPlace(String entityName, Synapse place) {
		this.place = place;
		this.entityName = entityName;
		updatePageTitle();
	}
	
	public void showTab() {
		globalAppState.pushCurrentPlace(place);
		view.setActive(true);
		updatePageTitle();
	}
	
	public void updatePageTitle() {
		if (view.isActive()) {
			if (entityName != null) {
				String entityId = "";
				if (place != null) {
					entityId = " - " +  place.getEntityId();
				}
				synapseJSNIUtils.setPageTitle(entityName + entityId);
			}
		}
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
	
	/**
	 * For testing purposes only
	 * @param entityName
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
}
