package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_CERTIFIED;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.ORC_ID;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.PROFILE;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.VERIFICATION_SUBMISSION;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.ChangePasswordWithCurrentPassword;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsPresenter implements SettingsView.Presenter {

	public static final String MUST_BE_CERTIFIED_TO_SUBMIT_PROFILE_VALIDATION_MESSAGE = "Only Certified Users can apply to have their user profile validated.  Please get certified and try again.";
	private SettingsView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SynapseAlert apiSynAlert;
	private SynapseAlert notificationSynAlert;
	private SynapseAlert passwordSynAlert;
	private PortalGinInjector ginInjector;
	private UserProfileModalWidget userProfileModalWidget;
	private SubscriptionListWidget subscriptionListWidget;
	private EmailAddressesWidget emailAddressesWidget;
	private SynapseJavascriptClient jsClient;
	public Callback resubmitVerificationCallback;
	public VerificationSubmissionWidget verificationModal;
	public UserBundle currentUserBundle;
	private PopupUtilsView popupUtils;

	@Inject
	public SettingsPresenter(SettingsView view, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient, PortalGinInjector ginInjector, UserProfileModalWidget userProfileModalWidget, SubscriptionListWidget subscriptionListWidget, EmailAddressesWidget emailAddressesWidget, PopupUtilsView popupUtils, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.ginInjector = ginInjector;
		this.userProfileModalWidget = userProfileModalWidget;
		this.subscriptionListWidget = subscriptionListWidget;
		this.emailAddressesWidget = emailAddressesWidget;
		this.popupUtils = popupUtils;
		this.jsClient = jsClient;
		view.setSubscriptionsListWidget(subscriptionListWidget.asWidget());
		view.setEmailAddressesWidget(emailAddressesWidget);
		view.setPresenter(this);
		resubmitVerificationCallback = () -> {
			newVerificationSubmissionClicked();
		};

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
	public void resetPassword(final String existingPassword, final String newPassword) {
		clearPasswordErrors();
		if (authenticationController.isLoggedIn()) {
			if (authenticationController.getCurrentUserProfile() != null && authenticationController.getCurrentUserProfile().getUserName() != null) {
				String username = authenticationController.getCurrentUserProfile().getUserName();
				ChangePasswordWithCurrentPassword changePasswordRequest = new ChangePasswordWithCurrentPassword();
				changePasswordRequest.setCurrentPassword(existingPassword);
				changePasswordRequest.setNewPassword(newPassword);
				changePasswordRequest.setUsername(authenticationController.getCurrentUserProfile().getUserName());
				jsClient.changePassword(changePasswordRequest, new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						view.showPasswordChangeSuccess();
						// login user as session token
						// has changed
						authenticationController.loginUser(username, newPassword, new AsyncCallback<UserProfile>() {
							@Override
							public void onSuccess(UserProfile result) {}

							@Override
							public void onFailure(Throwable caught) {
								// if login fails, simple send them to the login page to get a new session
								globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
							}
						});
					}

					@Override
					public void onFailure(Throwable caught) {
						passwordSynAlert.showError(caught.getMessage());
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
	public void updateMyNotificationSettings(final boolean sendEmailNotifications, final boolean markEmailedMessagesAsRead) {
		notificationSynAlert.clear();
		// get my profile
		AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(final UserProfile myProfile) {
				org.sagebionetworks.repo.model.message.Settings settings = myProfile.getNotificationSettings();
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
	}

	public void configure() {
		clear();
		if (authenticationController.isLoggedIn()) {
			getUserProfile();

			subscriptionListWidget.configure();
			if (globalApplicationState.isShowingUTCTime()) {
				view.setShowingUTCTime();
			} else {
				view.setShowingLocalTime();
			}
		}
		this.view.render();
	}

	private void getUserProfile() {
		// ask for everything in the user bundle
		currentUserBundle = null;
		int mask = PROFILE | ORC_ID | VERIFICATION_SUBMISSION | IS_CERTIFIED;
		view.setOrcIdVisible(false);
		view.setUnbindOrcIdVisible(false);
		jsClient.getUserBundle(Long.parseLong(authenticationController.getCurrentUserPrincipalId()), mask, new AsyncCallback<UserBundle>() {
			@Override
			public void onSuccess(UserBundle bundle) {
				currentUserBundle = bundle;
				emailAddressesWidget.configure(bundle.getUserProfile());
				authenticationController.updateCachedProfile(bundle.getUserProfile());
				view.updateNotificationCheckbox(bundle.getUserProfile());
				initializeVerificationUI();
				String orcId = bundle.getORCID();
				if (orcId != null && orcId.length() > 0) {
					view.setOrcId(orcId);
					view.setOrcIdVisible(true);
					view.setUnbindOrcIdVisible(true);
					view.setOrcIDLinkButtonVisible(false);
				} else {
					view.setOrcIDLinkButtonVisible(true);
				}
				view.setIsCertified(bundle.getIsCertified());
			}

			@Override
			public void onFailure(Throwable caught) {
				notificationSynAlert.handleException(caught);
			}
		});
	}

	@Override
	public void changeApiKey() {
		apiSynAlert.clear();
		Callback callback = () -> {
			changeApiKeyPostConfirmation();
		};
		view.showConfirm(DisplayConstants.API_KEY_CONFIRMATION, callback);
	}

	public void changeApiKeyPostConfirmation() {
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
		} else if (!checkPasswordDefined(password1)) {
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
	public void setShowUTCTime(boolean isUTC) {
		globalApplicationState.setShowUTCTime(isUTC);
	}

	public VerificationSubmissionWidget getVerificationSubmissionWidget() {
		if (verificationModal == null) {
			verificationModal = ginInjector.getVerificationSubmissionWidget();
		}
		return verificationModal;
	}

	@Override
	public void editVerificationSubmissionClicked() {
		// edit the existing submission
		getVerificationSubmissionWidget().configure(currentUserBundle.getVerificationSubmission(), false, // is ACT
				true) // isModal
				.setResubmitCallback(resubmitVerificationCallback).show();
	}

	@Override
	public void newVerificationSubmissionClicked() {
		if (!currentUserBundle.getIsCertified()) {
			view.showErrorMessage(MUST_BE_CERTIFIED_TO_SUBMIT_PROFILE_VALIDATION_MESSAGE);
			return;
		}
		List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
		if (currentUserBundle.getVerificationSubmission() != null) {
			attachments = currentUserBundle.getVerificationSubmission().getAttachments();
		}

		// create a new submission
		getVerificationSubmissionWidget().configure(currentUserBundle.getUserProfile(), currentUserBundle.getORCID(), true, // isModal
				attachments).show();
	}

	@Override
	public void linkOrcIdClicked() {
		String orcId = currentUserBundle.getORCID();
		if (orcId != null && orcId.length() > 0) {
			// already set!
			view.showErrorMessage("An ORC ID has already been linked to your Synapse account.");
		} else {
			DisplayUtils.newWindow("/Portal/oauth2AliasCallback?oauth2provider=ORCID", "_self", "");
		}
	}

	public void initializeVerificationUI() {
		// The UI is depends on the current state
		VerificationSubmission submission = currentUserBundle.getVerificationSubmission();

		if (submission == null) {
			// no submission. if the owner, provide way to submit
			view.showNotVerified();
		} else {
			// there's a submission in a state.
			showVerificationUI(submission);
		}
	}

	public void showVerificationUI(VerificationSubmission submission) {
		VerificationState currentState = submission.getStateHistory().get(submission.getStateHistory().size() - 1);
		if (currentState.getState() == VerificationStateEnum.SUSPENDED) {
			view.setVerificationSuspendedButtonVisible(true);
			view.setResubmitVerificationButtonVisible(true);
		} else if (currentState.getState() == VerificationStateEnum.REJECTED) {
			view.setVerificationRejectedButtonVisible(true);
			view.setResubmitVerificationButtonVisible(true);
		} else if (currentState.getState() == VerificationStateEnum.SUBMITTED) {
			view.setVerificationSubmittedButtonVisible(true);
		} else if (currentState.getState() == VerificationStateEnum.APPROVED) {
			view.setVerificationDetailsButtonVisible(true);
		}
	}

	@Override
	public void unbindOrcId() {
		popupUtils.showConfirmDialog("Unlink", "Are you sure you want to unlink this ORCID from your Synapse user profile?", new Callback() {
			@Override
			public void invoke() {
				unbindOrcIdAfterConfirmation();
			}
		});
	}

	public void unbindOrcIdAfterConfirmation() {
		jsClient.unbindOAuthProvidersUserId(OAuthProvider.ORCID, currentUserBundle.getORCID(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// ORCID successfully removed. refresh so that the user bundle and UI are up to date
				view.showInfo("ORCID has been successfully unbound.");
				globalApplicationState.refreshPage();
			}

			@Override
			public void onFailure(Throwable caught) {
				notificationSynAlert.handleException(caught);
			}
		});
	}
}
