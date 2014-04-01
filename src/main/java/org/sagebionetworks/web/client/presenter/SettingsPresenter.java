package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SettingsPresenter extends AbstractActivity implements SettingsView.Presenter, Presenter<Settings> {
		
	private Settings place;
	private SettingsView view;
	private AuthenticationController authenticationController;
	private UserAccountServiceAsync userService;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookieProvider;
	private NodeModelCreator nodeModelCreator;
	private AdapterFactory adapterFactory;
	private SynapseClientAsync synapseClient;
	
	private String apiKey = null;
	
	@Inject
	public SettingsPresenter(SettingsView view,
			AuthenticationController authenticationController,
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			CookieProvider cookieProvider,
			NodeModelCreator nodeModelCreator,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.cookieProvider = cookieProvider;
		this.nodeModelCreator = nodeModelCreator;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Set the presenter on the view
		this.view.render();
		
		// Install the view
		panel.setWidget(view);
		
	}

	@Override
	public void setPlace(Settings place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
		
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
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {
						view.setApiKey(DisplayConstants.ERROR_LOADING);
					}
				}
			});
		} else {
			view.setApiKey(apiKey);
		}
		
		view.updateNotificationCheckbox(authenticationController.getCurrentUserSessionData().getProfile());
	}

	@Override
	public void resetPassword(final String existingPassword, final String newPassword) {		
		if(authenticationController.isLoggedIn()) {
			if(authenticationController.getCurrentUserSessionData() != null 
					&& authenticationController.getCurrentUserSessionData().getProfile() != null
					&& authenticationController.getCurrentUserSessionData().getProfile().getUserName() != null) {
				final String username = authenticationController.getCurrentUserSessionData().getProfile().getUserName();
				authenticationController.loginUser(username, existingPassword, new AsyncCallback<String>() {				
					@Override
					public void onSuccess(String userSessionJson) {
						userService.changePassword(authenticationController.getCurrentUserSessionToken(), newPassword, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								view.showPasswordChangeSuccess();								
								// login user as session token has changed
								authenticationController.loginUser(username, newPassword, new AsyncCallback<String>() {
									@Override
									public void onSuccess(String result) {
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
								view.passwordChangeFailed();
								view.showErrorMessage("Password Change failed. Please try again.");
							}
						});
					}
					
					@Override
					public void onFailure(Throwable caught) {
						view.passwordChangeFailed();
						view.showErrorMessage("Incorrect username or password. Please enter your existing Synapse password.<br/><br/>If you have not setup a Synapse password, please see your Settings page to do so.");
					}
				});
				
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		} else {
			view.passwordChangeFailed();
			view.showInfo("Error","Reset Password failed. Please Login again.");
			goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}

	@Override
	public void createSynapsePassword() {
		if(authenticationController.isLoggedIn()) {
			String primaryEmail = DisplayUtils.getPrimaryEmail(authenticationController.getCurrentUserSessionData().getProfile());
			userService.sendPasswordResetEmail(primaryEmail, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					view.showRequestPasswordEmailSent();
					view.showInfo("Email Sent","You have been sent an email. Please check your inbox.");
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.requestPasswordEmailFailed();
					view.showErrorMessage("An error occurred. Please try reloading the page.");					
				}
			});
		} else {	
			view.requestPasswordEmailFailed();
			view.showInfo("Error", "Please Login Again.");
			goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}		
	}
	
	private void updateUserStorage() {
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
    public String mayStop() {
        view.clear();
        return null;
    }
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void updateMyNotificationSettings(final boolean sendEmailNotifications, final boolean markEmailedMessagesAsRead){
		//get my profile
		synapseClient.getUserProfile(null, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userProfileJson) {
				try {
					final UserProfile myProfile = new UserProfile(adapterFactory.createNew(userProfileJson));
					org.sagebionetworks.repo.model.message.Settings settings = myProfile.getNotificationSettings();
					if (settings == null) {
						settings = new org.sagebionetworks.repo.model.message.Settings();
						settings.setMarkEmailedMessagesAsRead(false);
						settings.setSendEmailNotifications(true);
						myProfile.setNotificationSettings(settings);
					}
					settings.setSendEmailNotifications(sendEmailNotifications);
					settings.setMarkEmailedMessagesAsRead(markEmailedMessagesAsRead);
					JSONObjectAdapter adapter = myProfile.writeToJSONObject(adapterFactory.createNew());
					userProfileJson = adapter.toJSONString();
					synapseClient.updateUserProfile(userProfileJson, new AsyncCallback<Void>() {
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
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}    				
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(caught.getMessage());
				}
			}
		});
	}
	
	private void showView(Settings place) {
		//String token = place.toToken();
		//Support other tokens?
		updateUserStorage();
	}
}

