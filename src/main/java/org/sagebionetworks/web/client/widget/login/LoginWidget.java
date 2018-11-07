package org.sagebionetworks.web.client.widget.login;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginWidget implements LoginWidgetView.Presenter {

	public static final String PLEASE_TRY_AGAIN = ". Please try again.";
	private LoginWidgetView view;
	private AuthenticationController authenticationController;	
	private Callback listener;	
	private GlobalApplicationState globalApplicationState;
	private SynapseAlert synAlert;
	
	public static final String LOGIN_PLACE  = "LoginPlace";
	
	@Inject
	public LoginWidget(LoginWidgetView view, 
			AuthenticationController controller, 
			GlobalApplicationState globalApplicationState, 
			SynapseAlert synAlert) {
		this.view = view;
		this.authenticationController = controller;	
		this.globalApplicationState = globalApplicationState;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
	}

	public Widget asWidget() {
		// setPresenter in asWidget() is necessary to connect the singleton view to the current presenter (which contains the current UserListener)
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void setUserListener(Callback listener){
		this.listener = listener;
	}
	
	@Override
	public void setUsernameAndPassword(final String username, final String password) {
		synAlert.clear();
		authenticationController.loginUser(username, password, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile profile) {
				clear();
				fireUserChange();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.clear();
				if (caught instanceof NotFoundException || caught instanceof UnauthorizedException) {
					synAlert.showError(caught.getMessage() + PLEASE_TRY_AGAIN);
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
	private void fireUserChange() {
		if (listener != null) {
			listener.invoke();
		} else {
			globalApplicationState.gotoLastPlace();
		}
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
}
