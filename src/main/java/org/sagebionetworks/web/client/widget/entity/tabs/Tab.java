package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

import com.google.gwt.user.client.ui.IsWidget;
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
	
	EntityActionController entityActionController;
	ActionMenuWidget entityActionMenu;
	EntityArea area;
	
	@Inject
	public Tab(TabView view,
			GlobalApplicationState globalAppState,
			SynapseJSNIUtils synapseJSNIUtils,
			GWTWrapper gwt,
			EntityActionController entityActionController,
			ActionMenuWidget entityActionMenu) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.gwt = gwt;
		view.setPresenter(this);
		this.entityActionController = entityActionController;
		this.entityActionMenu = entityActionMenu;
		entityActionMenu.addControllerWidget(entityActionController.asWidget());
		deferredShowTabCallback = new Callback() {
			@Override
			public void invoke() {
				showTab(pushState);
			}
		};
	}

	public void configure(String tabTitle, String helpMarkdown, String helpLink, EntityArea area) {
		view.configure(tabTitle, helpMarkdown, helpLink);
		onClickCallbacks = new ArrayList<CallbackP<Tab>>();
		this.area = area;
	}

	public void setContent(Widget widget) {
		view.setContent(widget);
	}

	public Widget getTabListItem() {
		return view.getTabListItem();
	}

	public void addTabListItemStyle(String style) {
		view.addTabListItemStyle(style);
	}

	public void setTabListItemVisible(boolean visible) {
		view.setTabListItemVisible(visible);
	}

	public boolean isTabListItemVisible() {
		return view.isTabListItemVisible();
	}

	public TabPane getTabPane() {
		return view.getTabPane();
	}

	public boolean isTabPaneVisible() {
		return getTabPane().isVisible();
	}

	public void setEntityNameAndPlace(String entityName, Synapse place) {
		this.place = place;
		this.entityName = entityName;
		updatePageTitle();
		view.updateHref(place);
	}

	public void showTab() {
		showTab(true);
	}

	public void showTab(boolean pushState) {
		this.pushState = pushState;
		if (place == null) {
			// try again later
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
					entityId = " - " + place.getEntityId();
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
	
	public ActionMenuWidget getEntityActionMenu() {
		return entityActionMenu;
	}
	
	public void configureEntityActionController(EntityBundle bundle, boolean isCurrentVersion, String wikiPageKey) {
		entityActionController.configure(entityActionMenu, bundle, isCurrentVersion, wikiPageKey, area);
	}
	
	/**
	 * For testing purposes only
	 * 
	 * @param entityName
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

}
