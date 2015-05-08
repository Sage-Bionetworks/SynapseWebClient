package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ClientProperties.DEFAULT_PLACE_TOKEN;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
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
public class ServiceErrorHandlerImpl implements ServiceErrorHandler, ServiceErrorHandlerView.Presenter {
	GlobalApplicationState globalApplicationState;
	AuthenticationController authController;
	ServiceErrorHandlerView view;
	Throwable ex;
	
	@Inject
	public ServiceErrorHandlerImpl(
			ServiceErrorHandlerView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authController
			) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authController = authController;
		view.setPresenter(this);
	}

	@Override
	public void onFailure(Throwable ex, CallbackP<Throwable> unhandledErrorCallback) {
		this.ex = ex;
		SynapseJSNIUtilsImpl._consoleError(getStackTrace(ex));
		boolean isLoggedIn = authController.isLoggedIn();
		if(ex instanceof ReadOnlyModeException) {
			view.showErrorMessage(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE);
		} else if(ex instanceof SynapseDownException) {
			globalApplicationState.getPlaceChanger().goTo(new Down(DEFAULT_PLACE_TOKEN));
		} else if(ex instanceof UnauthorizedException) {
			// send user to login page				
			view.showInfo(DisplayConstants.SESSION_TIMEOUT, DisplayConstants.SESSION_HAS_TIMED_OUT);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGOUT_TOKEN));
		} else if(ex instanceof ForbiddenException) {			
			if(!isLoggedIn) {				
				view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_FAILURE_PRIVLEDGES + " " + ex.getMessage());
			}
		} else if(ex instanceof BadRequestException) {
			//show error (not to file a jira though)
			view.showErrorMessage(ex.getMessage());
		} else if(ex instanceof NotFoundException) {
			view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);
			globalApplicationState.getPlaceChanger().goTo(new Home(DEFAULT_PLACE_TOKEN));
		} else if (ex instanceof UnknownErrorException) {
			//An unknown error occurred. 
			//Exception handling on the backend now throws the reason into the exception message.  Easy!
			if (!isLoggedIn) {
				view.showErrorMessage(ex.getMessage());
			} else {
				view.showJiraDialog(ex.getMessage());	
			}
		} else {
			//unhandled
			// For other exceptions, allow the consumer to send a good message to the user
			unhandledErrorCallback.invoke(ex);
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
	
}
