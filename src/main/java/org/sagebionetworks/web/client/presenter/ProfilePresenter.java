package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.TeamRequestBundle;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class ProfilePresenter extends AbstractActivity implements ProfileView.Presenter, Presenter<Profile> {
		
	public static final String USER_PROFILE_VISIBLE_STATE_KEY = "org.sagebionetworks.synapse.user.profile.visible.state";
	public static final String USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY = "org.sagebionetworks.synapse.user.profile.certification.message.visible.state";
	
	private Profile place;
	private ProfileView view;
	private SynapseClientAsync synapseClient;
	private ChallengeClientAsync challengeClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookies;
	private UserProfileModalWidget userProfileModalWidget;
	private LinkedInServiceAsync linkedInService;
	private GWTWrapper gwt;
	private OpenTeamInvitationsWidget openInvitesWidget;

	private PortalGinInjector ginInjector;
	private AdapterFactory adapterFactory;
	private int inviteCount;
	private int openRequestCount;
	private String currentUserId;
	private boolean isOwner;
	private int currentProjectOffset, currentChallengeOffset;
	public final static int PROJECT_PAGE_SIZE=100;
	public final static int CHALLENGE_PAGE_SIZE=100;
	public ProjectFilterEnum filterType;
	public Team filterTeam;
	public SortOptionEnum currentProjectSort;
	public TeamListWidget myTeamsWidget;
	public SynapseAlert profileSynAlert;
	public SynapseAlert projectSynAlert;
	public SynapseAlert teamSynAlert;

	
	@Inject
	public ProfilePresenter(ProfileView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			ChallengeClientAsync challengeClient,
			CookieProvider cookies,
			UserProfileModalWidget userProfileModalWidget,
			LinkedInServiceAsync linkedInServic,
			GWTWrapper gwt,
			TeamListWidget myTeamsWidget,
			OpenTeamInvitationsWidget openInvitesWidget,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.ginInjector = ginInjector;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.challengeClient = challengeClient;
		this.cookies = cookies;
		this.userProfileModalWidget = userProfileModalWidget;
		this.linkedInService = linkedInServic;
		this.gwt = gwt;
		this.myTeamsWidget = myTeamsWidget;
		this.openInvitesWidget = openInvitesWidget;
		this.currentProjectSort = SortOptionEnum.LATEST_ACTIVITY;
		view.clearSortOptions();
		for (SortOptionEnum sort: SortOptionEnum.values()) {
			view.addSortOption(sort);
		}
		profileSynAlert = ginInjector.getSynapseAlertWidget();
		projectSynAlert = ginInjector.getSynapseAlertWidget();
		teamSynAlert = ginInjector.getSynapseAlertWidget();
		view.setPresenter(this);
		view.addUserProfileModalWidget(userProfileModalWidget);
		view.addMyTeamsWidget(myTeamsWidget);
		view.addOpenInvitesWidget(openInvitesWidget);
		view.setProfileSynAlertWidget(profileSynAlert.asWidget());
		view.setProjectSynAlertWidget(projectSynAlert.asWidget());
		view.setTeamSynAlertWidget(teamSynAlert.asWidget());
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
		resetSynAlertWidgets();
		showView(place);
	}
	
	private void resetSynAlertWidgets() {
		profileSynAlert.clear();
		projectSynAlert.clear();
		teamSynAlert.clear();
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

	// Configuration
	public void updateProfileView(String userId, final ProfileArea initialTab) {
		inviteCount = 0;
		openRequestCount = 0;
		isOwner = authenticationController.isLoggedIn()
				&& authenticationController.getCurrentUserPrincipalId().equals(
						userId);
		this.currentProjectSort = SortOptionEnum.LATEST_ACTIVITY;
		view.clear();
		view.showLoading();
		view.setSortText(currentProjectSort.sortText);
		view.setProfileEditButtonVisible(isOwner);
		//TODO: remove isInTestWebsite condition once UserBundle is in place and we can display orc id link.
		view.setOrcIDLinkButtonVisible(isOwner && DisplayUtils.isInTestWebsite(cookies));
		view.showTabs(isOwner);
		myTeamsWidget.clear();
		myTeamsWidget.configure(false);
		currentUserId = userId == null ? authenticationController.getCurrentUserPrincipalId() : userId;
		if (isOwner) {
			// make sure we have the user favorites before continuing
			initUserFavorites(new Callback() {
				@Override
				public void invoke() {
					getUserProfile(initialTab);
				}
			});
			tabClicked(initialTab);
		} else {
			if (initialTab == ProfileArea.SETTINGS) 
				getUserProfile(ProfileArea.PROJECTS);
			else
				getUserProfile(initialTab);
			tabClicked(initialTab == ProfileArea.SETTINGS ? ProfileArea.PROJECTS : initialTab);
		}
	}
	
	private void getUserProfile(final ProfileArea initialTab) {
		this.profileSynAlert.clear();
		synapseClient.getUserProfile(currentUserId, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile profile) {
					initializeShowHideProfile(isOwner);
					getIsCertifiedAndUpdateView(profile, isOwner);
				}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				profileSynAlert.handleException(caught);
			}
		});
	}
	
	public void getIsCertifiedAndUpdateView(final UserProfile profile, final boolean isOwner) {
		view.setSynapseEmailVisible(authenticationController.isLoggedIn());
		synapseClient.getCertifiedUserPassingRecord(profile.getOwnerId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String passingRecordJson) {
				try {
					view.hideLoading();
					PassingRecord passingRecord = new PassingRecord(adapterFactory.createNew(passingRecordJson));
					if (passingRecord != null)
						view.addCertifiedBadge();
					view.setProfile(profile, isOwner);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				if (caught instanceof NotFoundException) {
					view.setProfile(profile, isOwner);
					initializeShowHideCertification(isOwner);
				}
				else
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void initializeShowHideProfile(boolean isOwner) {
		if (isOwner) {
			boolean isProfileVisible = true;
			try {
				String cookieValue = cookies.getCookie(USER_PROFILE_VISIBLE_STATE_KEY);
				if (cookieValue != null && !cookieValue.isEmpty()) {
					isProfileVisible = Boolean.valueOf(cookieValue);	
				}
			} catch (Exception e) {
				//if there are any problems getting the profile visibility state, ignore and use default (show)
			}
			setIsProfileVisible(isProfileVisible);
		} else {
			//not the owner
			//show the profile, and hide the profile button
			setIsProfileVisible(true);
			view.setHideProfileButtonVisible(false);
		}
	}
	
	public void initializeShowHideCertification(boolean isOwner) {
		if (isOwner) {
			boolean isCertificationMessageVisible = false;
			try {
				String cookieValue = cookies.getCookie(USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + currentUserId);
				if (cookieValue == null || !cookieValue.equalsIgnoreCase("false")) {
					isCertificationMessageVisible = true;	
				}
			} catch (Exception e) {
				//if there are any problems getting the certification message visibility state, ignore and use default (hide)
			}
			view.setGetCertifiedVisible(isCertificationMessageVisible);
		} else {
			//not the owner
			//hide certification message
			view.setGetCertifiedVisible(false);
		}
	}
	
	@Override
	public void hideProfileButtonClicked() {
		setIsProfileVisible(false);
		setIsProfileVisibleCookie(false);
	}
	
	@Override
	public void showProfileButtonClicked() {
		setIsProfileVisible(true);
		setIsProfileVisibleCookie(true);
	}
	
	private void setIsProfileVisible(boolean isVisible) {
		if (isVisible){
			view.showProfile();
		} else {
			view.hideProfile();
		}
		view.setShowProfileButtonVisible(!isVisible);
		view.setHideProfileButtonVisible(isVisible);
	}
	
	public void setIsProfileVisibleCookie(boolean isVisible) {
		Date yearFromNow = new Date();
		CalendarUtil.addMonthsToDate(yearFromNow, 12);
		cookies.setCookie(USER_PROFILE_VISIBLE_STATE_KEY, Boolean.toString(isVisible), yearFromNow);
	}
	
	public void refreshProjects() {
		currentProjectOffset = 0;
		view.clearProjects();
		getMoreProjects();		
		//also refresh the teams tab
		refreshTeams();
	}
	
	public void refreshChallenges() {
		currentChallengeOffset = 0;
		view.clearChallenges();
		getMoreChallenges();
	}
	
	/**
	 * Sets the project filter.  If filtered to a specific team, then the Team argument will be used.
	 * @param filterType
	 * @param team
	 */
	public void setProjectFilterAndRefresh(ProjectFilterEnum filterType, Team team) {
		this.filterType = filterType;
		filterTeam = team;
		refreshProjects();
	}

	public void getMoreProjects() {
		if (isOwner) {
			view.setProjectSortVisible(true);
			view.showProjectFiltersUI();
			//this depends on the active filter
			switch (filterType) {
				case ALL:
					view.setAllProjectsFilterSelected();
					getMyProjects(ProjectListType.MY_PROJECTS, ProjectFilterEnum.ALL, currentProjectOffset);
					break;
				case MINE:
					view.setMyProjectsFilterSelected();
					getMyProjects(ProjectListType.MY_CREATED_PROJECTS, ProjectFilterEnum.MINE, currentProjectOffset);
					break;
				case MY_PARTICIPATED_PROJECTS:
					view.setSharedDirectlyWithMeFilterSelected();
					getMyProjects(ProjectListType.MY_PARTICIPATED_PROJECTS, ProjectFilterEnum.MY_PARTICIPATED_PROJECTS, currentProjectOffset);
					break;
				case MY_TEAM_PROJECTS:
					view.setTeamsFilterSelected();
					getMyProjects(ProjectListType.MY_TEAM_PROJECTS, ProjectFilterEnum.MY_TEAM_PROJECTS, currentProjectOffset);
					break;
				case FAVORITES:
					view.setFavoritesFilterSelected();
					view.setProjectSortVisible(false);
					getFavorites();
					break;
				case TEAM:
					view.setTeamsFilterSelected();
					getTeamProjects(currentProjectOffset);
					break;
				default:
					break;
			}
		} else
			getUserProjects(currentProjectOffset);
	}
	
	@Override
	public void resort(SortOptionEnum sort) {
		currentProjectSort = sort;
		view.setSortText(sort.sortText);
		refreshProjects();
	}	
	
	@Override
	public void refreshTeams() {
		view.clearTeamNotificationCount();
		if (isOwner)
			refreshTeamInvites();
		getTeamBundles(currentUserId, isOwner);
	}
	
	@Override
	public void refreshTeamInvites() {
		CallbackP<List<OpenUserInvitationBundle>> openTeamInvitationsCallback = new CallbackP<List<OpenUserInvitationBundle>>() {
			@Override
			public void invoke(List<OpenUserInvitationBundle> invites) {
				updateTeamInvites(invites);
			}
		};
		openInvitesWidget.configure(new Callback() {
			@Override
			public void invoke() {
				//refresh the teams after joining one
				refreshTeams();
			}
		}, openTeamInvitationsCallback);
	}
	
	public void getTeamBundles(String userId, final boolean includeRequestCount) {
		myTeamsWidget.clear();
		myTeamsWidget.showLoading();
		synapseClient.getTeamsForUser(userId, includeRequestCount, new AsyncCallback<List<TeamRequestBundle>>() {
			@Override
			public void onSuccess(List<TeamRequestBundle> teamsRequestBundles) {
				myTeamsWidget.clear();
				if (teamsRequestBundles != null && teamsRequestBundles.size() > 0) {
					int totalRequestCount = 0;
					view.addMyTeamProjectsFilter();
					for (TeamRequestBundle teamAndRequest: teamsRequestBundles) {
						// requests will always be 0 or greater
						Long requestCount = teamAndRequest.getRequestCount();
						Team team = teamAndRequest.getTeam();
						myTeamsWidget.addTeam(team, requestCount);
						view.addTeamsFilterTeam(team);
						totalRequestCount += requestCount;
					}
					view.setTeamsFilterVisible(true);
					if (includeRequestCount) {
						addMembershipRequests(totalRequestCount);
					}
				} else {
					myTeamsWidget.showEmpty();
					view.setTeamsFilterVisible(false);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				myTeamsWidget.clear();
				view.setTeamsFilterVisible(false);
				view.setTeamsError(caught.getMessage());
			}
		});
	}
	
	
	public void getMoreChallenges() {
		view.showChallengesLoading(true);
		challengeClient.getChallenges(currentUserId, CHALLENGE_PAGE_SIZE, currentChallengeOffset, new AsyncCallback<ChallengePagedResults>() {
			@Override
			public void onSuccess(ChallengePagedResults challengeResults) {
				addChallengeResults(challengeResults.getResults());
				challengePageAdded(challengeResults.getTotalNumberOfResults());
			}
	            @Override
			public void onFailure(Throwable caught) {
				view.showChallengesLoading(false);
				view.setChallengesError("Could not load challenges:" + caught.getMessage());
			}
		});
	}
	
	public void getMyProjects(ProjectListType projectListType, final ProjectFilterEnum filter, int offset) {
		view.showProjectsLoading(true);
		synapseClient.getMyProjects(projectListType, PROJECT_PAGE_SIZE, offset, currentProjectSort.sortBy, currentProjectSort.sortDir, new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				if (filterType == filter) {
					addProjectResults(projectHeaders.getResults(), projectHeaders.getLastModifiedBy());
					projectPageAdded(projectHeaders.getTotalNumberOfResults());
				}
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
		synapseClient.getProjectsForTeam(filterTeam.getId(), PROJECT_PAGE_SIZE, offset, currentProjectSort.sortBy, currentProjectSort.sortDir,  new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				if (filterType == ProjectFilterEnum.TEAM) {
					addProjectResults(projectHeaders.getResults(), projectHeaders.getLastModifiedBy());
					projectPageAdded(projectHeaders.getTotalNumberOfResults());
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load team projects:" + caught.getMessage());
			}
		});
	}

	public void getUserProjects(int offset) {
		view.showProjectsLoading(true);
		synapseClient.getUserProjects(currentUserId, PROJECT_PAGE_SIZE, offset, currentProjectSort.sortBy, currentProjectSort.sortDir, new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				addProjectResults(projectHeaders.getResults(), projectHeaders.getLastModifiedBy());
				projectPageAdded(projectHeaders.getTotalNumberOfResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load user projects:" + caught.getMessage());
			}
		});
	}
	
	public void addProjectResults(List<ProjectHeader> projectHeaders, List<UserProfile> lastModifiedByList) {
		view.showProjectsLoading(false);
		view.clearProjects();
		for (int i = 0; i < projectHeaders.size(); i++) {
			ProjectBadge badge = ginInjector.getProjectBadgeWidget();
			badge.configure(projectHeaders.get(i), lastModifiedByList == null ? null :lastModifiedByList.get(i));
			Widget widget = badge.asWidget();
			view.addProjectWidget(widget);
		}
		if (projectHeaders.isEmpty())
			view.setEmptyProjectUIVisible(true);
	}
	
	public void addChallengeResults(List<ChallengeBundle> challenges) {
		view.showChallengesLoading(false);
		view.clearChallenges();
		for (ChallengeBundle challenge : challenges) {
			ChallengeBadge badge = ginInjector.getChallengeBadgeWidget();
			badge.configure(challenge);
			Widget widget = badge.asWidget();
			view.addChallengeWidget(widget);
		}
	}
	
	public void projectPageAdded(int totalNumberOfResults) {
		currentProjectOffset += PROJECT_PAGE_SIZE;
		view.setIsMoreProjectsVisible(currentProjectOffset < totalNumberOfResults);
	}
	
	public void challengePageAdded(Long totalNumberOfResults) {
		currentChallengeOffset += CHALLENGE_PAGE_SIZE;
		view.setIsMoreChallengesVisible(currentChallengeOffset < totalNumberOfResults);
	}

	
	public void getFavorites() {
		view.showProjectsLoading(true);
		EntityBrowserUtils.loadFavorites(synapseClient, adapterFactory, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				if (filterType == ProjectFilterEnum.FAVORITES) {
					//convert to Project Headers
					if (result.size() == 0) {
						view.showProjectsLoading(false);
						view.setFavoritesHelpPanelVisible(true);
					} else {
						List<ProjectHeader> headers = new ArrayList<ProjectHeader>(result.size());
						List<String> lastModifiedBy = new ArrayList<String>(result.size());
						for (EntityHeader header : result) {
							lastModifiedBy.add(header.getId());
							ProjectHeader projectHeader = new ProjectHeader();
							projectHeader.setId(header.getId());
							projectHeader.setName(header.getName());
							headers.add(projectHeader);
						}
						addProjectResults(headers, null);
						view.setIsMoreProjectsVisible(false);	
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showProjectsLoading(false);
				view.setProjectsError("Could not load user favorites:" + caught.getMessage());
			}
		});
	}
	
	
	@Override
	public void createProject(final String name) {
		projectSynAlert.clear();
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
					projectSynAlert.handleException(caught);
				}
			}
		});
	}
	

	@Override
	public void createTeam(final String teamName) {
		teamSynAlert.clear();
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
					teamSynAlert.handleException(caught);
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
	
	private void profileUpdated() {
		view.showInfo("Success", "Your profile has been updated.");
		editMyProfile();
		view.refreshHeader();
	}
	
	private void loggedInCheck() {
		if (!authenticationController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}
	
	private void showView(Profile place) {
		view.clear();
		profileSynAlert.clear();
		projectSynAlert.clear();
		teamSynAlert.clear();
		String token = place.toToken();
		if (authenticationController.isLoggedIn() && token.startsWith("message/")) {
			//show alert message and go to view current profile
			String message = token.substring("message/".length());
			message = URL.decodeQueryString(message);
			view.showInfo("", message);
			token = "v";
		}
		if (token.equals("v") || token.startsWith("v/")) {
			Place gotoPlace = null;
			if (authenticationController.isLoggedIn()) {
				//replace url with current user id
				token = authenticationController.getCurrentUserPrincipalId() + token.substring(1);
				gotoPlace = new Profile(token);
			} else {
				//does not make sense, go home
				gotoPlace = new Home(ClientProperties.DEFAULT_PLACE_TOKEN);
			}
			globalApplicationState.getPlaceChanger().goTo(gotoPlace);
			return;
		}
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
	public void updateTeamInvites(List<OpenUserInvitationBundle> invites) {
		if (invites != null && invites.size() != inviteCount) {
			inviteCount = invites.size();
		}
		if (openRequestCount + inviteCount > 0)
			view.setTeamNotificationCount(Integer.toString(openRequestCount + inviteCount));
	}

	@Override
	public void addMembershipRequests(int count) {
		if (count != openRequestCount) 
			openRequestCount = count;		
		if (openRequestCount + inviteCount > 0)
			view.setTeamNotificationCount(Integer.toString(openRequestCount + inviteCount));
	}
	
	/**
	 * Exposed for test purposes only
	 */
	public int getOpenRequestCount() {
		return openRequestCount;
	}
	
	public int getInviteCount() {
		return inviteCount;
	}
	
	public void setOpenRequestCount(int openRequestCount) {
		this.openRequestCount = openRequestCount;
	}
	
	public void setInviteCount(int inviteCount) {
		this.inviteCount = inviteCount;
	}
	
	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public int getCurrentOffset() {
		return currentProjectOffset;
	}

	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public void setCurrentOffset(int currentOffset) {
		this.currentProjectOffset = currentOffset;
	}
	
	/**
	 * Exposed for unit testing purposes only
	 * @return
	 */
	public int getCurrentChallengeOffset() {
		return currentChallengeOffset;
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
		// if we are editing, then pop up a confirm
		if (globalApplicationState.isEditing()) {
			Callback yesCallback = new Callback() {
				@Override
				public void invoke() {
					refreshData(tab);
					view.setTabSelected(tab);
				}
			};
			view.showConfirmDialog("",
					DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE,
					yesCallback);
		} else {
			refreshData(tab);
			view.setTabSelected(tab);
		}
	}
	
	private void refreshData(ProfileArea tab) {
		switch (tab) {
			case PROJECTS:
				setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
				break;
			case TEAMS:
				refreshTeams();
				break;
			case CHALLENGES:
			case SETTINGS:
			default:
				break;
		}
		//always refreshes challenges to determine if tab should be shown
		refreshChallenges();
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
	
	public void initUserFavorites(final Callback callback) {
		synapseClient.getFavorites(new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> favorites) {
				globalApplicationState.setFavorites(favorites);
				callback.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.invoke();
			}
		});
	}
	
	@Override
	public void applyFilterClicked(ProjectFilterEnum filterType, Team team) {
		setProjectFilterAndRefresh(filterType, team);
	}
	
	@Override
	public void setGetCertifiedDismissed() {
		//set certification message visible=false for a year
		Date yearFromNow = new Date();
		CalendarUtil.addMonthsToDate(yearFromNow, 12);
		cookies.setCookie(USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + currentUserId, Boolean.toString(false), yearFromNow);
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

	@Override
	public void onEditProfile() {
		this.userProfileModalWidget.showEditProfile(this.currentUserId, new Callback() {
			@Override
			public void invoke() {
				profileUpdated();
			}
		});
		
	}

	@Override
	public void onImportLinkedIn() {
		redirectToLinkedIn();
	}
	
	public void redirectToLinkedIn() {
		profileSynAlert.clear();
		linkedInService.returnAuthUrl(gwt.getHostPageBaseURL(), new AsyncCallback<LinkedInInfo>() {
			@Override
			public void onSuccess(LinkedInInfo result) {
				// Store the requestToken secret in a cookie, set to expire in five minutes
				Date date = new Date(System.currentTimeMillis() + 300000);
				cookies.setCookie(CookieKeys.LINKEDIN, result.getRequestSecret(), date);
				// Open the LinkedIn authentication window in the same tab
				Window.open(result.getAuthUrl(), "_self", "");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				profileSynAlert.handleException(caught);
			}
		});
	}
	
	/**
	 * This method will update the current user's profile using LinkedIn
	 */
	public void updateProfileWithLinkedIn(String requestToken, String verifier) {
		// Grab the requestToken secret from the cookie. If it's expired, show an error message.
		// If not, grab the user's info for an update.
		profileSynAlert.clear();
		String secret = cookies.getCookie(CookieKeys.LINKEDIN);
		if(secret == null || secret.equals("")) {
			view.showErrorMessage("Your request has timed out. Please reload the page and try again.");
		} else {
			linkedInService.getCurrentUserInfo(requestToken, secret, verifier, gwt.getHostPageBaseURL(), new AsyncCallback<UserProfile>() {
				@Override
				public void onSuccess(UserProfile linkedInProfile) {
					// Give the user a chance to edit the profile.
					userProfileModalWidget.showEditProfile(linkedInProfile.getOwnerId(), linkedInProfile, new Callback(){

						@Override
						public void invoke() {
							profileUpdated();
						}});
				}
				
				@Override
				public void onFailure(Throwable caught) {
					profileSynAlert.handleException(caught);								
				}
			});
		}
	}
}

