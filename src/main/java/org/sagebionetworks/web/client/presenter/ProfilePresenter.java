package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.EntityType;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
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
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget.ProfileUpdatedCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.Place;
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
	private ProfileUpdatedCallback profileUpdatedCallback;
	private SynapseJSNIUtils synapseJSNIUtils;
	private CookieProvider cookies;
	private RequestBuilderWrapper requestBuilder;
	private int teamNotificationCount;
	private String currentUserId;
	private boolean isOwner;
	private int currentOffset;
	public final static int PROJECT_PAGE_SIZE=100;
	public ProjectFilterEnum filterType;
	public Team filterTeam;
	
	@Inject
	public ProfilePresenter(ProfileView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			CookieProvider cookieProvider,
			GWTWrapper gwt, JSONObjectAdapter jsonObjectAdapter,
			ProfileFormWidget profileForm,
			AdapterFactory adapterFactory,
			SynapseJSNIUtils synapseJSNIUtils, 
			RequestBuilderWrapper requestBuilder) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.adapterFactory = adapterFactory;
		this.profileForm = profileForm;
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
		if (area != null && !area.equals(place.getArea())) {
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
		view.showLoading();
		isOwner = authenticationController.isLoggedIn() && authenticationController.getCurrentUserPrincipalId().equals(userId);
		currentUserId = userId == null ? authenticationController.getCurrentUserPrincipalId() : userId;
		synapseClient.getUserProfile(currentUserId, new AsyncCallback<UserProfile>() {
				@Override
				public void onSuccess(UserProfile profile) {
						if (isOwner) {
							//only configure the profile form (editor) if owner of this profile
							profileForm.configure(profile, profileUpdatedCallback);
						}
						
						getIsCertifiedAndUpdateView(profile, isOwner, initialTab);
					}
				@Override
				public void onFailure(Throwable caught) {
					view.hideLoading();
					DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);    					    				
				}
			});
	}
	
	public void getIsCertifiedAndUpdateView(final UserProfile profile, final boolean isOwner, final ProfileArea area) {
		synapseClient.getCertifiedUserPassingRecord(profile.getOwnerId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					view.hideLoading();
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					view.updateView(profile, isOwner, passingRecord, profileForm.asWidget());
					tabClicked(area);
					proceed();
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				if (caught instanceof NotFoundException) {
					view.updateView(profile, isOwner, null, profileForm.asWidget());
					tabClicked(area);
				}
				else
					view.showErrorMessage(caught.getMessage());
				
				proceed();
			}
			
			private void proceed() {
				setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
				refreshTeams();
				if (isOwner) {
					getFavorites();
				}
			}
		});
	}
	
	public void refreshProjects() {
		currentOffset = 0;
		view.clearProjects();
		getMoreProjects();
	}
	
	/**
	 * Sets the project filter.  If filtered to a specific team, then the Team argument will be used.
	 * @param filterType
	 * @param team
	 */
	public void setProjectFilterAndRefresh(ProjectFilterEnum filterType, Team team) {
		this.filterType =filterType;
		filterTeam = team;
		updateFilterView();
		refreshProjects();
	}

	public void getMoreProjects() {
		if (isOwner) {
			//this depends on the active filter
			switch (filterType) {
				case ALL:
					getAllMyProjects(currentOffset);
					break;
				case MINE:
					getProjectsCreatedByMe(currentOffset);
					break;
				case TEAM:
					getTeamProjects(currentOffset);
					break;
				default:
					break;
			}
		} else
			getUserProjects(currentOffset);
	}
	
	public void updateFilterView() {
		if (isOwner) {
			view.showProjectFiltersUI();
			
			//this depends on the active filter
			switch (filterType) {
				case ALL:
					view.setAllProjectFilterSelected();
					view.setProjectHighlightBoxText("All Projects");
					break;
				case MINE:
					view.setMyProjectFilterSelected();
					view.setProjectHighlightBoxText("My Projects");
					break;
				case TEAM:
					view.setTeamProjectFilterSelected(filterTeam);
					view.setProjectHighlightBoxText("Team Projects (" + filterTeam.getName() + ")");
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public void refreshTeams() {
		teamNotificationCount = 0;
		view.clearTeamNotificationCount();
		if (isOwner)
			view.refreshTeamInvites();
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
		synapseClient.getEntityHeaderBatch(challengeProjectIds, new AsyncCallback<ArrayList<EntityHeader>>() {
			
			@Override
			public void onSuccess(ArrayList<EntityHeader> headers) {
					//finally, we can tell the view to update the user challenges based on these entity headers
					
					//sort by name
					Collections.sort(headers, new Comparator<EntityHeader>() {
				        @Override
				        public int compare(EntityHeader o1, EntityHeader o2) {
				        	return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
				        }
					});
					
					view.setChallenges(headers);
				}	
			
			@Override
			public void onFailure(Throwable caught) {
				view.setChallengesError("Could not load Challenges:" + caught.getMessage());
			}
		});
	}
	
	public void getAllMyProjects(int offset) {
		view.showProjectsLoading(true);
		synapseClient.getMyProjects(PROJECT_PAGE_SIZE, offset, new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				addProjectResults(projectHeaders.getResults(), projectHeaders.getTotalNumberOfResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load my projects:" + caught.getMessage());
			}
		});
	}
	
	public void getTeamProjects(int offset) {
		view.showProjectsLoading(true);
		synapseClient.getProjectsForTeam(filterTeam.getId(), PROJECT_PAGE_SIZE, offset, new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				addProjectResults(projectHeaders.getResults(), projectHeaders.getTotalNumberOfResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load team projects:" + caught.getMessage());
			}
		});
	}

	public void getProjectsCreatedByMe(int offset) {
		view.showProjectsLoading(true);

		EntityQuery childrenQuery = createGetProjectsQuery(currentUserId, PROJECT_PAGE_SIZE, offset);
		synapseClient.executeEntityQuery(childrenQuery, new AsyncCallback<EntityQueryResults>() {
			@Override
			public void onSuccess(EntityQueryResults results) {
				List<ProjectHeader> headers = getHeadersFromQueryResults(results);
				addProjectResults(headers, results.getTotalEntityCount().intValue());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load projects I created:" + caught.getMessage());
			}
		});
	}
	
	public void getUserProjects(int offset) {
		view.showProjectsLoading(true);
		synapseClient.getUserProjects(currentUserId, PROJECT_PAGE_SIZE, offset, new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				List<ProjectHeader> headers = projectHeaders.getResults();
				addProjectResults(headers, projectHeaders.getTotalNumberOfResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load user projects:" + caught.getMessage());
			}
		});
	}
	
	public void addProjectResults(List<ProjectHeader> headers, int totalCount) {
		view.showProjectsLoading(false);
		view.addProjects(headers);
		projectPageAdded(totalCount);
	}
	
	public void projectPageAdded(int totalNumberOfResults) {
		currentOffset += PROJECT_PAGE_SIZE;
		view.setIsMoreProjectsVisible(currentOffset < totalNumberOfResults);
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
		view.clear();
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
	
	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public int getCurrentOffset() {
		return currentOffset;
	}

	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public void setCurrentOffset(int currentOffset) {
		this.currentOffset = currentOffset;
	}
	
	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public boolean isOwner() {
		return isOwner;
	}
	
	@Override
	public void tabClicked(final ProfileArea tab) {
		if (tab == null) {
			view.showErrorMessage("The selected tab is undefined.");
			return;
		}
		//if we are editing, then pop up a confirm
		if (globalApplicationState.isEditing()) {
			Callback yesCallback = new Callback() {
				@Override
				public void invoke() {
					profileForm.rollback();
					view.setTabSelected(tab);
				}
			};
			view.showConfirmDialog("", DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE, yesCallback);
		} else
			view.setTabSelected(tab);
	}
	
	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public void setIsOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}
	
	@Override
	public void certificationBadgeClicked() {
		goTo(new Certificate(currentUserId));
	}
	
	@Override
	public void applyFilterClicked() {
		if (view.isAllProjectFilterSelected()) {
			setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
		} else if (view.isMyProjectFilterSelected()) {
			setProjectFilterAndRefresh(ProjectFilterEnum.MINE, null);
		} else {
			//must be a team filter
			setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, view.getSelectedTeamFilter());
		}
	}
	
	@Override
	public void cancelFilterClicked() {
		//update the filter in the view based on the previously selected filter
		updateFilterView();
	}
	
	public EntityQuery createGetProjectsQuery(String creatorUserId, long limit, long offset) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition creatorCondition = EntityQueryUtils.buildCondition(EntityFieldName.createdByPrincipalId, Operator.EQUALS, creatorUserId);
		newQuery.setConditions(Arrays.asList(creatorCondition));
		newQuery.setFilterByType(EntityType.project);
		newQuery.setLimit(limit);
		newQuery.setOffset(offset);
		return newQuery;
	}
	
	public List<ProjectHeader> getHeadersFromQueryResults(EntityQueryResults results) {
		List<ProjectHeader> headerList = new LinkedList<ProjectHeader>();
		for (EntityQueryResult result : results.getEntities()) {
			ProjectHeader header = new ProjectHeader();
			header.setId(result.getId());
			header.setName(result.getName());
			headerList.add(header);
		}
		return headerList;
	}

	/**
	 * For testing purposes only
	 * @param currentUserId
	 */
	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}
	
	/**
	 * For testing purposes only
	 */
	public ProjectFilterEnum getFilterType() {
		return filterType;
	}
}

