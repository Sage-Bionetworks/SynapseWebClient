package org.sagebionetworks.web.client.presenter;

import java.util.Date;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileFormWidget implements ProfileFormView.Presenter {
		
	private ProfileFormView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private UserProfile ownerProfile;
	private ProfileUpdatedCallback profileUpdatedCallback;
	private AdapterFactory adapterFactory;
	private GlobalApplicationState globalApplicationState;
	
	private LinkedInServiceAsync linkedInService;
	private CookieProvider cookieProvider;
	private GWTWrapper gwt;
	
	@Inject
	public ProfileFormWidget(ProfileFormView view,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AdapterFactory adapterFactory,
			CookieProvider cookieProvider,
			LinkedInServiceAsync linkedInService,
			GWTWrapper gwt) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		this.cookieProvider = cookieProvider;
		this.linkedInService = linkedInService;
		this.gwt = gwt;
		view.setPresenter(this);
	}
	
	public interface ProfileUpdatedCallback {
		void profileUpdateSuccess();
		void onFailure(Throwable caught);
	}

	public void configure(UserProfile userProfile, ProfileUpdatedCallback profileUpdatedCallback) {
		ownerProfile = userProfile;
		this.profileUpdatedCallback = profileUpdatedCallback;
		view.updateView(userProfile);
	}
	
	@Override
	public void rollback() {
		stopEditing();
		view.updateView(ownerProfile);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void updateProfile(final String firstName, final String lastName, final String summary, final String position, final String location, final String industry, final String company, final String email, final String imageFileHandleId, final String teamName, final String url, final String userName) {				
		final UserSessionData currentUser = authenticationController.getCurrentUserSessionData();			
		if(currentUser != null) {
				//check for valid url
				if (!LoginPresenter.isValidUrl(url, true)) {
					view.showInvalidUrlUi();
					return;
				}
				//will only update username if it is set.  if cleared out it will keep the existing username
				if (userName != null && !LoginPresenter.isValidUsername(userName)) {
					view.showInvalidUsernameUi();
					return;
				}
				
				//get the owner profile (may or may not be currently set
				ProfileFormWidget.getMyProfile(synapseClient, adapterFactory, new AsyncCallback<UserProfile>() {
					@Override
					public void onSuccess(UserProfile profile) {
						ownerProfile = profile;
						ownerProfile.setFirstName(firstName);
						ownerProfile.setLastName(lastName);
						ownerProfile.setSummary(summary);
						ownerProfile.setPosition(position);
						ownerProfile.setLocation(location);
						ownerProfile.setIndustry(industry);
						ownerProfile.setCompany(company);
						ownerProfile.setDisplayName(firstName + " " + lastName);
						if (teamName != null)
							ownerProfile.setTeamName(teamName);
						if (url != null)
							ownerProfile.setUrl(url);
						if (userName != null)
							ownerProfile.setUserName(userName);
						final boolean isUpdatingEmail = email != null && !email.equals(profile.getEmail()); 
						if (isUpdatingEmail) {
							ownerProfile.setEmail(email);
						}
							
						ownerProfile.setProfilePicureFileHandleId(imageFileHandleId);
						
						synapseClient.updateUserProfile(ownerProfile, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								view.showUserUpdateSuccess();
								stopEditing();
								updateLoginInfo(currentUser);	
							}
							
							@Override
							public void onFailure(Throwable caught) {
								view.userUpdateFailed();
								profileUpdatedCallback.onFailure(caught);
							}
						});
					}
					@Override
					public void onFailure(Throwable caught) {
						profileUpdatedCallback.onFailure(caught);
					}
				});
		}
	}
	
	@Override
	public void startEditing() {
		globalApplicationState.setIsEditing(true);
		view.setIsDataModified(true);
	}
	
	@Override
	public void stopEditing() {
		globalApplicationState.setIsEditing(false);
		view.setIsDataModified(false);
	}
	
	public static void getMyProfile(SynapseClientAsync synapseClient, final AdapterFactory adapterFactory, final AsyncCallback<UserProfile> callback){
		synapseClient.getUserProfile(null, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile userProfile) {
				callback.onSuccess(userProfile);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void setUpdateButtonText(String text){
		view.setUpdateButtonText(text);
	}
	
	private void updateLoginInfo(UserSessionData currentUser) {
		AsyncCallback<UserSessionData> callback = new AsyncCallback<UserSessionData>() {
			@Override
			public void onFailure(Throwable caught) {
				sendSuccessMessageBackToOwner();
			}

			@Override
			public void onSuccess(UserSessionData result) {
				sendSuccessMessageBackToOwner();
			}
			
			public void sendSuccessMessageBackToOwner() {
				if (profileUpdatedCallback != null)
					profileUpdatedCallback.profileUpdateSuccess();
				else 
					globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
			}
		};
		authenticationController.revalidateSession(currentUser.getSession().getSessionToken(), callback);
	}
	
	@Override
	public void redirectToLinkedIn() {
		linkedInService.returnAuthUrl(gwt.getHostPageBaseURL(), new AsyncCallback<LinkedInInfo>() {
			@Override
			public void onSuccess(LinkedInInfo result) {
				// Store the requestToken secret in a cookie, set to expire in five minutes
				Date date = new Date(System.currentTimeMillis() + 300000);
				cookieProvider.setCookie(CookieKeys.LINKEDIN, result.getRequestSecret(), date);
				// Open the LinkedIn authentication window in the same tab
				Window.open(result.getAuthUrl(), "_self", "");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage("An error occurred. Please try reloading the page.");					
			}
		});
	}
	
	/**
	 * This method will update the current user's profile using LinkedIn
	 */
	public void updateProfileWithLinkedIn(String requestToken, String verifier) {
		// Grab the requestToken secret from the cookie. If it's expired, show an error message.
		// If not, grab the user's info for an update.
		String secret = cookieProvider.getCookie(CookieKeys.LINKEDIN);
		if(secret == null || secret.equals("")) {
			view.showErrorMessage("You request has timed out. Please reload the page and try again.");
		} else {
			linkedInService.getCurrentUserInfo(requestToken, secret, verifier, gwt.getHostPageBaseURL(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					//parse the LinkedIn UserProfile json
					UserProfile linkedInProfile;
					try {
						linkedInProfile = new UserProfile(adapterFactory.createNew(result));
						updateProfile(linkedInProfile.getFirstName(), linkedInProfile.getLastName(), 
						    		linkedInProfile.getSummary(), linkedInProfile.getPosition(), 
						    		linkedInProfile.getLocation(), linkedInProfile.getIndustry(), 
						    		linkedInProfile.getCompany(), null, linkedInProfile.getProfilePicureFileHandleId(), null, null, null);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						view.showErrorMessage("An error occurred. Please try reloading the page.");									
				}
			});
		}
	}
	
}
	
