package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class SynapseAlertImpl implements SynapseAlert, SynapseAlertView.Presenter  {
	GlobalApplicationState globalApplicationState;
	AuthenticationController authController;
	SynapseClientAsync synapseClient;
	GWTWrapper gwt;
	SynapseAlertView view;
	Throwable ex;
	String entityId;
	
	@Inject
	public SynapseAlertImpl(
			SynapseAlertView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authController,
			SynapseClientAsync synapseClient,
			GWTWrapper gwt
			) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authController = authController;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		view.setPresenter(this);
		view.clearState();
	}

	@Override
	public void handleException(Throwable ex) {
		clear();
		this.ex = ex;
		boolean isLoggedIn = authController.isLoggedIn();
		if(ex instanceof ReadOnlyModeException) {
			view.showError(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE);
		} else if(ex instanceof SynapseDownException) {
			globalApplicationState.getPlaceChanger().goTo(new Down(DEFAULT_PLACE_TOKEN));
		} else if(ex instanceof UnauthorizedException) {
			// send user to login page				
			view.showInfo(DisplayConstants.SESSION_TIMEOUT, DisplayConstants.SESSION_HAS_TIMED_OUT);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
		} else if(ex instanceof ForbiddenException) {			
			if(!isLoggedIn) {
				view.showLoginAlert();
			} else {
				view.showError(DisplayConstants.ERROR_FAILURE_PRIVLEDGES + " " + ex.getMessage());
			}
		} else if(ex instanceof NotFoundException) {
			view.showError(DisplayConstants.ERROR_NOT_FOUND  + " " + ex.getMessage());
		} else if (ex instanceof UnknownErrorException) {
			//An unknown error occurred. 
			//Exception handling on the backend now throws the reason into the exception message.  Easy!
			view.showError(ex.getMessage());
			if (isLoggedIn) {
				view.showJiraDialog(ex.getMessage());
			}
		} else {
			//not recognized
			String message = ex.getMessage(); 
			if (message == null || 
				message.isEmpty() ||
				message.equals("0")) {
				message = DisplayConstants.ERROR_RESPONSE_UNAVAILABLE;
			}
			view.showError(message);
		}
	}
	
	@Override
	public void onCreateJiraIssue(final String userReport) {
		JiraURLHelper jiraHelper = globalApplicationState.getJiraURLHelper();
		jiraHelper.createIssueOnBackend(userReport, ex,
			ex.getMessage(), new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					view.hideJiraDialog();
					view.showInfo("Report sent", "Thank you!");
				}

				@Override
				public void onFailure(Throwable caught) {
					// failure to create issue!
					view.hideJiraDialog();
					view.showError(DisplayConstants.ERROR_GENERIC_NOTIFY+"\n" 
					+ caught.getMessage() +"\n\n"
					+ userReport);
				}
			});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void showError(String error) {
		clear();
		view.showError(error);
	}
	
	@Override
	public void onLoginClicked() {
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));	
	}
	
	@Override
	public boolean isUserLoggedIn() {
		return authController.isLoggedIn();
	}
	
	@Override
	public void showMustLogin() {
		clear();
		view.showLoginAlert();
	}
	
	@Override
	public void clear() {
		view.clearState();
		ex = null;
		entityId = null;
	}
	
	@Override
	public void show403() {
		clear();
		view.show403();
	}
	@Override
	public void show403(String entityId) {
		clear();
		this.entityId = entityId;
		view.show403();
		if (!authController.isLoggedIn()) {
			view.showLoginAlert();
		} else {
			view.showRequestAccessUI();	
		}
	}
	
	@Override
	public void show404() {
		clear();
		view.show404();
	}
	
	@Override
	public void onRequestAccess() {
		view.showRequestAccessButtonLoading();
		String hostPageURL = gwt.getHostPageBaseURL();
		UserSessionData userData = authController.getCurrentUserSessionData();
		String userDisplayName = DisplayUtils.getDisplayName(userData.getProfile());
		String message = userDisplayName + " has requested access to an entity that you own. \nTo grant access, please visit " + gwt.getHostPageBaseURL() + "#!Synapse:" + entityId + " to change the Share settings.";
		synapseClient.sendMessageToEntityOwner(entityId, "Requesting access to " + entityId, message, gwt.getHostPageBaseURL(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo("Request sent", "");
				view.hideRequestAccessUI();
			}
			@Override
			public void onFailure(Throwable caught) {
				handleException(caught);
			}
		});
	}
}
