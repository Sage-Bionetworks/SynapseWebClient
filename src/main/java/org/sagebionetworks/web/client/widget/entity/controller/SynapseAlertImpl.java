package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.TooManyRequestsException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class SynapseAlertImpl implements SynapseAlert, SynapseAlertView.Presenter  {
	public static final String SERVER_STATUS_CODE_MESSAGE = "Server responded with unexpected status code: ";
	public static final String BROWSE_PATH = "/browse/";
	GlobalApplicationState globalApplicationState;
	AuthenticationController authController;
	SynapseAlertView view;
	PortalGinInjector ginInjector;
	Throwable ex;
	Callback reloadOnLoginListener;
	SynapseJSNIUtils jsniUtils;
	
	@Inject
	public SynapseAlertImpl(
			SynapseAlertView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authController,
			GWTWrapper gwt,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils
			) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authController = authController;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
		view.setPresenter(this);
		
		reloadOnLoginListener = () -> {
			SynapseAlertImpl.this.view.reload();
		};
	}

	@Override
	public void handleException(Throwable ex) {
		clear();
		this.ex = ex;
		boolean isLoggedIn = authController.isLoggedIn();
		if (ex instanceof StatusCodeException) {
			StatusCodeException sce = (StatusCodeException)ex;
			if (sce.getStatusCode() == 0) {
				// request failed (network error) or it's been aborted (left the page).
				view.showError(DisplayConstants.NETWORK_ERROR);
				view.setRetryButtonVisible(true);
			} else if (DisplayUtils.isDefined(sce.getStatusText())) {
				view.showError(sce.getStatusText());
			} else {
				view.showError(SERVER_STATUS_CODE_MESSAGE + sce.getStatusCode());
			}
		} else if(ex instanceof ReadOnlyModeException || ex instanceof SynapseDownException) {
			globalApplicationState.getPlaceChanger().goTo(new Down(DEFAULT_PLACE_TOKEN));
		} else if(ex instanceof UnauthorizedException) {
			// send user to login page
			// invalid session token.  log out user and send to login place
			authController.logoutUser();
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else if(ex instanceof ForbiddenException) {			
			if(!isLoggedIn) {
				showLogin();
			} else {
				view.showError(DisplayConstants.ERROR_FAILURE_PRIVLEDGES + " " + ex.getMessage());
			}
		} else if(ex instanceof NotFoundException) {
			view.showError(DisplayConstants.ERROR_NOT_FOUND  + " " + ex.getMessage());
		} else if (ex instanceof TooManyRequestsException) {
			view.showError(DisplayConstants.ERROR_TOO_MANY_REQUESTS  + "\n\n" + ex.getMessage());
		} else if (ex instanceof ConflictingUpdateException) {
			view.showError(DisplayConstants.ERROR_CONFLICTING_UPDATE + "\n" + ex.getMessage());
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
			ex.getMessage(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String key) {
					view.hideJiraDialog();
					String jiraEndpoint = ginInjector.getSynapseProperties().getSynapseProperty(WebConstants.CONFLUENCE_ENDPOINT);
					String url = jiraEndpoint + BROWSE_PATH + key;
					view.showJiraIssueOpen(key, url);
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
	public boolean isUserLoggedIn() {
		return authController.isLoggedIn();
	}
	
	@Override
	public void showLogin() {
		clear();
		// lazy inject login widget
		LoginWidget loginWidget = ginInjector.getLoginWidget();
		loginWidget.setUserListener(reloadOnLoginListener);
		view.setLoginWidget(loginWidget.asWidget());
		view.showLogin();
	}
	
	@Override
	public void clear() {
		view.clearState();
		ex = null;
	}
	
	@Override
	public void consoleError(String errorMessage) {
		jsniUtils.consoleError(errorMessage);
	}
}
