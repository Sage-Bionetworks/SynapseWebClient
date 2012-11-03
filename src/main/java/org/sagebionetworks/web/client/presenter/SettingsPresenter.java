package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SettingsPresenter extends AbstractActivity implements SettingsView.Presenter {
		
	private Settings place;
	private SettingsView view;
	private AuthenticationController authenticationController;
	private UserAccountServiceAsync userService;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookieProvider;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public SettingsPresenter(SettingsView view,
			AuthenticationController authenticationController,
			UserAccountServiceAsync userService,
			GlobalApplicationState globalApplicationState,
			CookieProvider cookieProvider,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.userService = userService;
		this.globalApplicationState = globalApplicationState;
		this.cookieProvider = cookieProvider;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Set the presenter on the view
		this.view.render();
		
		// Install the view
		panel.setWidget(view);
		
	}

	public void setPlace(Settings place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}

	@Override
	public void resetPassword(final String username, final String existingPassword, final String newPassword) {
		final UserSessionData currentUser = authenticationController.getLoggedInUser();
		if(currentUser != null) {
			authenticationController.loginUser(username, existingPassword, false, new AsyncCallback<String>() {				
				@Override
				public void onSuccess(String result) {
					// 2. set password
					userService.setPassword(newPassword, new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							view.showPasswordChangeSuccess();
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
			view.passwordChangeFailed();
			view.showInfo("Error","Reset Password failed. Please Login again.");
			goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}

	@Override
	public void createSynapsePassword() {
		final UserSessionData currentUser = authenticationController.getLoggedInUser();
		if(currentUser != null) {
			userService.sendSetApiPasswordEmail(new AsyncCallback<Void>() {
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
					StorageUsageSummaryList storageUsageSummaryList = nodeModelCreator.createEntity(storageUsageSummaryListJson, StorageUsageSummaryList.class);
					view.updateStorageUsage(storageUsageSummaryList.getTotalSize());
				} catch (RestServiceException e) {
					onFailure(e);
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
	
	private void showView(Settings place) {
		//String token = place.toToken();
		//Support other tokens?
		updateUserStorage();
	}
}

