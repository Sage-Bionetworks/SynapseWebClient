package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserProfileClientAsync;
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
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.LinkedInInfo;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class ProfilePresenter extends AbstractActivity implements ProfileView.Presenter, Presenter<Profile> {
		
	public static final int DELAY_GET_MY_TEAMS = 300;
	public static final String USER_PROFILE_VISIBLE_STATE_KEY = "org.sagebionetworks.synapse.user.profile.visible.state";
	public static final String USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY = "org.sagebionetworks.synapse.user.profile.certification.message.visible.state";
	public static final String USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY = "org.sagebionetworks.synapse.user.profile.validation.message.visible.state";
	
	public static int PROFILE = 0x1;
	public static int ORC_ID = 0x2;
	public static int VERIFICATION_SUBMISSION = 0x4;
	public static int IS_CERTIFIED = 0x8;
	public static int IS_VERIFIED = 0x10;
	public static int IS_ACT_MEMBER = 0x20;
	
	private Profile place;
	private ProfileView view;
	private SynapseClientAsync synapseClient;
	private UserProfileClientAsync userProfileClient;
	private ChallengeClientAsync challengeClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private CookieProvider cookies;
	private UserProfileModalWidget userProfileModalWidget;
	private LinkedInServiceAsync linkedInService;
	private GWTWrapper gwt;
	private OpenTeamInvitationsWidget openInvitesWidget;
	
	private SettingsPresenter settingsPresenter;
	private PortalGinInjector ginInjector;
	private AdapterFactory adapterFactory;
	private int inviteCount;
	private int openRequestCount;
	private String currentUserId;
	private boolean isOwner;
	private int currentProjectOffset, currentChallengeOffset;
	public final static int PROJECT_PAGE_SIZE=20;
	public final static int CHALLENGE_PAGE_SIZE=20;
	public ProfileArea currentArea;
	public ProjectFilterEnum filterType;
	public String filterTeamId;
	public SortOptionEnum currentProjectSort;
	public TeamListWidget myTeamsWidget;
	public SynapseAlert profileSynAlert;
	public SynapseAlert projectSynAlert;
	public SynapseAlert teamSynAlert;
	public SynapseAlert challengeSynAlert;
	public VerificationSubmissionWidget verificationModal;
	public UserBundle currentUserBundle;
	public Map<String, Boolean> isACTMemberMap;
	public Callback resubmitVerificationCallback;
	public LoadMoreWidgetContainer loadMoreProjectsWidgetContainer;
	public Callback getMoreProjectsCallback;
	public Callback refreshTeamsCallback;
	
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
			PortalGinInjector ginInjector,
			UserProfileClientAsync userProfileClient,
			VerificationSubmissionWidget verificationModal,
			SettingsPresenter settingsPresenter) {
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
		this.userProfileClient = userProfileClient;
		this.verificationModal = verificationModal;
		this.settingsPresenter = settingsPresenter;
		isACTMemberMap = new HashMap<String, Boolean>();
		view.clearSortOptions();
		for (SortOptionEnum sort: SortOptionEnum.values()) {
			view.addSortOption(sort);
		}
		profileSynAlert = ginInjector.getSynapseAlertWidget();
		projectSynAlert = ginInjector.getSynapseAlertWidget();
		teamSynAlert = ginInjector.getSynapseAlertWidget();
		challengeSynAlert = ginInjector.getSynapseAlertWidget();
		view.setPresenter(this);
		view.addUserProfileModalWidget(userProfileModalWidget);
		view.addMyTeamsWidget(myTeamsWidget);
		view.addOpenInvitesWidget(openInvitesWidget);
		view.setProfileSynAlertWidget(profileSynAlert.asWidget());
		view.setProjectSynAlertWidget(projectSynAlert.asWidget());
		view.setTeamSynAlertWidget(teamSynAlert.asWidget());
		view.setChallengeSynAlertWidget(challengeSynAlert.asWidget());
		resubmitVerificationCallback = new Callback() {
			@Override
			public void invoke() {
				newVerificationSubmissionClicked();
			}
		};
		getMoreProjectsCallback = new Callback() {
			@Override
			public void invoke() {
				getMoreProjects();
			}
		};
		refreshTeamsCallback = new Callback() {
			@Override
			public void invoke() {
				refreshTeams();
			}
		};
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
	
	public void updateArea(ProfileArea area, boolean pushState) {
		if (area != null && place != null && !area.equals(place.getArea())) {
			place.setArea(area, filterType, filterTeamId);
			if (pushState) {
				globalApplicationState.pushCurrentPlace(place);	
			} else {
				globalApplicationState.replaceCurrentPlace(place);
			}
		}
	}
	
	// Configuration
	public void updateProfileView(String userId) {
		inviteCount = 0;
		openRequestCount = 0;
		isOwner = authenticationController.isLoggedIn()
				&& authenticationController.getCurrentUserPrincipalId().equals(
						userId);
		if (currentArea == null || (ProfileArea.SETTINGS.equals(currentArea) && !isOwner)) {
			currentArea = ProfileArea.PROJECTS;
		}
		this.currentProjectSort = SortOptionEnum.LATEST_ACTIVITY;
		view.clear();
		view.showLoading();
		view.setSortText(currentProjectSort.sortText);
		view.setProfileEditButtonVisible(isOwner);
		view.setOrcIDLinkButtonVisible(isOwner);
		view.showTabs(isOwner);
		settingsPresenter.clear();
		myTeamsWidget.clear();
		view.clearTeamNotificationCount();
		myTeamsWidget.configure(false);
		currentUserId = userId == null ? authenticationController.getCurrentUserPrincipalId() : userId;
		if (isOwner) {
			// make sure we have the user favorites before continuing
			initUserFavorites(new Callback() {
				@Override
				public void invoke() {
					getUserProfile();
					boolean pushState = false;
					showTab(currentArea, pushState);
				}
			});
		} else {
			getUserProfile();
			boolean pushState = false;
			showTab(currentArea, pushState);
		}
	}
	
	private void getUserProfile() {
		//ask for everything in the user bundle
		currentUserBundle = null;
		int mask = PROFILE | ORC_ID | VERIFICATION_SUBMISSION | IS_CERTIFIED | IS_VERIFIED;
		Long currentUserIdLong = currentUserId != null ?  Long.parseLong(currentUserId)  : null;
		view.setSynapseEmailVisible(authenticationController.isLoggedIn());
		view.setOrcIdVisible(false);
		view.setUnbindOrcIdVisible(false);
		userProfileClient.getUserBundle(currentUserIdLong, mask, new AsyncCallback<UserBundle>() {
			@Override
			public void onSuccess(UserBundle bundle) {
				view.hideLoading();
				currentUserBundle = bundle;
				initializeShowHideProfile(isOwner);
				boolean isCertified = bundle.getIsCertified();
				if (isCertified) {
					view.addCertifiedBadge();
				} else {
					initializeShowHideCertification(isOwner);
				}
				initializeVerificationUI();
				view.setProfile(bundle.getUserProfile(), isOwner);
				String orcId = bundle.getORCID();
				if (orcId != null && orcId.length() > 0) {
					view.setOrcId(orcId);
					view.setOrcIdVisible(true);
					view.setUnbindOrcIdVisible(isOwner);
					view.setOrcIDLinkButtonVisible(false);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				profileSynAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void unbindOrcId() {
		view.showConfirmDialog("Unlink","Are you sure you want to unlink this ORC ID from your Synapse user profile?", new Callback() {
			@Override
			public void invoke() {
				unbindOrcIdAfterConfirmation();
			}
		});
	}
	
	public void unbindOrcIdAfterConfirmation() {
		userProfileClient.unbindOAuthProvidersUserId(OAuthProvider.ORCID, currentUserBundle.getORCID(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				//ORC id successfully removed.  refresh so that the user bundle and UI are up to date
				view.showInfo("Success", "ORC ID has been unbound.");
				globalApplicationState.refreshPage();
			}
			@Override
			public void onFailure(Throwable caught) {
				profileSynAlert.handleException(caught);
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
	
	public void initializeVerificationUI() {
		//verification UI is hidden by default (in view.clear())
		boolean isVerified = currentUserBundle.getIsVerified();
		//The UI is depends on the current state
		VerificationSubmission submission = currentUserBundle.getVerificationSubmission();
		if (isVerified) {
			List<VerificationState> stateHistory = submission.getStateHistory();
			VerificationState latestState = stateHistory.get(stateHistory.size()-1);
			String dateVerified = gwt.getFormattedDateString(latestState.getCreatedOn());
			view.showVerifiedBadge(submission.getFirstName(), submission.getLastName(), submission.getLocation(),submission.getCompany(), submission.getOrcid(), dateVerified);
		}
		
		if (submission == null) {
			//no submission.  if the owner, provide way to submit
			initializeShowHideVerification(isOwner);
		} else {
			//there's a submission in a state other than approved.  Show UI if owner or act member
			getIsACTMemberAndShowVerificationUI(submission);
		}
	}
	
	public void initializeShowHideVerification(boolean isOwner){
		if (isOwner) {
			boolean isVerificationAlertVisible = false;
			try {
				String cookieValue = cookies.getCookie(USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + currentUserId);
				if (cookieValue == null || !cookieValue.equalsIgnoreCase("false")) {
					isVerificationAlertVisible = true;	
				}
			} catch (Exception e) {
				//if there are any problems getting the certification message visibility state, ignore and use default (hide)
			}
			view.setVerificationAlertVisible(isVerificationAlertVisible);
			//show the submit verification button if the full alert isn't visible
			view.setVerificationButtonVisible(!isVerificationAlertVisible);
		}
	}
	
	public void getIsACTMemberAndShowVerificationUI(final VerificationSubmission submission) {
		if (authenticationController.isLoggedIn()) {
			//do we know if the current user is an act member?
			Boolean isActMember = isACTMemberMap.get(authenticationController.getCurrentUserPrincipalId());
			if (isActMember == null) {
				//we don't know, find out.
				int mask = IS_ACT_MEMBER;
				userProfileClient.getMyOwnUserBundle(mask, new AsyncCallback<UserBundle>() {
					@Override
					public void onSuccess(UserBundle userBundle) {
						isACTMemberMap.put(authenticationController.getCurrentUserPrincipalId(), userBundle.getIsACTMember());
						showVerificationUI(submission, userBundle.getIsACTMember());
					}
					
					@Override
					public void onFailure(Throwable caught) {
						view.hideLoading();
						profileSynAlert.handleException(caught);
					}
				});	
			} else {
				showVerificationUI(submission, isActMember);
			}
		}
	}
	
	public void showVerificationUI(VerificationSubmission submission, Boolean isACTMember) {
		if (isOwner || isACTMember) {
			VerificationState currentState = submission.getStateHistory().get(submission.getStateHistory().size()-1);
			if (currentState.getState() == VerificationStateEnum.SUSPENDED) {
				view.setVerificationSuspendedButtonVisible(true);
				view.setResubmitVerificationButtonVisible(isOwner);
			} else if (currentState.getState() == VerificationStateEnum.REJECTED) {
				view.setVerificationRejectedButtonVisible(true);
				view.setResubmitVerificationButtonVisible(isOwner);
			} else if (currentState.getState() == VerificationStateEnum.SUBMITTED) {
				view.setVerificationSubmittedButtonVisible(true);
			} else if (currentState.getState() == VerificationStateEnum.APPROVED) {
				view.setVerificationDetailsButtonVisible(true);
			}
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
		loadMoreProjectsWidgetContainer = ginInjector.getLoadMoreProjectsWidgetContainer();
		view.setProjectContainer(loadMoreProjectsWidgetContainer.asWidget());
		loadMoreProjectsWidgetContainer.setIsMore(false);
		loadMoreProjectsWidgetContainer.configure(getMoreProjectsCallback);
		getMoreProjects();
		if (isOwner) {
			// refresh owner teams to update the team notification count, and team filter
			gwt.scheduleExecution(refreshTeamsCallback, DELAY_GET_MY_TEAMS);
		}
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
	public void setProjectFilterAndRefresh(ProjectFilterEnum filterType, String filterTeamId) {
		if (filterType == null) {
			filterType = ProjectFilterEnum.ALL;
		}
		this.filterType = filterType;
		this.filterTeamId = filterTeamId;
		if (place != null)
			place.setArea(ProfileArea.PROJECTS, filterType, filterTeamId);
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
				case CREATED_BY_ME:
					view.setMyProjectsFilterSelected();
					getMyProjects(ProjectListType.MY_CREATED_PROJECTS, ProjectFilterEnum.CREATED_BY_ME, currentProjectOffset);
					break;
				case SHARED_DIRECTLY_WITH_ME:
					view.setSharedDirectlyWithMeFilterSelected();
					getMyProjects(ProjectListType.MY_PARTICIPATED_PROJECTS, ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, currentProjectOffset);
					break;
				case ALL_MY_TEAM_PROJECTS:
					view.setTeamsFilterSelected();
					getMyProjects(ProjectListType.MY_TEAM_PROJECTS, ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, currentProjectOffset);
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
		teamSynAlert.clear();
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
				teamSynAlert.handleException(caught);
			}
		});
	}
	
	
	public void getMoreChallenges() {
		challengeSynAlert.clear();
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
				challengeSynAlert.handleException(caught);
			}
		});
	}
	
	public void getMyProjects(ProjectListType projectListType, final ProjectFilterEnum filter, int offset) {
		projectSynAlert.clear();
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
				projectSynAlert.handleException(caught);
			}
		});
	}
	
	public void getTeamProjects(int offset) {
		projectSynAlert.clear();
		synapseClient.getProjectsForTeam(filterTeamId, PROJECT_PAGE_SIZE, offset, currentProjectSort.sortBy, currentProjectSort.sortDir,  new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				if (filterType == ProjectFilterEnum.TEAM) {
					addProjectResults(projectHeaders.getResults(), projectHeaders.getLastModifiedBy());
					projectPageAdded(projectHeaders.getTotalNumberOfResults());
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				projectSynAlert.handleException(caught);
			}
		});
	}

	public void getUserProjects(int offset) {
		projectSynAlert.clear();
		synapseClient.getUserProjects(currentUserId, PROJECT_PAGE_SIZE, offset, currentProjectSort.sortBy, currentProjectSort.sortDir, new AsyncCallback<ProjectPagedResults>() {
			@Override
			public void onSuccess(ProjectPagedResults projectHeaders) {
				addProjectResults(projectHeaders.getResults(), projectHeaders.getLastModifiedBy());
				projectPageAdded(projectHeaders.getTotalNumberOfResults());
			}
			@Override
			public void onFailure(Throwable caught) {
				projectSynAlert.handleException(caught);
			}
		});
	}
	
	public void addProjectResults(List<ProjectHeader> projectHeaders, List<UserProfile> lastModifiedByList) {
		for (int i = 0; i < projectHeaders.size(); i++) {
			ProjectBadge badge = ginInjector.getProjectBadgeWidget();
			badge.addStyleName("margin-bottom-10 col-xs-12");
			badge.configure(projectHeaders.get(i), lastModifiedByList == null ? null :lastModifiedByList.get(i));
			Widget widget = badge.asWidget();
			loadMoreProjectsWidgetContainer.add(widget);
		}
	}
	
	public void addChallengeResults(List<ChallengeBundle> challenges) {
		view.showChallengesLoading(false);
		view.clearChallenges();
		for (ChallengeBundle challenge : challenges) {
			ChallengeBadge badge = ginInjector.getChallengeBadgeWidget();
			badge.configure(challenge);
			Widget widget = badge.asWidget();
			view.addChallengeWidget(widget);
			if (currentArea != null && currentArea.equals(ProfileArea.CHALLENGES)) {
				// SWC-3213: extra call to make sure challenge tab is currently shown
				view.setTabSelected(ProfileArea.CHALLENGES);
			}
		}
	}
	
	public void projectPageAdded(int totalNumberOfResults) {
		currentProjectOffset += PROJECT_PAGE_SIZE;
		loadMoreProjectsWidgetContainer.setIsMore(currentProjectOffset < totalNumberOfResults);
	}
	
	public void challengePageAdded(Long totalNumberOfResults) {
		currentChallengeOffset += CHALLENGE_PAGE_SIZE;
		view.setIsMoreChallengesVisible(currentChallengeOffset < totalNumberOfResults);
	}
	
	public void getFavorites() {
		projectSynAlert.clear();
		EntityBrowserUtils.loadFavorites(synapseClient, adapterFactory, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				if (filterType == ProjectFilterEnum.FAVORITES) {
					//convert to Project Headers
					if (result.size() == 0) {
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
						loadMoreProjectsWidgetContainer.setIsMore(false);
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				projectSynAlert.handleException(caught);
			}
		});
	}
	
	
	@Override
	public void createProject(final String name) {
		projectSynAlert.clear();
		//validate project name
		if (!DisplayUtils.isDefined(name)) {
			projectSynAlert.showError(DisplayConstants.PLEASE_ENTER_PROJECT_NAME);
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
					projectSynAlert.showError(DisplayConstants.WARNING_PROJECT_NAME_EXISTS);
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
			teamSynAlert.showError(DisplayConstants.PLEASE_ENTER_TEAM_NAME);
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
					teamSynAlert.showError(DisplayConstants.WARNING_TEAM_NAME_EXISTS);
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
		currentArea = place.getArea();
		filterType = place.getProjectFilter();
		filterTeamId = place.getTeamId();
		view.setPresenter(this);
		if (token.equals("oauth_bound")) {
			view.showInfo("", DisplayConstants.SUCCESSFULLY_LINKED_OAUTH2_ACCOUNT);
			token = "v";
		}
		if (token.equals("v") || token.startsWith("v/") || token.isEmpty()) {
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
			updateProfileView(place.getUserId());
		}
		else {
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
				//if this is a number, then treat it as a a user id
				try{
					Long.parseLong(place.getUserId());
					updateProfileView(place.getUserId());
				} catch (NumberFormatException nfe) {
					getUserIdFromUsername(token);
				}
			}
		}
	}
	
	public void getUserIdFromUsername(String userName) {
		synapseClient.getUserIdFromUsername(userName, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String userId) {
				place.setUserId(userId);
				updateProfileView(userId);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				profileSynAlert.handleException(caught);
			}
		});
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
	
	public void showTab(ProfileArea tab, boolean pushState) {
		updateArea(tab, pushState);
		refreshData(tab);
		view.setTabSelected(tab);
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
					boolean pushState = true;
					showTab(tab, pushState);
				}
			};
			view.showConfirmDialog("",
					DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE,
					yesCallback);
		} else {
			boolean pushState = true;
			showTab(tab, pushState);
		}
	}
	
	private void refreshData(ProfileArea tab) {
		switch (tab) {
			case PROJECTS:
				setProjectFilterAndRefresh(filterType, filterTeamId);
				break;
			case TEAMS:
				refreshTeams();
				break;
			case SETTINGS:
				settingsPresenter.configure();
				view.setSettingsWidget(settingsPresenter.asWidget());
				break;
			case CHALLENGES:
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
		String filterTeamId = null;
		if (team != null) {
			filterTeamId = team.getId();
		}
		setProjectFilterAndRefresh(filterType, filterTeamId);
		globalApplicationState.pushCurrentPlace(place);
	}
	
	@Override
	public void setGetCertifiedDismissed() {
		//set certification message visible=false for a year
		Date yearFromNow = new Date();
		CalendarUtil.addMonthsToDate(yearFromNow, 12);
		cookies.setCookie(USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + currentUserId, Boolean.toString(false), yearFromNow);
	}
	@Override
	public void setVerifyDismissed() {
		//set verify message visible=false for a year
		Date yearFromNow = new Date();
		CalendarUtil.addMonthsToDate(yearFromNow, 12);
		cookies.setCookie(USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + currentUserId, Boolean.toString(false), yearFromNow);
		//and show button instead
		view.setVerificationButtonVisible(true);
	}
	
	@Override
	public void setVerifyUndismissed() {
		cookies.removeCookie(USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + currentUserId);
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
	
	@Override
	public void editVerificationSubmissionClicked() {
		//edit the existing submission
		verificationModal.configure(
				currentUserBundle.getVerificationSubmission(), 
				isACTMemberMap.get(authenticationController.getCurrentUserPrincipalId()), 
				true) //isModal
			.setResubmitCallback(resubmitVerificationCallback)
			.show();		
	}
	
	@Override
	public void newVerificationSubmissionClicked() {
		List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
		if (currentUserBundle.getVerificationSubmission() != null) {
			attachments = currentUserBundle.getVerificationSubmission().getAttachments();
		}
		
		//create a new submission
		verificationModal.configure(
				currentUserBundle.getUserProfile(), 
				currentUserBundle.getORCID(), 
				true, //isModal
				attachments) 
			.show();
	}
	
	@Override
	public void linkOrcIdClicked() {
		String orcId = currentUserBundle.getORCID();
		if (orcId != null && orcId.length() > 0) {
			//already set!
			view.showErrorMessage("An ORC ID has already been linked to your Synapse account.");
		} else {
			DisplayUtils.newWindow("/Portal/oauth2AliasCallback?oauth2provider=ORCID", "_self", "");
		}
	}
	
	// used for targeted unit test only
	public void setLoadMoreProjectsWidgetContainer(LoadMoreWidgetContainer loadMoreProjectsWidgetContainer) {
		this.loadMoreProjectsWidgetContainer = loadMoreProjectsWidgetContainer;
	}
}

