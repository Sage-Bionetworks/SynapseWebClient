package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class LoginPresenter extends AbstractActivity implements LoginView.Presenter, Presenter<LoginPlace> {
	private LoginView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseAlert synAlert;

	@Inject
	public LoginPresenter(LoginView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, SynapseAlert synAlert) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}

	private Callback getAcceptTermsOfUseCallback() {
		return () -> {
			synAlert.clear();
			view.showLoggingInLoader();
			authenticationController.signTermsOfUse(true, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
					view.showLogin();
				}

				@Override
				public void onSuccess(Void result) {
					// Have to get the UserSessionData again,
					// since it won't contain the UserProfile if the terms haven't been signed
					synAlert.clear();
					authenticationController.initializeFromExistingSessionCookie(new AsyncCallback<UserProfile>() {
						@Override
						public void onFailure(Throwable caught) {
							synAlert.handleException(caught);
							view.showLogin();
						}

						@Override
						public void onSuccess(UserProfile result) {
							// Signed ToU. Check for temp username, passing record, and then forward
							userAuthenticated();
						}
					});
				}
			});
		};
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(this.view.asWidget());
	}

	@Override
	public void setPlace(final LoginPlace place) {
		view.setPresenter(this);
		view.clear();
		showView(place);
	}

	public void showView(final LoginPlace place) {
		String token = place.toToken();
		if (LoginPlace.LOGOUT_TOKEN.equals(token)) {
			authenticationController.logoutUser();
			view.showInfo(DisplayConstants.LOGOUT_TEXT);
			globalApplicationState.getPlaceChanger().goTo(new Home(ClientProperties.DEFAULT_PLACE_TOKEN));
		} else if (WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN.equals(token)) {
			// User does not exist, redirect to Registration page
			view.showErrorMessage(DisplayConstants.CREATE_ACCOUNT_MESSAGE_SSO);
			globalApplicationState.getPlaceChanger().goTo(new RegisterAccount(ClientProperties.DEFAULT_PLACE_TOKEN));
		} else if (WebConstants.OPEN_ID_ERROR_TOKEN.equals(token)) {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
			view.showErrorMessage(DisplayConstants.SSO_ERROR_UNKNOWN);
			view.showLogin();
		} else if (LoginPlace.CHANGE_USERNAME.equals(token) && authenticationController.isLoggedIn()) {
			// go to the change username page
			gotoChangeUsernamePlace();
		} else if (LoginPlace.SHOW_TOU.equals(token) && authenticationController.getCurrentUserSessionToken() != null) {
			showTermsOfUse(false);
		} else if (LoginPlace.SHOW_SIGNED_TOU.equals(token) && authenticationController.getCurrentUserSessionToken() != null) {
			showTermsOfUse(true);
		} else if (!ClientProperties.DEFAULT_PLACE_TOKEN.equals(token) && !LoginPlace.CHANGE_USERNAME.equals(token) && !"".equals(token) && token != null) {
			revalidateSession(token);
		} else {
			if (authenticationController.isLoggedIn()) {
				Place defaultPlace = new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.PROJECTS);
				globalApplicationState.gotoLastPlace(defaultPlace);
			} else {
				// standard view
				view.showLogin();
			}
		}
	}

	private void gotoChangeUsernamePlace() {
		globalApplicationState.getPlaceChanger().goTo(new ChangeUsername(ClientProperties.DEFAULT_PLACE_TOKEN));
	}

	/**
	 * Check for temp username, and prompt for change if user has not set
	 */
	public void checkForTempUsername() {
		// get my profile, and check for a default username
		UserProfile userProfile = authenticationController.getCurrentUserProfile();
		if (userProfile != null && DisplayUtils.isTemporaryUsername(userProfile.getUserName())) {
			gotoChangeUsernamePlace();
		} else {
			goToLastPlace();
		}
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	@Override
	public void goToLastPlace() {
		view.hideLoggingInLoader();
		Place defaultPlace = new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.PROJECTS);
		globalApplicationState.gotoLastPlace(defaultPlace);
	}

	public void showTermsOfUse(boolean isSigned) {
		synAlert.clear();
		view.hideLoggingInLoader();
		view.showTermsOfUse(isSigned, getAcceptTermsOfUseCallback());
	}

	public void userAuthenticated() {
		view.hideLoggingInLoader();
		// the user should be logged in now.
		if (authenticationController.getCurrentUserSessionToken() == null) {
			view.showErrorMessage("An error occurred during login. Please try logging in again.");
			view.showLogin();
		} else {
			checkForTempUsername();
		}
	}

	private void revalidateSession(String token) {
		// Single Sign on token. try refreshing the token to see if it is valid. if so, log user in
		// parse token
		view.showLoggingInLoader();
		if (token != null) {
			synAlert.clear();
			AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
				@Override
				public void onSuccess(UserProfile result) {
					userAuthenticated();
				}

				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
					view.showLogin();
				}
			};

			authenticationController.setNewSessionToken(token, callback);
		}
	}
}
