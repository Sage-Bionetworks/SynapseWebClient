package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidget implements LoginWidgetView.Presenter {

	private LoginWidgetView view;
	private AuthenticationController authenticationController;	
	private UserListener listener;	
	private GlobalApplicationState globalApplicationState;
	private SynapseAlert synAlert;
	
	public static final String LOGIN_PLACE  = "LoginPlace";
	
	@Inject
	public LoginWidget(LoginWidgetView view, 
			AuthenticationController controller, 
			GlobalApplicationState globalApplicationState, 
			SynapseAlert synAlert) {
		this.view = view;
		view.setPresenter(this);
		this.authenticationController = controller;	
		this.globalApplicationState = globalApplicationState;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
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
					if (!userSessionData.getSession().getAcceptsTermsOfUse()) {
						globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.SHOW_TOU));
					} else {
						fireUserChange(userSessionData);	
					}
				} catch (Exception ex) {
					onFailure(ex);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.clear();
				if (caught instanceof NotFoundException) {
					view.showAuthenticationFailed();
				} else {
					synAlert.handleException(caught);	
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

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
}
