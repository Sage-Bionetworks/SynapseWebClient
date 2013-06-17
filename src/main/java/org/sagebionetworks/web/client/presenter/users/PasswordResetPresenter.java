package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.factory.SystemFactory;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.users.PasswordResetView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

@SuppressWarnings("unused")
public class PasswordResetPresenter extends AbstractActivity implements PasswordResetView.Presenter, Presenter<PasswordReset> {
	public static final String REGISTRATION_TOKEN_PREFIX = "register_";
	public static final String CHANGE_EMAIL_TOKEN_PREFIX = "change_email_";
	private PasswordReset place;	
	private PasswordResetView view;
	private CookieProvider cookieProvider;
	private UserAccountServiceAsync userService;
	private AuthenticationController authenticationController;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	SystemFactory systemFactory;
	
	private String registrationToken, changeEmailToken = null;
	
	@Inject
	public PasswordResetPresenter(PasswordResetView view,
			CookieProvider cookieProvider, UserAccountServiceAsync userService,
			AuthenticationController authenticationController,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			GlobalApplicationState globalApplicationState,
			NodeModelCreator nodeModelCreator,
			SystemFactory systemFactory) {
		this.view = view;
		this.userService = userService;
		this.authenticationController = authenticationController;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		// Set the presenter on the view
		this.cookieProvider = cookieProvider;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator=nodeModelCreator;
		this.systemFactory = systemFactory;
		
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
		registrationToken = null;
		changeEmailToken = null;
		
		// show proper view if token is present
		if(DisplayUtils.DEFAULT_PLACE_TOKEN.equals(place.toToken())) {
			view.showRequestForm();
		} else if (place.toToken().startsWith(REGISTRATION_TOKEN_PREFIX)) {
			// if this is a registration token, we don't have enough information
			// to log them in, but we can still set the password from this token
			registrationToken = place.toToken();
			view.showResetForm();
		} else if (place.toToken().startsWith(CHANGE_EMAIL_TOKEN_PREFIX)) {
			//this is a change email token.
			//the user must be logged in for this to work.
			if (!systemFactory.getCookieHelper().isLoggedIn()) {
				view.showMessage("You must be logged in to change your email address.");
				globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			} else {
				changeEmailToken = place.toToken();
				view.showResetForm();
			}
		} else {
			// Show password reset form
			view.showMessage(AbstractImagePrototype.create(sageImageBundle.loading16()).getHTML() + " Loading Password Reset...");
			
			// show same error if service fails as with an invalid token					
			final String errorMessage = "Password reset period has expired. <a href=\"#!PasswordReset:0\">Please request another Password Reset</a>.";
			String sessionToken = place.toToken();
			authenticationController.loginUser(sessionToken, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					if(result != null) {
						view.showResetForm();
					} else {
						view.showMessage(errorMessage);
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showMessage(errorMessage);
				}
			});
		}
	}

	@Override
	public void requestPasswordReset(String emailAddress) {
		userService.sendPasswordResetEmail(emailAddress, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showRequestSentSuccess();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage("An error occurred in sending your request. Please retry.");
			}
		});
		
	}

	@Override
	public void resetPassword(final String newPassword) {
		if (registrationToken != null) {
			userService.setRegistrationUserPassword(registrationToken,	newPassword, new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							view.showInfo(DisplayConstants.PASSWORD_SET_TEXT);
							globalApplicationState.getPlaceChanger().goTo(
									new LoginPlace(LoginPlace.LOGIN_TOKEN));
						}

						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(DisplayConstants.PASSWORD_SET_FAILED_TEXT);
						}
					});
		} else if (changeEmailToken != null) {
				userService.changeEmailAddress(changeEmailToken, newPassword, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								view.showInfo(DisplayConstants.PASSWORD_AND_EMAIL_SET_TEXT);
								globalApplicationState.getPlaceChanger().goTo(
										new LoginPlace(LoginPlace.LOGIN_TOKEN));
							}

							@Override
							public void onFailure(Throwable caught) {
								view.showErrorMessage(DisplayConstants.EMAIL_SET_FAILED_TEXT);
							}
						});
			
		} else {
			UserSessionData currentUser = authenticationController.getLoggedInUser();
			if (currentUser != null) {
				userService.setPassword(
						newPassword, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								view.showInfo(DisplayConstants.PASSWORD_RESET_TEXT);
								view.showPasswordResetSuccess();
								globalApplicationState.getPlaceChanger().goTo(new Home(DisplayUtils.DEFAULT_PLACE_TOKEN)); // redirect to home page
							}

							@Override
							public void onFailure(Throwable caught) {
								view.showErrorMessage(DisplayConstants.PASSWORD_RESET_FAILED_TEXT);
							}
						});
			}
		}
	}
}
