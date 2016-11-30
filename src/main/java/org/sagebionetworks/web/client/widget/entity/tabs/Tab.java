package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Tab implements TabView.Presenter {
	TabView view;
	GlobalApplicationState globalAppState;
	SynapseJSNIUtils synapseJSNIUtils;
	
	Synapse place;
	String entityName;
	List<CallbackP<Tab>> onClickCallbacks;
	boolean isContentStale;
	GWTWrapper gwt;
	Callback deferredShowTabCallback;
	boolean pushState;
	@Inject
	public Tab(TabView view, GlobalApplicationState globalAppState, SynapseJSNIUtils synapseJSNIUtils, GWTWrapper gwt) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.gwt = gwt;
		view.setPresenter(this);
		deferredShowTabCallback = new Callback() {
			@Override
			public void invoke() {
				showTab(pushState);
			}
		};
	}
	
	public void configure(String tabTitle, Widget content, String helpMarkdown, String helpLink) {
		view.configure(tabTitle, content, helpMarkdown, helpLink);
		onClickCallbacks = new ArrayList<CallbackP<Tab>>();
	}
	
	public Widget getTabListItem() {
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
		showTab(true);
	}
	
	public void showTab(boolean pushState) {
		this.pushState = pushState;
		if (place == null) {
			//try again later
			gwt.scheduleExecution(deferredShowTabCallback, 200);
			return;
		}
		if (pushState) {
			globalAppState.pushCurrentPlace(place);	
		} else {
			globalAppState.replaceCurrentPlace(place);
		}
		
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
		onClickCallbacks.add(0, onClickCallback);
	}
	
	@Override
	public void onTabClicked() {
		for (CallbackP<Tab> callbackP : onClickCallbacks) {
			callbackP.invoke(this);
		}
	}
	
	public boolean isContentStale() {
		return isContentStale;
	}

	public void setContentStale(boolean isContentStale) {
		this.isContentStale = isContentStale;
	}

	/**
	 * For testing purposes only
	 * @param entityName
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
}
