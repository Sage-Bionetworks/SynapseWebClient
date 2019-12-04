package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_1;
import static org.sagebionetworks.web.shared.WebConstants.ISSUE_PRIORITY_MINOR;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.DeprecatedServiceException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.TooManyRequestsException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseAlertImpl implements SynapseAlert {
	public static final String SERVER_STATUS_CODE_MESSAGE = "Server responded with unexpected status code: ";
	public static final String BROWSE_PATH = "/browse/";
	GlobalApplicationState globalApplicationState;
	AuthenticationController authController;
	SynapseAlertView view;
	PortalGinInjector ginInjector;
	Throwable ex;
	SynapseJSNIUtils jsniUtils;
	JSONObjectAdapter jsonObjectAdapter;
	GWTWrapper gwt;

	@Inject
	public SynapseAlertImpl(SynapseAlertView view, GlobalApplicationState globalApplicationState, AuthenticationController authController, GWTWrapper gwt, PortalGinInjector ginInjector, SynapseJSNIUtils jsniUtils, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authController = authController;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.gwt = gwt;
	}

	@Override
	public void handleException(Throwable ex) {
		clear();
		this.ex = ex;
		boolean isLoggedIn = authController.isLoggedIn();
		String message = ex.getMessage() == null ? "" : ex.getMessage();
		if (ex instanceof StatusCodeException) {
			StatusCodeException sce = (StatusCodeException) ex;
			if (sce.getStatusCode() == 0) {
				// request failed (network error) or it's been aborted (left the page).
				view.showError(DisplayConstants.NETWORK_ERROR);
				view.setRetryButtonVisible(true);
			} else if (DisplayUtils.isDefined(sce.getStatusText())) {
				view.showError(sce.getStatusText());
			} else {
				view.showError(SERVER_STATUS_CODE_MESSAGE + sce.getStatusCode());
			}
		} else if (ex instanceof ReadOnlyModeException || ex instanceof SynapseDownException) {
			globalApplicationState.getPlaceChanger().goTo(new Down(DEFAULT_PLACE_TOKEN));
		} else if (ex instanceof UnauthorizedException) {
			// send user to login page
			// invalid session token. log out user and send to login place
			authController.logoutUser();
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else if (ex instanceof ForbiddenException) {
			if (!isLoggedIn) {
				showLogin();
			} else {
				view.showError(DisplayConstants.ERROR_FAILURE_PRIVLEDGES + " " + message);
			}
		} else if (ex instanceof NotFoundException) {
			view.showError(DisplayConstants.ERROR_NOT_FOUND + " " + message);
		} else if (ex instanceof TooManyRequestsException) {
			view.showError(DisplayConstants.ERROR_TOO_MANY_REQUESTS + "\n\n" + message);
		} else if (ex instanceof ConflictingUpdateException) {
			view.showError(DisplayConstants.ERROR_CONFLICTING_UPDATE + "\n" + message);
		} else if (ex instanceof DeprecatedServiceException) {
			view.showError(DisplayConstants.ERROR_DEPRECATED_SERVICE + "\n" + message);
		} else if (ex instanceof UnknownErrorException) {
			// An unknown error occurred.
			// Exception handling on the backend now throws the reason into the exception message. Easy!
			view.showError(message);
			if (isLoggedIn) {
				onCreateJiraIssue(message);
			}
		} else {
			// not recognized
			if (message == null || message.isEmpty() || message.equals("0")) {
				message = DisplayConstants.ERROR_RESPONSE_UNAVAILABLE;
			}

			// if this is json, report the reason value (if available)
			try {
				JSONObjectAdapter json = jsonObjectAdapter.createNew(message);
				if (json.has("reason")) {
					message = json.getString("reason");
				}
			} catch (Throwable e) {
				// was not json
			}

			view.showError(message);
		}
	}

	public void onCreateJiraIssue(String errorMessage) {
		String userId = WebConstants.ANONYMOUS, email = WebConstants.ANONYMOUS, displayName = WebConstants.ANONYMOUS;
		UserProfile userProfile = authController.getCurrentUserProfile();
		if (userProfile != null) {
			userId = userProfile.getOwnerId();
			displayName = DisplayUtils.getDisplayName(userProfile);
		}
		String description = FLAG_ISSUE_DESCRIPTION_PART_1 + gwt.getCurrentURL() + "\n\n" + errorMessage;

		jsniUtils.showJiraIssueCollector("", description, WebConstants.SWC_ISSUE_COLLECTOR_URL, userId, displayName, email, "", "", "", ISSUE_PRIORITY_MINOR);
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
