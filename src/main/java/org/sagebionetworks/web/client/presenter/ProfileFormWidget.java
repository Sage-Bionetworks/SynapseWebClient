package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProfileFormView;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileFormWidget implements ProfileFormView.Presenter {
		
	private ProfileFormView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private UserProfile ownerProfile;
	private JSONObjectAdapter jsonObjectAdapter;
	private ProfileUpdatedCallback profileUpdatedCallback;
	
	@Inject
	public ProfileFormWidget(ProfileFormView view,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
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
	public void updateProfile(final String firstName, final String lastName, final String summary, final String position, final String location, final String industry, final String company, final String email, final AttachmentData pic, final String teamName, final String url) {
		final UserSessionData currentUser = authenticationController.getLoggedInUser();
		if(currentUser != null) {
				//get the owner profile (may or may not be currently set
				synapseClient.getUserProfile(null, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String userProfileJson) {
						try {
							final UserProfile profile = nodeModelCreator.createJSONEntity(userProfileJson, UserProfile.class);
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
							final boolean isUpdatingEmail = email != null && !email.equals(profile.getEmail()); 
							if (isUpdatingEmail) {
								ownerProfile.setEmail(email);
							}
								
							if (pic != null)
								ownerProfile.setPic(pic);
							
							JSONObjectAdapter adapter = ownerProfile.writeToJSONObject(jsonObjectAdapter.createNew());
							userProfileJson = adapter.toJSONString();

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
	
	@Override
	public void cancelClicked() {
		profileUpdatedCallback.profileUpdateCancelled();
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
				profileUpdatedCallback.profileUpdateSuccess();
			}
		};

		if(currentUser.getIsSSO()) {
			authenticationController.loginUserSSO(currentUser.getSessionToken(), callback);
		} else {
			authenticationController.loginUser(currentUser.getSessionToken(), callback);
		}

	}
}
	
