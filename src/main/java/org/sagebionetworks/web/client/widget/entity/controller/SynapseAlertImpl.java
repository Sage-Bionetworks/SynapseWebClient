package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class SynapseAlertImpl implements SynapseAlert, SynapseAlertView.Presenter {
	GlobalApplicationState globalApplicationState;
	AuthenticationController authController;
	SynapseAlertView view;
	SynapseJSNIUtils synapseJSNIUtils;
	Throwable ex;
	
	@Inject
	public SynapseAlertImpl(
			SynapseAlertView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authController,
			SynapseJSNIUtils synapseJSNIUtils
			) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authController = authController;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
		view.clearState();
	}

	@Override
	public void handleException(Throwable ex) {
		clearState();
		this.ex = ex;
		synapseJSNIUtils.consoleError(getStackTrace(ex));
		boolean isLoggedIn = authController.isLoggedIn();
		if(ex instanceof ReadOnlyModeException) {
			showError(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE);
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
				showError(DisplayConstants.ERROR_FAILURE_PRIVLEDGES + " " + ex.getMessage());
			}
		} else if(ex instanceof BadRequestException) {
			//show error (not to file a jira though)
			showError(ex.getMessage());
		} else if(ex instanceof NotFoundException) {
			showError(DisplayConstants.ERROR_NOT_FOUND  + " " + ex.getMessage());
		} else if (ex instanceof UnknownErrorException) {
			//An unknown error occurred. 
			//Exception handling on the backend now throws the reason into the exception message.  Easy!
			showError(ex.getMessage());
			if (isLoggedIn) {
				view.showJiraDialog(ex.getMessage());
			}
		} else {
			//not recognized
			showError(ex.getMessage());
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
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY+"\n" 
					+ caught.getMessage() +"\n\n"
					+ userReport);
				}
			});
	}
	
	public static String getStackTrace(Throwable t) {
		StringBuilder stackTrace = new StringBuilder();
		if (t != null) {
			for (StackTraceElement element : t.getStackTrace()) {
				stackTrace.append(element + "\n");
			}
		}
		return stackTrace.toString();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	protected void clearState() {
		view.clearState();
		ex = null;
	}
	
	@Override
	public void showError(String error) {
		clearState();
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
		clearState();
		view.showLoginAlert();
	}

}
