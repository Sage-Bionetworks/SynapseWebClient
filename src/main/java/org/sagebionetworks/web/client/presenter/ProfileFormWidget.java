package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileFormWidget implements ProfileFormView.Presenter {
		
	private ProfileFormView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private UserProfile ownerProfile;
	private JSONObjectAdapter jsonObjectAdapter;
	private ProfileUpdatedCallback profileUpdatedCallback;
	private AdapterFactory adapterFactory;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public ProfileFormWidget(ProfileFormView view,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}
	
	public interface ProfileUpdatedCallback {
		void profileUpdateCancelled();
		void profileUpdateSuccess();
		void onFailure(Throwable caught);
	}

	public void configure(UserProfile userProfile, ProfileUpdatedCallback profileUpdatedCallback) {
		ownerProfile = userProfile;
		this.profileUpdatedCallback = profileUpdatedCallback;
		view.updateView(userProfile);
	}
	
	public void hideCancelButton(){
		view.hideCancelButton();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void updateProfile(final String firstName, final String lastName, final String summary, final String position, final String location, final String industry, final String company, final String email, final AttachmentData pic, final String teamName, final String url, final String userName) {				
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
						try {
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
								
							if (pic != null)
								ownerProfile.setPic(pic);
							
							JSONObjectAdapter adapter = ownerProfile.writeToJSONObject(jsonObjectAdapter.createNew());
							String userProfileJson = adapter.toJSONString();

							synapseClient.updateUserProfile(userProfileJson, new AsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									view.showUserUpdateSuccess();
									updateLoginInfo(currentUser);	
								}
								
								@Override
								public void onFailure(Throwable caught) {
									view.userUpdateFailed();
									profileUpdatedCallback.onFailure(caught);
								}
							});
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}    				
					}
					@Override
					public void onFailure(Throwable caught) {
						profileUpdatedCallback.onFailure(caught);
					}
				});
		}
	}
	
	
	public static void getMyProfile(SynapseClientAsync synapseClient, final AdapterFactory adapterFactory, final AsyncCallback<UserProfile> callback){
		synapseClient.getUserProfile(null, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userProfileJson) {
				try {
					callback.onSuccess(new UserProfile(adapterFactory.createNew(userProfileJson)));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}    				
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void cancelClicked() {
		profileUpdatedCallback.profileUpdateCancelled();
	}
	
	public void setUpdateButtonText(String text){
		view.setUpdateButtonText(text);
	}
	
	private void updateLoginInfo(UserSessionData currentUser) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				sendSuccessMessageBackToOwner();
			}

			@Override
			public void onSuccess(String result) {
				sendSuccessMessageBackToOwner();
			}
			
			public void sendSuccessMessageBackToOwner() {
				if (profileUpdatedCallback != null)
					profileUpdatedCallback.profileUpdateSuccess();
				else 
					globalApplicationState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_PLACE_TOKEN));
			}
		};

		if(currentUser.getIsSSO()) {
			authenticationController.loginUserSSO(currentUser.getSession().getSessionToken(), callback);
		} else {
			authenticationController.loginUser(currentUser.getSession().getSessionToken(), callback);
		}

	}
}
	
