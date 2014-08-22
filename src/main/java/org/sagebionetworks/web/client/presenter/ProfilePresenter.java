package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ProfilePresenter extends AbstractActivity implements ProfileView.Presenter, Presenter<Profile> {
		
	private Profile place;
	private ProfileView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	
	private ProfileFormWidget profileForm;
	private GWTWrapper gwt;
	private AdapterFactory adapterFactory;
	private SearchServiceAsync searchService;
	private ProfileUpdatedCallback profileUpdatedCallback;
	private SynapseJSNIUtils synapseJSNIUtils;
	private CookieProvider cookies;
	private RequestBuilderWrapper requestBuilder;
	private int teamNotificationCount;
	private String currentUserId;
	private boolean isOwner;
	
	@Inject
	public ProfilePresenter(ProfileView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			CookieProvider cookieProvider,
			GWTWrapper gwt, JSONObjectAdapter jsonObjectAdapter,
			ProfileFormWidget profileForm,
			AdapterFactory adapterFactory,
			SearchServiceAsync searchService,
			SynapseJSNIUtils synapseJSNIUtils, 
			RequestBuilderWrapper requestBuilder) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.adapterFactory = adapterFactory;
		this.profileForm = profileForm;
		this.searchService = searchService;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.requestBuilder = requestBuilder;
		this.cookies = cookieProvider;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
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
	
	public void editMyProfile() {
		if (checkIsLoggedIn())
			goTo(new Profile(authenticationController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
	}
	
	public void viewMyProfile() {
		if (checkIsLoggedIn())
			goTo(new Profile(authenticationController.getCurrentUserPrincipalId()));
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void updateArea(ProfileArea area) {
		if (area != null) {
			place.setArea(area);
			place.setNoRestartActivity(true);
			globalApplicationState.getPlaceChanger().goTo(place);
		}
	}
	
	/**
	 * This method will update the current user's profile using LinkedIn
	 */
	@Override
	public void updateProfileWithLinkedIn(String requestToken, String verifier) {
		profileForm.updateProfileWithLinkedIn(requestToken, verifier);
	}
	
	private void updateProfileView(String userId, final ProfileArea initialTab) {
		view.clear();
		isOwner = authenticationController.isLoggedIn() && authenticationController.getCurrentUserPrincipalId().equals(userId);
		currentUserId = userId == null ? authenticationController.getCurrentUserPrincipalId() : userId;
		synapseClient.getUserProfile(currentUserId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String userProfileJson) {
					try {
						final UserProfile profile = new UserProfile(adapterFactory.createNew(userProfileJson));
						if (isOwner) {
							//only configure the profile form (editor) if owner of this profile
							profileForm.configure(profile, profileUpdatedCallback);
						}
						
						getIsCertifiedAndUpdateView(profile, isOwner, initialTab);
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
	
	public void getIsCertifiedAndUpdateView(final UserProfile profile, final boolean isOwner, final ProfileArea area) {
		synapseClient.getCertifiedUserPassingRecord(profile.getOwnerId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.updateView(profile, isOwner, passingRecord, profileForm.asWidget(), area);
					proceed();
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException)
					view.updateView(profile, isOwner, null, profileForm.asWidget(), area);
				else
					view.showErrorMessage(caught.getMessage());
				
				proceed();
			}
			
			private void proceed() {
				getUserProjects(profile.getOwnerId());
				refreshTeams();
				if (isOwner) {
					getFavorites();
				}
			}
		});
	}
	
	@Override
	public void refreshTeams() {
		teamNotificationCount = 0;
		getTeamsAndChallenges(currentUserId);
	}
	
	public void getTeamsAndChallenges(String userId) {
		AsyncCallback<List<Team>> teamCallback = new AsyncCallback<List<Team>>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setTeamsError(caught.getMessage());
			}
			@Override
			public void onSuccess(List<Team> teams) {
				view.setTeams(teams,isOwner);
				getChallenges(teams);
			}
		};
		TeamListWidget.getTeams(userId, synapseClient, adapterFactory, teamCallback);
	}
	
	public void getChallenges(List<Team> teams) {
		//show challenges associated with the user
		getChallengeProjectIds(teams);
	}
	
	public void getChallengeProjectIds(final List<Team> myTeams) {
		getTeamId2ChallengeIdWhitelist(new CallbackP<JSONObjectAdapter>() {
			@Override
			public void invoke(JSONObjectAdapter mapping) {
				Set<String> challengeEntities = new HashSet<String>();
				for (Team team : myTeams) {
					if (mapping.has(team.getId())) {
						try {
							challengeEntities.add(mapping.getString(team.getId()));
						} catch (JSONObjectAdapterException e) {
							//problem with one of the mapping entries
						}
					}
				}
				getChallengeProjectHeaders(challengeEntities);
			}
		});
	}
	
	public void getTeamId2ChallengeIdWhitelist(final CallbackP<JSONObjectAdapter> callback) {
		String responseText = cookies.getCookie(HomePresenter.TEAMS_2_CHALLENGE_ENTITIES_COOKIE);
		
		if (responseText != null) {
			parseTeam2ChallengeWhitelist(responseText, callback);
			return;
		}
		requestBuilder.configure(RequestBuilder.GET, DisplayUtils.createRedirectUrl(synapseJSNIUtils.getBaseFileHandleUrl(), gwt.encodeQueryString(ClientProperties.TEAM2CHALLENGE_WHITELIST_URL)));
	     try
	     {
	    	 requestBuilder.sendRequest(null, new RequestCallback() {
	            @Override
	            public void onError(Request request, Throwable exception) 
	            {
	            	//do nothing, may or may not have any challenges
	            }

	            @Override
	            public void onResponseReceived(Request request,Response response) 
	            {
	            	String responseText = response.getText();
	            	Date expires = new Date(System.currentTimeMillis() + 1000*60*60*24); // store for a day
	            	cookies.setCookie(HomePresenter.TEAMS_2_CHALLENGE_ENTITIES_COOKIE, responseText, expires);
	            	parseTeam2ChallengeWhitelist(responseText, callback);
	            }

	         });
	     }
	     catch (Exception e){
         	//failed to load my challenges
	    	 view.setChallengesError("Could not load Challenges: " + e.getMessage());
	     }
	}
	
	private void parseTeam2ChallengeWhitelist(String responseText, CallbackP<JSONObjectAdapter> callback){
		try {
			callback.invoke(adapterFactory.createNew(responseText));
		} catch (Throwable e) {
			//just in case there is a parsing exception
		}
	}
	
	public void getChallengeProjectHeaders(final Set<String> challengeProjectIdsSet) {
		List<String> challengeProjectIds = new ArrayList<String>();
		challengeProjectIds.addAll(challengeProjectIdsSet);
		synapseClient.getEntityHeaderBatch(challengeProjectIds, new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> entityHeaderStrings) {
				try {
					//finally, we can tell the view to update the user challenges based on these entity headers
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for (String headerString : entityHeaderStrings) {
						EntityHeader header = new EntityHeader(adapterFactory.createNew(headerString));
						headers.add(header);
					}
					
					//sort by name
					Collections.sort(headers, new Comparator<EntityHeader>() {
				        @Override
				        public int compare(EntityHeader o1, EntityHeader o2) {
				        	return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
				        }
					});
					
					view.setChallenges(headers);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}	
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setChallengesError("Could not load Challenges:" + caught.getMessage());
			}
		});
	}
	
	public void getUserProjects(String userId) {
		EntityBrowserUtils.loadUserUpdateable(userId, searchService, adapterFactory, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setProjects(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setProjectsError("Could not load Projects: " + caught.getMessage());
			}
		});
	}
	
	public void getFavorites() {
		EntityBrowserUtils.loadFavorites(synapseClient, adapterFactory, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setFavorites(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setFavoritesError("Could not load Favorites: " + caught.getMessage());
			}
		});
	}
	
	@Override
	public void createProject(final String name) {
		//validate project name
		if (!DisplayUtils.isDefined(name)) {
			view.showErrorMessage(DisplayConstants.PLEASE_ENTER_PROJECT_NAME);
			return;
		}
		
		CreateEntityUtil.createProject(name, synapseClient, adapterFactory, globalApplicationState, authenticationController, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String newProjectId) {
				view.showInfo(DisplayConstants.LABEL_PROJECT_CREATED, name);
				globalApplicationState.getPlaceChanger().goTo(new Synapse(newProjectId));						
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof ConflictException) {
					view.showErrorMessage(DisplayConstants.WARNING_PROJECT_NAME_EXISTS);
				} else {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					} 
				}
			}
		});
	}
	

	@Override
	public void createTeam(final String teamName) {
		//validate team name
		if (!DisplayUtils.isDefined(teamName)) {
			view.showErrorMessage(DisplayConstants.PLEASE_ENTER_TEAM_NAME);
			return;
		}

		synapseClient.createTeam(teamName, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String newTeamId) {
				view.showInfo(DisplayConstants.LABEL_TEAM_CREATED, teamName);
				globalApplicationState.getPlaceChanger().goTo(new org.sagebionetworks.web.client.place.Team(newTeamId));						
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof ConflictException) {
					view.showErrorMessage(DisplayConstants.WARNING_TEAM_NAME_EXISTS);
				} else {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {					
						view.showErrorMessage(caught.getMessage());
					}
				}
			}
		});
	}
	
	private boolean checkIsLoggedIn() {
		if (!authenticationController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			return false;
		}
		return true;
	}
	
	private void setupProfileFormCallback() {
		profileUpdatedCallback = new ProfileUpdatedCallback() {
			@Override
			public void profileUpdateSuccess() {
				view.showInfo("Success", "Your profile has been updated.");
				continueToEditProfile();
			}
			
			public void continueToEditProfile() {
				editMyProfile();
				view.refreshHeader();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {
					view.showErrorMessage(caught.getMessage());
				}
				continueToEditProfile();
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
		if (authenticationController.isLoggedIn() && authenticationController.getCurrentUserPrincipalId().equals(place.getUserId())) {
			//View my profile
			updateProfileView(place.getUserId(), place.getArea());
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
				editMyProfile();
			} else {
				//otherwise, this is a user id
				updateProfileView(place.getUserId(), place.getArea());
			}
		}
	}
	
	@Override
	public void updateTeamInvites(List<MembershipInvitationBundle> invites) {
		if (invites != null && invites.size() > 0) {
			teamNotificationCount += invites.size();
			//update team notification count
			if (teamNotificationCount > 0)
				view.setTeamNotificationCount(Integer.toString(teamNotificationCount));
}
	}

	@Override
	public void addMembershipRequests(int count) {
		teamNotificationCount += count;
		if (teamNotificationCount > 0)
			view.setTeamNotificationCount(Integer.toString(teamNotificationCount));
	}
	
	/**
	 * Exposed for test purposes only
	 */
	public int getTeamNotificationCount() {
		return teamNotificationCount;
	}
	
	public void setTeamNotificationCount(int teamNotificationCount) {
		this.teamNotificationCount = teamNotificationCount;
	}
}

