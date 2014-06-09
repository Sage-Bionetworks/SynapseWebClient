package org.sagebionetworks.web.client.presenter;

import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ProfilePresenter extends AbstractActivity implements ProfileView.Presenter, Presenter<Profile> {
		
	private Profile place;
	private ProfileView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private LinkedInServiceAsync linkedInService;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookieProvider;
	private UserProfile ownerProfile;
	private ProfileFormWidget profileForm;
	private GWTWrapper gwt;
	private AdapterFactory adapterFactory;
	private ProfileUpdatedCallback profileUpdatedCallback;
	
	@Inject
	public ProfilePresenter(ProfileView view,
			AuthenticationController authenticationController,
			LinkedInServiceAsync linkedInService,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			CookieProvider cookieProvider,
			GWTWrapper gwt, JSONObjectAdapter jsonObjectAdapter,
			ProfileFormWidget profileForm,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.linkedInService = linkedInService;
		this.globalApplicationState = globalApplicationState;
		this.cookieProvider = cookieProvider;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.gwt = gwt;
		this.adapterFactory = adapterFactory;
		this.profileForm = profileForm;
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
	public void setPlace(Profile place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		showView(place);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
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
	
	@Override
	public void showEditProfile() {
		updateProfileView(authenticationController.getCurrentUserPrincipalId(), true);
	}
	@Override
	public void showViewMyProfile() {
		updateProfileView(authenticationController.getCurrentUserPrincipalId(), false);
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	/**
	 * This method will update the current user's profile using LinkedIn
	 */
	@Override
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
						linkedInProfile = nodeModelCreator.createJSONEntity(result, UserProfile.class);
						 profileForm.updateProfile(linkedInProfile.getFirstName(), linkedInProfile.getLastName(), 
						    		linkedInProfile.getSummary(), linkedInProfile.getPosition(), 
						    		linkedInProfile.getLocation(), linkedInProfile.getIndustry(), 
						    		linkedInProfile.getCompany(), null, linkedInProfile.getPic(), null, null, null);
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
	
	private void updateProfileView(final String userId, final boolean isEditing) {
		view.clear();
		final boolean isOwner = authenticationController.isLoggedIn() && authenticationController.getCurrentUserPrincipalId().equals(userId);
		globalApplicationState.setIsEditing(isEditing);
		final String targetUserId = userId == null ? authenticationController.getCurrentUserPrincipalId() : userId;
		synapseClient.getUserProfile(userId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String userProfileJson) {
					try {
						final UserProfile profile = nodeModelCreator.createJSONEntity(userProfileJson, UserProfile.class);
						if (isOwner) {
							ownerProfile = profile;
							//only configure the profile form (editor) if owner of this profile
							profileForm.configure(profile, profileUpdatedCallback);
						}
						
						AsyncCallback<List<Team>> teamCallback = new AsyncCallback<List<Team>>() {
							@Override
							public void onFailure(Throwable caught) {
								view.showErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(List<Team> teams) {
								getIsCertifiedAndUpdateView(profile, teams, isEditing, isOwner);
							}
						};
						TeamListWidget.getTeams(targetUserId, synapseClient, adapterFactory, teamCallback);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}    				
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);    					    				
				}
			});
	}
	
	public void getIsCertifiedAndUpdateView(final UserProfile profile, final List<Team> teams, final boolean isEditing, final boolean isOwner) {
		synapseClient.getCertifiedUserPassingRecord(profile.getOwnerId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.updateView(profile, teams, isEditing, isOwner, passingRecord, profileForm.asWidget());
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException)
					view.updateView(profile, teams, isEditing, isOwner, null, profileForm.asWidget());
				else
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public String getEmailAddress() {
		return ownerProfile != null ? DisplayUtils.getPrimaryEmail(ownerProfile) : null;
	}
	
	private void setupProfileFormCallback() {
		profileUpdatedCallback = new ProfileUpdatedCallback() {
			
			@Override
			public void profileUpdateSuccess() {
				view.showInfo("Success", "Your profile has been updated.");
				continueToViewProfile();
			}
			
			@Override
			public void profileUpdateCancelled() {
				continueToViewProfile();
			}
			
			public void continueToViewProfile() {
				showViewMyProfile();
				view.refreshHeader();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(caught.getMessage());
				}
			}
		};
	}
	
	private void loggedInCheck() {
		if (!authenticationController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}
	
	private void showView(Profile place) {
		setupProfileFormCallback();
		String token = place.toToken();
		if (authenticationController.isLoggedIn() && authenticationController.getCurrentUserPrincipalId().equals(token)) {
			//View my profile
			updateProfileView(token, false);
		}
		else if(!"".equals(token) && token != null) {
			//if this contains an oauth_token, it's from linkedin
			if (token.contains("oauth_token"))
			{
				// User just logged in to LinkedIn. Get the request token and their info to update
				// their profile with.

				//must be logged in
				loggedInCheck();

				String requestToken = "";
				String verifier = "";
				if (token.startsWith("?"))
					token = token.substring(1);
				String[] oAuthTokens = token.split("&");
				for(String s : oAuthTokens) {
					String[] tokenParts = s.split("=");
					if(tokenParts[0].equals("oauth_token")) {
						requestToken = tokenParts[1];
					} else if(tokenParts[0].equals("oauth_verifier")) {
						verifier = tokenParts[1];
					}
				}
				
				if(!requestToken.equals("") && !verifier.equals("")) {
					updateProfileWithLinkedIn(requestToken, verifier);
				} else {
					view.showErrorMessage("An error occurred. Please try reloading the page.");
				}
			} else if (Profile.EDIT_PROFILE_TOKEN.equals(token)) {
				showEditProfile();
			} else {
				//otherwise, this is a user id
				updateProfileView(token, false);
			}
		}
	}
}

