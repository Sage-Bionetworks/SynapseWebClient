package org.sagebionetworks.web.client.widget.header;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.Lookup;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	private UserAccountServiceAsync userAccountService;
	
	@Inject
	public Header(HeaderView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, UserAccountServiceAsync userAccountService) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.userAccountService = userAccountService;
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
	
	@Override
	public void getSupportHRef(final AsyncCallback<String> callback) {
		try {
			if (getUser() == null){
				callback.onSuccess("http://"+DisplayUtils.SUPPORT_URL);
			}
			else {
				userAccountService.getFastPassSupportUrl(new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						if (result != null && result.length()>0)
							callback.onSuccess("http://support.sagebase.org/sagebase?fastpass="+URL.encodeQueryString(result));
						else
							callback.onSuccess("http://"+DisplayUtils.SUPPORT_URL);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						//failed, just go
						callback.onSuccess("http://"+DisplayUtils.SUPPORT_URL);
					}
				});
			}
		} catch (RestServiceException e) {
			//if it fails, go to the support site without the fastpass url?
			callback.onSuccess("http://"+DisplayUtils.SUPPORT_URL);
		}
	}
}
