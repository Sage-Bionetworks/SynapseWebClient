package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.LockedException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidget implements LoginWidgetView.Presenter {

	private LoginWidgetView view;
	private AuthenticationController authenticationController;	
	private UserListener listener;	
	private String openIdActionUrl;
	private String openIdReturnUrl;
	private GlobalApplicationState globalApplicationState;
	private SynapseJSNIUtils synapseJsniUtils;
	
	public static final String LOGIN_PLACE  = "LoginPlace";
	
	@Inject
	public LoginWidget(LoginWidgetView view, AuthenticationController controller, GlobalApplicationState globalApplicationState, SynapseJSNIUtils synapseJsniUtils) {
		this.view = view;
		view.setPresenter(this);
		this.authenticationController = controller;	
		this.globalApplicationState = globalApplicationState;
		this.synapseJsniUtils = synapseJsniUtils;
		openIdActionUrl = WebConstants.OPEN_ID_URI;
		// note, this is now a relative URL
		openIdReturnUrl = synapseJsniUtils.getLocationPath()+synapseJsniUtils.getLocationQueryString()+"#!"+LOGIN_PLACE;
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void setUserListener(UserListener listener){
		this.listener = listener;
	}
	
	@Override
	public void setUsernameAndPassword(final String username, final String password) {		
		authenticationController.loginUser(username, password, new AsyncCallback<UserSessionData>() {
			@Override
			public void onSuccess(UserSessionData userSessionData) {
				clear();
				try {
					fireUserChange(userSessionData);
				} catch (Exception ex) {
					onFailure(ex);
				}
			}

			@Override
			public void onFailure(Throwable caught) {				
				view.clear();
				if(caught instanceof ReadOnlyModeException) {
					view.showError(DisplayConstants.LOGIN_READ_ONLY_MODE);
				} else if(caught instanceof SynapseDownException) {
					view.showError(DisplayConstants.LOGIN_DOWN_MODE);
				} else if(caught instanceof LockedException) {
					view.showError(caught.getMessage());
				} else {
					synapseJsniUtils.consoleError(caught.getMessage());
					view.showAuthenticationFailed();
				}
			}
		});
	}
	
	public void clear() {
		view.clear();
		view.clearUsername();
	}

	// needed?
	private void fireUserChange(UserSessionData user) {
		if (listener != null)
			listener.userChanged(user);
	}
	
	public void setOpenIdActionUrl(String url) {
		this.openIdActionUrl = url;
	}

	public void setOpenIdReturnUrl(String url) {
		this.openIdReturnUrl = url;
	}
	
	@Override
	public String getOpenIdActionUrl() {
		return openIdActionUrl;
	}
	
	@Override
	public String getOpenIdReturnUrl() {
		return openIdReturnUrl;
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
}
