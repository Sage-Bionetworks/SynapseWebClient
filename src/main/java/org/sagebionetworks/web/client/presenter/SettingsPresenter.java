package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidget;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsPresenter implements SettingsView.Presenter {

	private SettingsView view;
	private AuthenticationController authenticationController;
	private UserAccountServiceAsync userService;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SynapseAlert apiSynAlert;
	private SynapseAlert notificationSynAlert;
	private SynapseAlert passwordSynAlert;
	private PortalGinInjector ginInjector;
	private UserProfileModalWidget userProfileModalWidget;
	private SubscriptionListWidget subscriptionListWidget;
	private PasswordStrengthWidget passwordStrengthWidget;
	private EmailAddressesWidget emailAddressesWidget;
	private SynapseJavascriptClient jsClient;
	@Inject
	public SettingsPresenter(SettingsView view,
			AuthenticationController authenticationController,
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			PortalGinInjector ginInjector,
			UserProfileModalWidget userProfileModalWidget,
			SubscriptionListWidget subscriptionListWidget,
			PasswordStrengthWidget passwordStrengthWidget,
			EmailAddressesWidget emailAddressesWidget,
			SynapseJavascriptClient jsClient) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		fixServiceEntryPoint(userService);
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.ginInjector = ginInjector;
		this.userProfileModalWidget = userProfileModalWidget;
		this.subscriptionListWidget = subscriptionListWidget;
		this.passwordStrengthWidget = passwordStrengthWidget;
		this.emailAddressesWidget = emailAddressesWidget;
		this.jsClient = jsClient;
		view.setSubscriptionsListWidget(subscriptionListWidget.asWidget());
		view.setPasswordStrengthWidget(passwordStrengthWidget.asWidget());
		view.setEmailAddressesWidget(emailAddressesWidget);
		view.setPresenter(this);
		setSynAlertWidgets();
	}

	private void setSynAlertWidgets() {
		apiSynAlert = ginInjector.getSynapseAlertWidget();
		notificationSynAlert = ginInjector.getSynapseAlertWidget();
		passwordSynAlert = ginInjector.getSynapseAlertWidget();
		view.setAPISynAlertWidget(apiSynAlert);
		view.setNotificationSynAlertWidget(notificationSynAlert);
		view.setPasswordSynAlertWidget(passwordSynAlert);
	}

	@Override
	public void getAPIKey() {
		apiSynAlert.clear();
		// lookup API key
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.setApiKey(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				apiSynAlert.handleException(caught);
			}
		};
		synapseClient.getAPIKey(callback);
	}

	@Override
	public void resetPassword(final String existingPassword,
			final String newPassword) {
		clearPasswordErrors();
		if (authenticationController.isLoggedIn()) {
			if (authenticationController.getCurrentUserProfile() != null
					&& authenticationController.getCurrentUserProfile().getUserName() != null) {
				final String username = authenticationController.getCurrentUserProfile().getUserName();
				authenticationController.loginUser(username, existingPassword,
						new AsyncCallback<UserProfile>() {
							@Override
							public void onSuccess(UserProfile userSessionData) {
								userService.changePassword(authenticationController.getCurrentUserSessionToken(),newPassword, new AsyncCallback<Void>() {
									@Override
									public void onSuccess(Void result) {
										passwordStrengthWidget.setVisible(false);
										view.showPasswordChangeSuccess();
										// login user as session token
										// has changed
										authenticationController.loginUser(username, newPassword, new AsyncCallback<UserProfile>() {
											@Override
											public void onSuccess(UserProfile result) {
											}
											@Override
											public void onFailure(Throwable caught) {
												//if login fails, simple send them to the login page to get a new session
												globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
											}
										});
									}

									@Override
									public void onFailure(
											Throwable caught) {
										passwordSynAlert.handleException(caught);
										view.setChangePasswordEnabled(true);
									}
								});
							}
							@Override
							public void onFailure(Throwable caught) {
								passwordSynAlert.showError("Incorrect password. Please enter your existing Synapse password.");
								view.setCurrentPasswordInError(true);
								view.setChangePasswordEnabled(true);
							}
						});
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		} else {
			view.showInfo("Reset Password failed. Please Login again.");
			goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

	// notification checkbox
	@Override
	public void updateMyNotificationSettings(
			final boolean sendEmailNotifications,
			final boolean markEmailedMessagesAsRead) {
		notificationSynAlert.clear();
		// get my profile
		AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(final UserProfile myProfile) {
				org.sagebionetworks.repo.model.message.Settings settings = myProfile
						.getNotificationSettings();
				if (settings == null) {
					settings = new org.sagebionetworks.repo.model.message.Settings();
					settings.setMarkEmailedMessagesAsRead(false);
					settings.setSendEmailNotifications(true);
					myProfile.setNotificationSettings(settings);
				}
				settings.setSendEmailNotifications(sendEmailNotifications);
				settings.setMarkEmailedMessagesAsRead(markEmailedMessagesAsRead);

				synapseClient.updateUserProfile(myProfile, new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						view.showInfo(DisplayConstants.UPDATED_NOTIFICATION_SETTINGS);
						authenticationController.updateCachedProfile(myProfile);
					}

					@Override
					public void onFailure(Throwable caught) {
						notificationSynAlert.handleException(caught);
					}
				});
			}

			@Override
			public void onFailure(Throwable caught) {
				notificationSynAlert.handleException(caught);
			}
		};
		jsClient.getUserProfile(null, callback);
	}
	
	public void clear() {
		view.clear();
		apiSynAlert.clear();
		notificationSynAlert.clear();
		emailAddressesWidget.clear();
		passwordSynAlert.clear();
		passwordStrengthWidget.setVisible(false);
	}
	
	public void configure() {
		clear();
		if (authenticationController.isLoggedIn()) {
			AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
				@Override
				public void onSuccess(UserProfile result) {
					emailAddressesWidget.configure(result);
					authenticationController.updateCachedProfile(result);
					view.updateNotificationCheckbox(result);	
				}
				@Override
				public void onFailure(Throwable caught) {
					notificationSynAlert.handleException(caught);
				}
			};
			jsClient.getUserProfile(null, callback);
			
			subscriptionListWidget.configure();
			if (globalApplicationState.isShowingUTCTime()) {
				view.setShowingUTCTime();	
			} else {
				view.setShowingLocalTime();
			}
		}
		this.view.render();
	}

	@Override
	public void changeApiKey() {
		apiSynAlert.clear();
		Callback callback = () -> {
			changeApiKeyPostConfirmation();	
		};
		view.showConfirm(DisplayConstants.API_KEY_CONFIRMATION, callback);
	}
	
	public void changeApiKeyPostConfirmation(){
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.API_KEY_CHANGED);
				view.setApiKey(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				apiSynAlert.handleException(caught);
			}
		};
		synapseClient.deleteApiKey(callback);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onEditProfile() {
		userProfileModalWidget.showEditProfile(authenticationController.getCurrentUserPrincipalId(), new Callback() {
			@Override
			public void invoke() {
				goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
			}
		});
	}

	@Override
	public void changePassword() {
		clearPasswordErrors();
		String currentPassword = view.getCurrentPasswordField();
		String password1 = view.getPassword1Field();
		String password2 = view.getPassword2Field();
		if (!checkPasswordDefined(currentPassword)) {
			view.setCurrentPasswordInError(true);
			passwordSynAlert.showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		} else if (!checkPasswordDefined(password1)){
			view.setPassword1InError(true);
			passwordSynAlert.showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		} else if (!checkPasswordDefined(password2)) {
			view.setPassword2InError(true);
			passwordSynAlert.showError(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
		} else if (!password1.equals(password2)) {
			view.setPassword2InError(true);
			passwordSynAlert.showError(DisplayConstants.PASSWORDS_MISMATCH);
		} else {
			view.setChangePasswordEnabled(false);
			resetPassword(currentPassword, password1);
		}
	}
	
	public void clearPasswordErrors() {
		passwordSynAlert.clear();
		view.setCurrentPasswordInError(false);
		view.setPassword1InError(false);
		view.setPassword2InError(false);
	}
	
	private boolean checkPasswordDefined(String password) {
		return password != null && !password.isEmpty();
	}
	
	@Override
	public void passwordChanged(String password) {
		passwordStrengthWidget.scorePassword(password);
	}

	@Override
	public void setShowUTCTime(boolean isUTC) {
		globalApplicationState.setShowUTCTime(isUTC);
	}
}
