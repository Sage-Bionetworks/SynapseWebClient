package org.sagebionetworks.web.client.presenter.users;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.PasswordResetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class PasswordResetPresenter extends AbstractActivity implements PasswordResetView.Presenter, Presenter<PasswordReset> {
	private PasswordReset place;	
	private PasswordResetView view;
	private CookieProvider cookieProvider;
	private UserAccountServiceAsync userService;
	private AuthenticationController authenticationController;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private GlobalApplicationState globalApplicationState;
	private PasswordStrengthWidget passwordStrengthWidget;
	private SynapseAlert synAlert;
	private String sessionToken = null;
	
	@Inject
	public PasswordResetPresenter(PasswordResetView view,
			CookieProvider cookieProvider, UserAccountServiceAsync userService,
			AuthenticationController authenticationController,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			GlobalApplicationState globalApplicationState,
			PasswordStrengthWidget passwordStrengthWidget,
			SynapseAlert synAlert) {
		this.view = view;
		this.userService = userService;
		fixServiceEntryPoint(userService);
		this.authenticationController = authenticationController;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		// Set the presenter on the view
		this.cookieProvider = cookieProvider;
		this.globalApplicationState = globalApplicationState;
		this.passwordStrengthWidget = passwordStrengthWidget;
		this.synAlert = synAlert;
		view.setPasswordStrengthWidget(passwordStrengthWidget.asWidget());
		view.setSynAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(PasswordReset place) {
		this.place = place;
		view.setPresenter(this);			
		view.clear(); 
		
		// Assume all tokens other than the default are session tokens
		if (!ClientProperties.DEFAULT_PLACE_TOKEN.equals(place.toToken())) {
			sessionToken = place.toToken();
			// validate that session token is still valid before showing form
			view.showLoading();
			authenticationController.setNewSessionToken(sessionToken, new AsyncCallback<UserProfile>() {
				@Override
				public void onSuccess(UserProfile result) {
					view.showResetForm();	
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showExpiredRequest();
				}
			});
		} else {
			view.showRequestForm();
		}
	}

	@Override
	public void requestPasswordReset(String emailAddress) {
		synAlert.clear();
		userService.sendPasswordResetEmail(emailAddress, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showRequestSentSuccess();
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					view.showErrorMessage(caught.getMessage());
				} else {
					synAlert.handleException(caught);
				}
				view.setSubmitButtonEnabled(true);
			}
		});
		
	}

	@Override
	public void resetPassword(final String newPassword) {
		synAlert.clear();
		if (sessionToken == null && authenticationController.isLoggedIn()) {
			sessionToken = authenticationController.getCurrentUserSessionToken();
		}
		userService.changePassword(sessionToken, newPassword, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DisplayConstants.PASSWORD_RESET_TEXT);
				view.showPasswordResetSuccess();
				
				UserProfile profile = authenticationController.getCurrentUserProfile();
				if (profile != null && profile.getUserName() != null && !DisplayUtils.isTemporaryUsername(profile.getUserName()))
					//re-login like we do on the Settings page (when changing the password)
					reloginUser(profile.getUserName(), newPassword);
				else {
					//pop up terms of service on re-login
					authenticationController.logoutUser();
					globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN)); // redirect to login page
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setSubmitButtonEnabled(true);
				synAlert.handleException(caught);
			}
		});
	}
	
	public void reloginUser(String username, String newPassword) {
		// login user as session token has changed
        authenticationController.loginUser(username, newPassword, new AsyncCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile result) {
                	globalApplicationState.gotoLastPlace();
                }
                @Override
                public void onFailure(Throwable caught) {
                    // if login fails, simple send them to the login page to get a new session
                    globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
                }
        });
	}
	
	@Override
	public void passwordChanged(String password) {
		passwordStrengthWidget.scorePassword(password);
	}

}
