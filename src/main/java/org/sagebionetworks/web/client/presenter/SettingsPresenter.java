package org.sagebionetworks.web.client.presenter;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsPresenter implements SettingsView.Presenter {
		
	private SettingsView view;
	private AuthenticationController authenticationController;
	private UserAccountServiceAsync userService;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private String apiKey = null;
	
	@Inject
	public SettingsPresenter(SettingsView view,
			AuthenticationController authenticationController,
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			NodeModelCreator nodeModelCreator,
			SynapseClientAsync synapseClient,
			GWTWrapper gwt) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		view.setPresenter(this);
	}

	private void getAPIKey() {
		// lookup API key
		if(apiKey == null) {			
			synapseClient.getAPIKey(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					apiKey = result;
					view.setApiKey(apiKey);
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
						view.setApiKey(DisplayConstants.ERROR_LOADING);
					}
				}
			});
		} else {
			view.setApiKey(apiKey);
		}
	}

	@Override
	public void resetPassword(final String existingPassword, final String newPassword) {		
		if(authenticationController.isLoggedIn()) {
			if(authenticationController.getCurrentUserSessionData() != null 
					&& authenticationController.getCurrentUserSessionData().getProfile() != null
					&& authenticationController.getCurrentUserSessionData().getProfile().getUserName() != null) {
				final String username = authenticationController.getCurrentUserSessionData().getProfile().getUserName();
				authenticationController.loginUser(username, existingPassword, new AsyncCallback<UserSessionData>() {				
					@Override
					public void onSuccess(UserSessionData userSessionData) {
						userService.changePassword(authenticationController.getCurrentUserSessionToken(), newPassword, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								view.showPasswordChangeSuccess();								
								// login user as session token has changed
								authenticationController.loginUser(username, newPassword, new AsyncCallback<UserSessionData>() {
									@Override
									public void onSuccess(UserSessionData result) {
									}
									@Override
									public void onFailure(Throwable caught) {
										// if login fails, simple send them to the login page to get a new session
										globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
									}
								});
							}

							@Override
							public void onFailure(Throwable caught) {						
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
			view.showInfo("Error","Reset Password failed. Please Login again.");
			goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}
	
	public void getUserNotificationEmail() {
		synapseClient.getNotificationEmail(new AsyncCallback<String>(){
			@Override
			public void onSuccess(String notificationEmail) {
				view.showNotificationEmailAddress(notificationEmail);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	

	public void setUserNotificationEmail(final String email) {
		synapseClient.setNotificationEmail(email, new AsyncCallback<Void>(){
			@Override
			public void onSuccess(Void callback) {
				//reload profile
				goTo(new Profile(Profile.EDIT_PROFILE_TOKEN));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void updateUserStorage() {
		userService.getStorageUsage(new AsyncCallback<String>(){
			@Override
			public void onSuccess(String storageUsageSummaryListJson) {
				try {
					StorageUsageSummaryList storageUsageSummaryList = nodeModelCreator.createJSONEntity(storageUsageSummaryListJson, StorageUsageSummaryList.class);
					view.updateStorageUsage(storageUsageSummaryList.getTotalSize());
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}    				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//couldn't figure out the usage, update the view to indicate that the test was inconclusive
				view.clearStorageUsageUI();
			}
		});
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void updateMyNotificationSettings(final boolean sendEmailNotifications, final boolean markEmailedMessagesAsRead){
		//get my profile
		synapseClient.getUserProfile(null, new AsyncCallback<UserProfile>() {
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
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(caught.getMessage());
				}
			}
		});
	}
	
	private void updateView() {
		updateUserStorage();
		getUserNotificationEmail();
		getAPIKey();
		view.updateNotificationCheckbox(authenticationController.getCurrentUserSessionData().getProfile());
	}

	@Override
	public void changeApiKey() {
		synapseClient.deleteApiKey(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.API_KEY_CHANGED, "");
				view.setApiKey(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		});
	}
	
	@Override
	public void addEmail(String emailAddress) {
		//Is this email already in the profile email list?
		//If so, just update it as the new notification email.  Otherwise, kick off the verification process.
		List<String> emailAddresses = authenticationController.getCurrentUserSessionData().getProfile().getEmails();
		if (emailAddresses == null || emailAddresses.isEmpty()) throw new IllegalStateException("UserProfile email list is empty");
		for (String email : emailAddresses) {
			if (email.equalsIgnoreCase(emailAddress)) {
				//update the notification email
				setUserNotificationEmail(emailAddress);	
				return;
			}
		}
		//did not find in the list
		additionalEmailValidation(emailAddress);
	}
	
	public void additionalEmailValidation(String emailAddress) {
		//need to validate
		String callbackUrl = gwt.getHostPageBaseURL() + "#!Account:";
		synapseClient.additionalEmailValidation(authenticationController.getCurrentUserPrincipalId(), emailAddress, callbackUrl, new AsyncCallback<Void>() {
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
	
	public Widget asWidget() {
		this.view.render();
		updateView();
		return view.asWidget();
	}
}

