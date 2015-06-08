package org.sagebionetworks.web.client.presenter;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;

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
	private GWTWrapper gwt;
	private String apiKey = null;
	private SynapseAlert apiSynAlert;
	private SynapseAlert notificationSynAlert;
	private SynapseAlert addressSynAlert;
	private PortalGinInjector ginInjector;
	private UserProfileModalWidget userProfileModalWidget;
	
	@Inject
	public SettingsPresenter(SettingsView view,
			AuthenticationController authenticationController,
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient, GWTWrapper gwt,
			PortalGinInjector ginInjector,
			UserProfileModalWidget userProfileModalWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.ginInjector = ginInjector;
		this.gwt = gwt;
		this.userProfileModalWidget = userProfileModalWidget;
		view.setPresenter(this);
		setSynAlertWidgets();
	}

	private void setSynAlertWidgets() {
		apiSynAlert = ginInjector.getSynapseAlertWidget();
		notificationSynAlert = ginInjector.getSynapseAlertWidget();
		addressSynAlert = ginInjector.getSynapseAlertWidget();
		view.setAPISynAlertWidget(apiSynAlert.asWidget());
		view.setNotificationSynAlertWidget(notificationSynAlert.asWidget());
		view.setAddressSynAlertWidget(addressSynAlert.asWidget());
	}

	@Override
	public void getAPIKey() {
		apiSynAlert.clear();
		// lookup API key
		if (apiKey == null) {
			AsyncCallback<String> callback = new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					apiKey = result;
					view.setApiKey(apiKey);
				}

				@Override
				public void onFailure(Throwable caught) {
					apiSynAlert.handleException(caught);
				}
			};
			synapseClient.getAPIKey(callback);
		} else {
			view.setApiKey(apiKey);
		}
	}

	@Override
	public void resetPassword(final String existingPassword,
			final String newPassword) {
		if (authenticationController.isLoggedIn()) {
			if (authenticationController.getCurrentUserSessionData() != null
					&& authenticationController.getCurrentUserSessionData().getProfile() != null
					&& authenticationController.getCurrentUserSessionData().getProfile().getUserName() != null) {
				final String username = authenticationController.getCurrentUserSessionData().getProfile().getUserName();
				authenticationController.loginUser(username, existingPassword,
						new AsyncCallback<UserSessionData>() {
							@Override
							public void onSuccess(UserSessionData userSessionData) {
								userService.changePassword(authenticationController.getCurrentUserSessionToken(),newPassword, new AsyncCallback<Void>() {
									@Override
									public void onSuccess(Void result) {
										view.showPasswordChangeSuccess();
										// login user as session token
										// has changed
										authenticationController.loginUser(username, newPassword, new AsyncCallback<UserSessionData>() {
											@Override
											public void onSuccess(UserSessionData result) {
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
										view.passwordChangeFailed("Password Change failed. Please try again.");
									}
								});
							}
							@Override
							public void onFailure(Throwable caught) {
								view.passwordChangeFailed("Incorrect password. Please enter your existing Synapse password.");
							}
						});
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		} else {
			view.showInfo("Error", "Reset Password failed. Please Login again.");
			goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}

	public void getUserNotificationEmail() {
		addressSynAlert.clear();
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String notificationEmail) {
				view.showNotificationEmailAddress(notificationEmail);
			}

			@Override
			public void onFailure(Throwable caught) {
				addressSynAlert.handleException(caught);
			}
		};
		synapseClient.getNotificationEmail(callback);
	}

	public void setUserNotificationEmail(final String email) {
		addressSynAlert.clear();
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void callback) {
				// reload profile
				goTo(new Profile(Profile.EDIT_PROFILE_TOKEN));
			}

			@Override
			public void onFailure(Throwable caught) {
				addressSynAlert.handleException(caught);
			}
		};
		synapseClient.setNotificationEmail(email, callback);
	}

	public void updateUserStorage() {
		userService.getStorageUsage(new AsyncCallback<StorageUsageSummaryList>() {
			@Override
			public void onSuccess(StorageUsageSummaryList results) {
				StorageUsageSummaryList storageUsageSummaryList = results;
				view.updateStorageUsage(storageUsageSummaryList.getTotalSize());
			}

			@Override
			public void onFailure(Throwable caught) {
				// couldn't figure out the usage, update the view to
				// indicate that the test was inconclusive
				view.clearStorageUsageUI();
			}
		});
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
						view.showInfo(DisplayConstants.UPDATED_NOTIFICATION_SETTINGS, "");
						authenticationController.updateCachedProfile(myProfile);
					}

					@Override
					public void onFailure(Throwable caught) {
						view.showErrorMessage(caught.getMessage());
					}
				});
			}

			@Override
			public void onFailure(Throwable caught) {
				notificationSynAlert.handleException(caught);
			}
		};
		synapseClient.getUserProfile(null, callback);
	}

	// configuration
	private void updateView() {
		view.hideAPIKey();
		apiSynAlert.clear();
		notificationSynAlert.clear();
		addressSynAlert.clear();
		updateUserStorage();
		getUserNotificationEmail();
		view.updateNotificationCheckbox(authenticationController.getCurrentUserSessionData().getProfile());
	}

	@Override
	public void changeApiKey() {
		apiSynAlert.clear();
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.API_KEY_CHANGED, "");
				view.setApiKey(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				apiSynAlert.handleException(caught);
			}
		};
		synapseClient.deleteApiKey(callback);
	}

	@Override
	public void addEmail(String emailAddress) {
		// Is this email already in the profile email list?
		// If so, just update it as the new notification email. Otherwise, kick
		// off the verification process.
		List<String> emailAddresses = authenticationController.getCurrentUserSessionData().getProfile().getEmails();
		if (emailAddresses == null || emailAddresses.isEmpty())
			throw new IllegalStateException("UserProfile email list is empty");
		for (String email : emailAddresses) {
			if (email.equalsIgnoreCase(emailAddress)) {
				// update the notification email
				setUserNotificationEmail(emailAddress);
				return;
			}
		}
		// did not find in the list
		additionalEmailValidation(emailAddress);
	}

	public void additionalEmailValidation(String emailAddress) {
		// need to validate
		String callbackUrl = gwt.getHostPageBaseURL() + "#!Account:";
		synapseClient.additionalEmailValidation(
				authenticationController.getCurrentUserPrincipalId(),
				emailAddress, callbackUrl, new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						view.showEmailChangeSuccess(DisplayConstants.EMAIL_ADDED);
					}

					@Override
					public void onFailure(Throwable caught) {
						view.showEmailChangeFailed(caught.getMessage());
					}
				});
	}

	// The entry point of this class, called from the ProfilePresenter
	public Widget asWidget() {
		this.apiSynAlert.clear();
		this.notificationSynAlert.clear();
		this.addressSynAlert.clear();
		this.view.render();		
		updateView();
		return view.asWidget();
	}
	
	@Override
	public void onEditProfile() {
		userProfileModalWidget.showEditProfile(authenticationController.getCurrentUserPrincipalId(), new Callback() {
			@Override
			public void invoke() {
				goTo(new Profile(Profile.SETTINGS_DELIMITER));
			}
		});
	}
}
