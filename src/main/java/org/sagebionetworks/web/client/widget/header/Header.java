package org.sagebionetworks.web.client.widget.header;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Lookup;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Header implements HeaderView.Presenter {

	
	public static enum MenuItems {
		DATASETS, TOOLS, NETWORKS, PEOPLE, PROJECTS
	}
	
	private HeaderView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public Header(HeaderView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	public void setMenuItemActive(MenuItems menuItem) {
		view.setMenuItemActive(menuItem);
	}

	public void removeMenuItemActive(MenuItems menuItem) {
		view.removeMenuItemActive(menuItem);
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void setSearchVisible(boolean searchVisible) {
		view.setSearchVisible(searchVisible);
	}
	
	public void refresh() {
		view.refresh();
		view.setSearchVisible(true);
	}

	@Override
	public UserSessionData getUser() {
		return authenticationController.getLoggedInUser(); 
	}

	@Override
	public void lookupId(String synapseId) {
		globalApplicationState.getPlaceChanger().goTo(new Lookup(synapseId));
	}
	
}
