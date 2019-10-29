package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.PaginatedTeamIds;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.principal.PrincipalAliasRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidget;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitserver.ChallengeClientImplTest;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class ProfilePresenterTest {
	ProfilePresenter profilePresenter;
	@Mock
	ProfileView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	UserAccountServiceAsync mockUserService;
	@Mock
	LinkedInServiceAsync mockLinkedInServic;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	PortalGinInjector mockInjector;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	Profile place;
	@Mock
	UserProfileModalWidget mockUserProfileModalWidget;
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String password = "password";
	List<EntityHeader> myFavorites;
	List<ProjectHeader> myProjects;
	List<Challenge> testChallenges;
	@Mock
	ProjectBadge mockProjectBadge;
	@Mock
	ChallengeBadge mockChallengeBadge;
	@Mock
	TeamListWidget mockTeamListWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	OpenTeamInvitationsWidget mockTeamInviteWidget;
	@Captor
	ArgumentCaptor<PrincipalAliasRequest> principalAliasRequestCaptor;

	Long targetUserIdLong = 12345L;
	String targetUserId = targetUserIdLong.toString();
	String targetUsername = "jediknight";
	
	@Mock
	UserBundle mockUserBundle;
	@Mock
	UserBundle mockCurrentUserBundle;
	@Mock
	Team mockTeam;
	@Mock
	SettingsPresenter mockSettingsPresenter;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	PromptForValuesModalView mockPromptModalView;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PaginatedTeamIds mockPaginatedTeamIds;
	@Mock
	PaginatedTeamIds mockPaginatedTeamIdsPage2;
	@Mock
	Project mockProject;
	@Mock
	PrincipalAliasResponse mockPrincipalAliasResponse;
	@Mock
	DownloadListWidget mockDownloadListWidget;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	ProfileCertifiedValidatedWidget mockProfileCertifiedValidatedWidget;
	
	List<Team> myTeams;
	List<String> teamIds;
	public static final String NEXT_PAGE_TOKEN = "19282";
	@Before
	public void setup() throws JSONObjectAdapterException {
		when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		profilePresenter = new ProfilePresenter(mockView, 
				mockAuthenticationController, 
				mockGlobalApplicationState,
				mockGwt, 
				mockTeamListWidget, 
				mockTeamInviteWidget, 
				mockInjector,
				mockSynapseJavascriptClient,
				mockIsACTMemberAsyncHandler
				);
		verify(mockView).setPresenter(profilePresenter);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockInjector.getPromptForValuesModal()).thenReturn(mockPromptModalView);
		when(mockInjector.getUserProfileModalWidget()).thenReturn(mockUserProfileModalWidget);
		when(mockInjector.getProfileCertifiedValidatedWidget()).thenReturn(mockProfileCertifiedValidatedWidget);
		when(mockInjector.getProjectBadgeWidget()).thenReturn(mockProjectBadge);
		when(mockInjector.getChallengeBadgeWidget()).thenReturn(mockChallengeBadge);
		when(mockInjector.getSettingsPresenter()).thenReturn(mockSettingsPresenter);
		when(mockInjector.getDownloadListWidget()).thenReturn(mockDownloadListWidget);
		userProfile.setDisplayName("tester");
		userProfile.setOwnerId("1");
		userProfile.setEmail("original.email@sagebase.org");
		testUser.setProfile(userProfile);
		testUser.setSession(new Session());
		testUser.getSession().setSessionToken("token");
		testUser.setIsSSO(false);
		
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		when(mockPrincipalAliasResponse.getPrincipalId()).thenReturn(targetUserIdLong);
		AsyncMockStubber.callSuccessWith(mockPrincipalAliasResponse).when(mockSynapseJavascriptClient).getPrincipalAlias(any(PrincipalAliasRequest.class), any(AsyncCallback.class));
		when(mockUserBundle.getUserProfile()).thenReturn(userProfile);
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		//by default, we only have a single page of results
		when(mockPaginatedTeamIds.getNextPageToken()).thenReturn(null);
		when(mockSynapseJavascriptClient.getUserTeams(anyString(), anyBoolean(), anyString())).thenReturn(getDoneFuture(mockPaginatedTeamIds), getDoneFuture(mockPaginatedTeamIdsPage2));
		myTeams = getUserTeams();
		when(mockSynapseJavascriptClient.listTeams(anyList())).thenReturn(getDoneFuture(myTeams));
		teamIds = getTeamIds(myTeams);
		when(mockPaginatedTeamIds.getTeamIds()).thenReturn(teamIds);
		when(mockPaginatedTeamIdsPage2.getTeamIds()).thenReturn(teamIds);
		
		//set up get user projects test
		EntityHeader project1 = new EntityHeader();
		project1.setId("syn1");
		EntityHeader project2 = new EntityHeader();
		project2.setId("syn2");
		
		myFavorites = new ArrayList<EntityHeader>();
		myFavorites.add(project1);
		myFavorites.add(project2);
		
		ProjectHeader projectHeader1 = new ProjectHeader();
		projectHeader1.setId("syn1");
		ProjectHeader projectHeader2 = new ProjectHeader();
		projectHeader2.setId("syn2");
		
		myProjects = new ArrayList<ProjectHeader>();
		myProjects.add(projectHeader1);
		myProjects.add(projectHeader2);
		
		AsyncMockStubber.callSuccessWith(myProjects).when(mockSynapseJavascriptClient).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(myProjects).when(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(myProjects).when(mockSynapseJavascriptClient).getProjectsForTeam(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(myFavorites).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		
		//set up create project test
		when(mockProject.getId()).thenReturn("syn88888888");
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getDoneFuture(mockProject));
		
		//set up create team test
		when(mockTeam.getId()).thenReturn("new team id");
		AsyncMockStubber.callSuccessWith(mockTeam).when(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		
		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> testBatchResults = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		List<EntityHeader> testEvaluationResults = new ArrayList<EntityHeader>();
		EntityHeader testEvaluation = new EntityHeader();
		testEvaluation.setId("eval project id 1");
		testEvaluation.setName("My Test Evaluation Project");
		testEvaluationResults.add(testEvaluation);
		testBatchResults.setTotalNumberOfResults(1);
		testBatchResults.setResults(testEvaluationResults);
		when(mockGlobalApplicationState.isEditing()).thenReturn(false);
		setupTestChallengePagedResults();
		
		when(place.toToken()).thenReturn(targetUserId);
		when(place.getUserId()).thenReturn(targetUserId);
		
		when(mockInjector.getLoadMoreProjectsWidgetContainer()).thenReturn(mockLoadMoreContainer);
	}
	
	public static List<String> getTeamIds(List<Team> teams) {
		List<String> teamIds = new ArrayList<String>();
		for (Team team : teams) {
			teamIds.add(team.getId());
		}
		return teamIds;
	}
	
	public static List<Team> getUserTeams() {
		Team testTeam1 = new Team();
		testTeam1.setId("42");
		testTeam1.setName("My Test Team");
		Team testTeam2 = new Team();
		testTeam2.setId("24");
		testTeam2.setName("Team Test My");
		ArrayList<Team> teamList = new ArrayList<Team>();
		teamList.add(testTeam1);
		teamList.add(testTeam2);
		return teamList;
	}
	
	public void setupTestChallengePagedResults() {
		testChallenges = new ArrayList<Challenge>();
		testChallenges.add(ChallengeClientImplTest.getTestChallenge());
		AsyncMockStubber.callSuccessWith(testChallenges).when(mockSynapseJavascriptClient).getChallenges(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testUpdateProfileView() {
		boolean isOwner = true;
		String userId = userProfile.getOwnerId();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(isOwner);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		profilePresenter.updateProfileView(userId);
		
		verify(mockView).clear();
		verify(mockTeamListWidget, Mockito.atLeastOnce()).clear();
		verify(mockView).showLoading();
		verify(mockView).setProfileEditButtonVisible(isOwner);
		verify(mockView).showTabs(isOwner);
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		
		//SWC-4872: if not ACT, then profile certification/validation state not shown.
		// If ACT, configure and show that widget.
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());
		CallbackP<Boolean> isACTCallback = callbackPCaptor.getValue();
		isACTCallback.invoke(false);
		verify(mockView, never()).setCertifiedValidatedWidget(any(IsWidget.class));
		isACTCallback.invoke(true);
		verify(mockProfileCertifiedValidatedWidget).configure(Long.parseLong(userId));
		verify(mockView).setCertifiedValidatedWidget(mockProfileCertifiedValidatedWidget);
	}
	
	@Test
	public void testStart() {
		profilePresenter.setPlace(place);
		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		
		profilePresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);
	}
	
	@Test
	public void testPublicView() throws JSONObjectAdapterException{
		//view another user profile
		userProfile.setOwnerId(targetUserId);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		
		when(place.toToken()).thenReturn(targetUserId);
		when(place.getUserId()).thenReturn(targetUserId);
		profilePresenter.setPlace(place);
		verify(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		
		verify(mockSynapseJavascriptClient, never()).getUserTeams(anyString(), anyBoolean(), anyString());
		
		//not logged in, should not ask for this user favs
		verify(mockSynapseJavascriptClient, never()).getFavorites(any(AsyncCallback.class));
	}
	
	@Test
	public void testViewUsername() {
		//view another user profile based on username
		userProfile.setOwnerId(targetUserId);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(place.toToken()).thenReturn(targetUsername);
		when(place.getUserId()).thenReturn(null);
		profilePresenter.setPlace(place);
		
		verify(mockSynapseJavascriptClient).getPrincipalAlias(principalAliasRequestCaptor.capture(), any(AsyncCallback.class));
		assertEquals(targetUsername, principalAliasRequestCaptor.getValue().getAlias());
		verify(mockSynapseJavascriptClient).getUserBundle(eq(Long.parseLong(targetUserId)), anyInt(), any(AsyncCallback.class));
		verify(place).setUserId(targetUserId);
	}
	
	@Test
	public void testViewUsernameFailure() {
		//view another user profile based on username
		userProfile.setOwnerId(targetUserId);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(place.toToken()).thenReturn(targetUsername);
		when(place.getUserId()).thenReturn(null);
		Exception ex = new Exception("an error");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getPrincipalAlias(any(PrincipalAliasRequest.class), any(AsyncCallback.class));
		
		profilePresenter.setPlace(place);
		
		verify(mockSynapseJavascriptClient).getPrincipalAlias(any(PrincipalAliasRequest.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	private void invokeGetMyTeamsCallback() {
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockGwt).scheduleExecution(callbackCaptor.capture(), eq(ProfilePresenter.DELAY_GET_MY_TEAMS));
		callbackCaptor.getValue().invoke();
	}

	@Test
	public void testViewMyProfileNoRedirect() throws JSONObjectAdapterException{
		//view another user profile
		setPlaceMyProfile("456");
		verify(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		
		verify(mockSynapseJavascriptClient, never()).getUserTeams(anyString(), anyBoolean(), anyString());
		//should attempt to get my teams, but delayed.
		invokeGetMyTeamsCallback();
		//also verify that it is asking for the correct teams
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseJavascriptClient).getUserTeams(captor.capture(), anyBoolean(), anyString());
		verify(mockSynapseJavascriptClient).listTeams(anyList());
		assertEquals("456", captor.getValue());
	} 
	

	@Test
	public void testEditMyProfileNoRedirect() {
		//view another user profile
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("1");
		when(place.getUserId()).thenReturn("2");
		profilePresenter.setPlace(place);
		verify(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		verify(mockView, never()).showLoginAlert();
	}
	
	@Test
	public void testSettingsNotOwner() {
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("2");
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(place.getUserId()).thenReturn("4");
		when(place.getArea()).thenReturn(ProfileArea.SETTINGS);
		profilePresenter.setPlace(place);
		boolean isOwner = false;
		verify(mockView).showTabs(isOwner);
	}		
	
	@Test
	public void testGetProfileError() {
		//some other error occurred
		Exception ex = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		profilePresenter.updateProfileView("1");
		verify(mockView).hideLoading();
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testRefreshProjects() {
		profilePresenter.setCurrentOffset(22);
		//on refresh, current project offset should be reset to 0
		profilePresenter.refreshProjects();
		assertEquals(ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockInjector).getLoadMoreProjectsWidgetContainer();
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockLoadMoreContainer, times(2)).setIsMore(false);
		verify(mockLoadMoreContainer).configure(any(Callback.class));
	}
	
	@Test
	public void testGetAllMyProjects() {
		profilePresenter.setIsOwner(true);
		//when setting the filter to all, it should ask for all of my projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).setAllProjectsFilterSelected();
		verify(mockView).showProjectFiltersUI();
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.ALL), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
		verify(mockLoadMoreContainer, never()).clear();
		
		// if we set the place and get projects again, verify existing project load more container is cleared (deregisters callback)
		profilePresenter.setPlace(place);
		verify(mockLoadMoreContainer).clear();
	}
	
	@Test
	public void testGetAllTheirProjects() {
		profilePresenter.setIsOwner(false);
		//when setting the filter to all, it should ask for all of their projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
	}
	
	@Test
	public void testGetProjectsDefaultFilter() {
		profilePresenter.setIsOwner(false);
		//when setting the filter to all, it should ask for all of their projects
		profilePresenter.setProjectFilterAndRefresh(null, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
	}
	
	@Test
	public void testGetProjectsDefaultFilterFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),   any(AsyncCallback.class));
		profilePresenter.setIsOwner(false);
		//when setting the filter to all, it should ask for all of their projects
		profilePresenter.setProjectFilterAndRefresh(null, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testGetProjectsCreatedByMe() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		//when setting the filter to my projects, it should query for projects created by me
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setMyProjectsFilterSelected();
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.CREATED), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
	}

	@Test
	public void testGetFavorites() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.FAVORITES, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setFavoritesFilterSelected();
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setLastActivityOnColumnVisible(false);
	}
	
	@Test
	public void testGetSharedDirectlyWithMeProjects() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setSharedDirectlyWithMeFilterSelected();
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.PARTICIPATED), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
	}
	
	@Test
	public void testGetFavoritesEmpty() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		myFavorites.clear();
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.FAVORITES, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).setFavoritesFilterSelected();
		verify(mockView).setFavoritesHelpPanelVisible(true);
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		verify(mockLoadMoreContainer, never()).add(any(Widget.class));
		verify(mockView).setLastActivityOnColumnVisible(false);
	}
	
	@Test
	public void testGetProjectsByTeam() {
		profilePresenter.setIsOwner(true);
		String teamId = "39448";
		
		//when setting the filter to all, it should ask for all of my projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, teamId);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setTeamsFilterSelected();
		verify(mockSynapseJavascriptClient).getProjectsForTeam(eq(teamId), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
	}
	
	@Test
	public void testGetProjectsByTeamFailure() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		AsyncMockStubber.callFailureWith(new Exception("failed")).when(mockSynapseJavascriptClient).getProjectsForTeam(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, "123");
		verify(mockSynAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testGetProjectCreatedByMeFailure() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("111");
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.CREATED), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),   any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockSynAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testApplyFilterClickedAll() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.ALL), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
		verify(mockGlobalApplicationState).pushCurrentPlace(any(Place.class));
	}
	
	@Test
	public void testApplyFilterClickedMine() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.CREATED), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMyParticipatedProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.PARTICIPATED), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMyTeamProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.TEAM), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setLastActivityOnColumnVisible(true);
	}

	@Test
	public void testApplyFilterClickedFavorites() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.FAVORITES, null);
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		verify(mockView).setLastActivityOnColumnVisible(false);
	}
	
	@Test
	public void testGetMyProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.ALL), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		profilePresenter.getMyProjects(ProjectListType.ALL, ProjectFilterEnum.ALL, 0);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.ALL), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockSynAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testProjectPageAdded() {
		//add a page (where the full project list fits in a single page
		profilePresenter.setLoadMoreProjectsWidgetContainer(mockLoadMoreContainer);
		profilePresenter.setCurrentOffset(0);
		profilePresenter.projectPageAdded(ProfilePresenter.PROJECT_PAGE_SIZE - 10);
		assertEquals(ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockLoadMoreContainer).setIsMore(false);
	}
	
	@Test
	public void testProjectPageAddedZeroResults() {
		profilePresenter.setLoadMoreProjectsWidgetContainer(mockLoadMoreContainer);
		profilePresenter.setCurrentOffset(0);
		profilePresenter.projectPageAdded(0);
		verify(mockLoadMoreContainer).setIsMore(false);
	}
	
	@Test
	public void testProjectPageAddedWithMoreResults() {
		profilePresenter.setLoadMoreProjectsWidgetContainer(mockLoadMoreContainer);
		profilePresenter.setCurrentOffset(0);
		profilePresenter.projectPageAdded(ProfilePresenter.PROJECT_PAGE_SIZE + 10);
		assertEquals(ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockLoadMoreContainer).setIsMore(true);
	}
	
	@Test
	public void testProjectPageTwoAddedWithMoreResults() {
		profilePresenter.setLoadMoreProjectsWidgetContainer(mockLoadMoreContainer);
		profilePresenter.setCurrentOffset(ProfilePresenter.PROJECT_PAGE_SIZE);
		profilePresenter.projectPageAdded(2*ProfilePresenter.PROJECT_PAGE_SIZE + 10);
		assertEquals(2*ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockLoadMoreContainer).setIsMore(true);
	}

	
	@Test
	public void testCreateProject() {
		profilePresenter.createProjectAfterPrompt("valid name");
		verify(mockSynapseJavascriptClient).createEntity(any(Entity.class));
		//inform user of success, and go to new project page
		verify(mockView).showInfo(anyString());
		verify(mockPlaceChanger).goTo(isA(Synapse.class));
	}

	@Test
	public void testCreateProjectEmptyName() {
		profilePresenter.createProjectAfterPrompt("");
		verify(mockSynapseJavascriptClient, never()).createEntity(any(Entity.class));
		verify(mockPromptModalView).showError(anyString());
		reset(mockPromptModalView);
		
		profilePresenter.createProjectAfterPrompt(null);

		verify(mockSynapseJavascriptClient, never()).createEntity(any(Entity.class));
		verify(mockPromptModalView).showError(anyString());
	}

	@Test
	public void testCreateProjectError() {
		String errorMessage = "unhandled";
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new Exception(errorMessage)));
		
		profilePresenter.createProjectAfterPrompt("valid name");
		verify(mockPromptModalView).showError(errorMessage);
	}
	
	@Test
	public void testCreateProjectNameConflictError() {
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new ConflictException("special handled exception type")));
		profilePresenter.createProjectAfterPrompt("valid name");
		verify(mockPromptModalView).showError(eq(DisplayConstants.WARNING_PROJECT_NAME_EXISTS));
	}
	
	@Test
	public void testSortedByLatestActivity() {
		profilePresenter.sort(ProjectListSortColumn.LAST_ACTIVITY);
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(ProjectListSortColumn.LAST_ACTIVITY), eq(SortDirection.ASC), any(AsyncCallback.class));
		
		profilePresenter.sort(ProjectListSortColumn.LAST_ACTIVITY);
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(ProjectListSortColumn.LAST_ACTIVITY), eq(SortDirection.DESC), any(AsyncCallback.class));

		profilePresenter.sort(ProjectListSortColumn.PROJECT_NAME);
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(ProjectListSortColumn.PROJECT_NAME), eq(SortDirection.ASC), any(AsyncCallback.class));
		
		profilePresenter.sort(ProjectListSortColumn.PROJECT_NAME);
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(ProjectListSortColumn.PROJECT_NAME), eq(SortDirection.DESC), any(AsyncCallback.class));
	}

	@Test
	public void testCreateTeam() {
		profilePresenter.createTeamAfterPrompt("valid name");
		verify(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		//inform user of success, and go to new team page
		verify(mockView).showInfo(anyString());
		verify(mockPlaceChanger).goTo(isA(org.sagebionetworks.web.client.place.Team.class));
	}

	@Test
	public void testCreateTeamEmptyName() {
		profilePresenter.createTeamAfterPrompt("");
		verify(mockSynapseJavascriptClient, never()).createTeam(any(Team.class), any(AsyncCallback.class));
		verify(mockPromptModalView).showError(anyString());
		reset(mockPromptModalView);
		profilePresenter.createTeamAfterPrompt(null);
		verify(mockSynapseJavascriptClient, never()).createTeam(any(Team.class), any(AsyncCallback.class));
		verify(mockPromptModalView).showError(anyString());
	}

	@Test
	public void testCreateTeamError() {
		String errorMessage = "unhandled";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		profilePresenter.createTeamAfterPrompt("valid name");
		verify(mockPromptModalView).showError(errorMessage);
	}
	
	@Test
	public void testCreateTeamNameConflictError() {
		AsyncMockStubber.callFailureWith(new ConflictException("special handled exception type")).when(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		profilePresenter.createTeamAfterPrompt("valid name");
		verify(mockPromptModalView).showError(DisplayConstants.WARNING_TEAM_NAME_EXISTS);
	}
	
	//Challenge tests
	
	@Test
	public void testChallengePlace() {
		when(place.getArea()).thenReturn(ProfileArea.CHALLENGES);
		profilePresenter.setPlace(place);
		profilePresenter.getMoreChallenges();
		verify(mockView, atLeastOnce()).setTabSelected(ProfileArea.CHALLENGES);
	}

	@Test
	public void testRefreshChallenges() {
		profilePresenter.tabClicked(ProfileArea.CHALLENGES);
		verify(mockView).clearChallenges();
		assertEquals(ProfilePresenter.CHALLENGE_PAGE_SIZE, profilePresenter.getCurrentChallengeOffset());
		verify(mockView, times(2)).showChallengesLoading(anyBoolean());
		verify(mockView).addChallengeWidget(any(Widget.class));
	}
	
	public ArrayList<Team> setupUserTeams(long openRequestNumberPerTeam, int numTeams) {
		ArrayList<Team> teams = new ArrayList<Team>();
		for (int i = 0; i < numTeams; i++) {
			Team testTeam = new Team();
			testTeam.setId(String.valueOf(i));
			testTeam.setName("My Test Team " + i);
			teams.add(testTeam);
		}
		
		teamIds = getTeamIds(teams);
		when(mockPaginatedTeamIds.getTeamIds()).thenReturn(teamIds);
		when(mockSynapseJavascriptClient.getUserTeams(anyString(), anyBoolean(), anyString())).thenReturn(getDoneFuture(mockPaginatedTeamIds));
		when(mockSynapseJavascriptClient.listTeams(anyList())).thenReturn(getDoneFuture(teams));
		AsyncMockStubber.callSuccessWith(openRequestNumberPerTeam).when(mockSynapseJavascriptClient).getOpenMembershipRequestCount(anyString(), any(AsyncCallback.class));
		return teams;
	}
	
	@Test
	public void testGetTeamBundlesNoRequests() throws Exception {
		setupUserTeams(3,1);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);

		setPlaceMyProfile("12345");

		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockSynapseJavascriptClient).getUserTeams(eq("12345"), eq(true), eq(null));
		verify(mockSynapseJavascriptClient).listTeams(teamIds);
		verify(mockTeamListWidget).addTeam(any(Team.class));
		verify(mockView, Mockito.never()).setTeamNotificationCount(anyString());
		verify(mockLoadMoreContainer, never()).clear();
		
		// if we set the place and get teams again, verify existing teams load more container is cleared (deregisters callback)
		profilePresenter.setPlace(place);
		verify(mockLoadMoreContainer).clear();
	}
	
	@Test
	public void testGetTeamBundlesWithRequests() throws Exception {
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		
		setPlaceMyProfile(userProfile.getOwnerId());
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockSynapseJavascriptClient).getUserTeams(userProfile.getOwnerId(), true, null);
		verify(mockSynapseJavascriptClient).listTeams(teamIds);
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class));
	}
	
	@Test
	public void testGetUserTeamsFailure() throws Exception {
		String userId = "23938473";
		profilePresenter.setCurrentUserId(userId);
		Exception ex = new Exception("unhandled exception");
		when(mockSynapseJavascriptClient.getUserTeams(anyString(), anyBoolean(), anyString())).thenReturn(getFailedFuture(ex));
		
		profilePresenter.getTeamBundles();
		
		verify(mockSynapseJavascriptClient).getUserTeams(userId, true, null);
		verify(mockSynapseJavascriptClient, never()).listTeams(anyList());
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testListTeamsFailure() throws Exception {
		String userId = "23938473";
		profilePresenter.setCurrentUserId(userId);
		Exception ex = new Exception("unhandled exception");
		when(mockSynapseJavascriptClient.listTeams(anyList())).thenReturn(getFailedFuture(ex));
		
		profilePresenter.getTeamBundles();
		
		verify(mockSynapseJavascriptClient).getUserTeams(userId, true, null);
		verify(mockSynapseJavascriptClient).listTeams(anyList());
		verify(mockSynAlert).handleException(ex);
	}

	
	@Test
	public void testGetQueryForRequestCount() throws Exception {
		//when request count is 0, should do nothing
		setupUserTeams(0,1);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		
		setPlaceMyProfile(userProfile.getOwnerId());
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockTeamListWidget).setNotificationValue(anyString(), eq(0L));
		verify(mockView, never()).showErrorMessage(anyString());

		//when request count is >0, should set the request count in the view
		setupUserTeams(5,1);
		reset(mockLoadMoreContainer);
		profilePresenter.getTeamBundles();
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockTeamListWidget).setNotificationValue(anyString(), eq(5L));
		verify(mockView, never()).showErrorMessage(anyString());
	}
	
	private void verifyLoadMoreTeamsConfiguredAndLoading() {
		verify(mockLoadMoreContainer).configure(callbackCaptor.capture());
		verify(mockLoadMoreContainer).onLoadMore();
		//simulate loadMoreContainer calling back to get more results
		callbackCaptor.getValue().invoke();
	}
	
	@Test
	public void testTeamsTabNotOwner() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(false);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockSynapseJavascriptClient).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockSynapseJavascriptClient).listTeams(anyList());
		verify(mockView, never()).setTeamNotificationCount(anyString());
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class));
		verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
	}
	
	@Test
	public void testTeamsTabOwner() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockSynapseJavascriptClient).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockSynapseJavascriptClient).listTeams(anyList());
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class));
	}
	
	@Test
	public void testDownloadsTabOwner() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		when(place.getArea()).thenReturn(ProfileArea.DOWNLOADS);
		
		profilePresenter.tabClicked(ProfileArea.DOWNLOADS);
		
		verify(mockDownloadListWidget).refresh();
		verify(mockView).setDownloadListWidget(any(IsWidget.class));
	}
	
	@Test
	public void testGetTeamsError() {
		profilePresenter.setPlace(place);
		String errorMessage = "error loading teams";
		
		Exception ex = new Exception(errorMessage);
		when(mockSynapseJavascriptClient.getUserTeams(anyString(), anyBoolean(), anyString())).thenReturn(getFailedFuture(ex));
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockSynapseJavascriptClient).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockSynAlert).handleException(ex);
	}
	
	private void setPlaceMyProfile(String myPrincipalId) {
		userProfile.setOwnerId(myPrincipalId);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(myPrincipalId);
		when(place.getUserId()).thenReturn(myPrincipalId);
		profilePresenter.setPlace(place);
	}
	
	@Test
	public void testGetTeamFilters() {
		//SWC-4858: if there's more than one page, verify that it shows all pages
		when(mockPaginatedTeamIds.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		setPlaceMyProfile("456");
		invokeGetMyTeamsCallback();
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		verify(mockSynapseJavascriptClient, times(2)).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockSynapseJavascriptClient, times(2)).listTeams(anyList());
		verify(mockView, atLeastOnce()).setTeamsFilterVisible(true);
		// filters are added, but teams not added to team list (in teams tab).
		verify(mockTeamListWidget, never()).addTeam(any(Team.class));
		verify(mockView, times(4)).addTeamsFilterTeam(any(Team.class));
	}
	
	@Test
	public void testGetTeamFiltersEmpty() {
		setupUserTeams(0, 0);
		setPlaceMyProfile("456");
		invokeGetMyTeamsCallback();
		verify(mockSynapseJavascriptClient).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockSynapseJavascriptClient, never()).listTeams(anyList());

		verify(mockView).setTeamsFilterVisible(false);
	}
	
	@Test
	public void testGetTeamFiltersError() {
		String errorMessage = "error loading teams";
		Exception ex = new Exception(errorMessage);
		when(mockSynapseJavascriptClient.getUserTeams(anyString(), anyBoolean(), anyString())).thenReturn(getFailedFuture(ex));
		setPlaceMyProfile("456");
		invokeGetMyTeamsCallback();
		verify(mockSynapseJavascriptClient).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockView).setTeamsFilterVisible(false);
	}
	
	@Test
	public void testEditMyProfile() {
		String testUserId = "9980";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(testUserId);
		
		profilePresenter.editMyProfile();
		//verify updateView shows Settings as the initial tab
		ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Profile capturedPlace = captor.getValue();
		assertEquals(ProfileArea.SETTINGS, capturedPlace.getArea());
		assertEquals(testUserId, capturedPlace.getUserId());
	}
	@Test
	public void testEditMyProfileAsAnonymous() {
		//verify forces login if anonymous and trying to edit own profile
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		
		profilePresenter.editMyProfile();
		
		verify(mockView).showLoginAlert();
		verify(mockPlaceChanger, never()).goTo(any(Place.class));
	}
	
	@Test
	public void testViewMyProfile() {
		String testUserId = "9981";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(testUserId);
		
		profilePresenter.viewMyProfile("");
		//verify updateView shows Settings as the initial tab
		ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Profile capturedPlace = captor.getValue();
		assertEquals(ProfileArea.PROFILE, capturedPlace.getArea());
		assertEquals(testUserId, capturedPlace.getUserId());
	}
	@Test
	public void testViewMyProfileAsAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		
		//verify forces login if anonymous and trying to view own anonymous profile
		profilePresenter.viewMyProfile("");
		
		verify(mockView).showLoginAlert();
		verify(mockPlaceChanger, never()).goTo(any(Place.class));
	}

	@Test
	public void testUpdateTeamInvites() {
		profilePresenter.setIsOwner(true);
		Long inviteCount = 3L;
		AsyncMockStubber.callSuccessWith(inviteCount).when(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		profilePresenter.updateMembershipInvitationCount();
		
		assertEquals(inviteCount.intValue(), profilePresenter.getInviteCount());
		verify(mockView).setTeamNotificationCount(eq(Long.toString(inviteCount)));
	}
	
	@Test
	public void testAddMembershipRequests() {
		profilePresenter.setIsOwner(true);
		Long beforeNotificationCount = 12L; 
		AsyncMockStubber.callSuccessWith(beforeNotificationCount).when(mockSynapseJavascriptClient).getOpenMembershipRequestCount(any(AsyncCallback.class));
		profilePresenter.updateMembershipRequestCount();
		verify(mockView).setTeamNotificationCount(Long.toString(beforeNotificationCount));
		
		Long inviteCount = 10L;
		AsyncMockStubber.callSuccessWith(inviteCount).when(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		profilePresenter.updateMembershipInvitationCount();
		
		verify(mockView).setTeamNotificationCount(eq(Long.toString(beforeNotificationCount + inviteCount)));
	}
	
	@Test
	public void testUpdateTeamInvitesZero() {
		profilePresenter.setIsOwner(true);
		AsyncMockStubber.callSuccessWith(0L).when(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		profilePresenter.updateMembershipInvitationCount();
		verify(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		assertEquals(0, profilePresenter.getInviteCount());
		verify(mockView, never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testAddMembershipRequestsZero() {
		profilePresenter.setIsOwner(true);
		AsyncMockStubber.callSuccessWith(0L).when(mockSynapseJavascriptClient).getOpenMembershipRequestCount(any(AsyncCallback.class));
		profilePresenter.updateMembershipRequestCount();
		verify(mockSynapseJavascriptClient).getOpenMembershipRequestCount(any(AsyncCallback.class));
		assertEquals(0, profilePresenter.getOpenRequestCount());
		verify(mockView, never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testRefreshTeamsOwnerTeamsAndInvites() {
		int totalNotifications = 12; // must be even for tests to pass
		int inviteCount = totalNotifications/2;
		AsyncMockStubber.callSuccessWith((long)totalNotifications).when(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}	
		setupUserTeams(totalNotifications/2, 1);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		setPlaceMyProfile(targetUserId);
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), eq((CallbackP)null));
		//updates total notifications when refreshing teams
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class));
		//heretofore, have verified proper behavior without adding
		
		//doubling the notifications from invitations
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}
		//doubling number of teams
		setupUserTeams(totalNotifications/2, 2);
		reset(mockLoadMoreContainer);
		profilePresenter.refreshTeams();
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockTeamInviteWidget, times(2)).configure(refreshTeamsCallback.capture(), eq((CallbackP)null));
		
		//invites are still set
		verify(mockView, atLeastOnce()).setTeamNotificationCount(String.valueOf(totalNotifications));
		// on teams page, does not update project team filters
		verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget, times(3)).addTeam(any(Team.class));
	}
	
	@Test
	public void testRefreshTeamsEmpty() {
		int totalNotifications = 0; // must be even for tests to pass
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		setupUserTeams(0, 0);
		profilePresenter.setIsOwner(true);
		profilePresenter.refreshTeams();
		verifyLoadMoreTeamsConfiguredAndLoading();
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), eq((CallbackP)null));
		verify(mockView, never()).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockTeamListWidget).showEmpty();
		verify(mockLoadMoreContainer, times(2)).setIsMore(false);
	}
	
	@Test
	public void testRefreshTeamsOwnerOnlyTeams() {
		int totalNotifications = 12; // must be even for tests to pass
		AsyncMockStubber.callSuccessWith((long)totalNotifications).when(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		int inviteCount = 0;
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}
		setupUserTeams(totalNotifications, 1);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		
		setPlaceMyProfile(targetUserId);
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), eq((CallbackP)null));
		//called by updateTeamInvites due to second switch, even if invites doesn't change
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class));
	}
	
	@Test
	public void testRefreshTeamsOwnerOnlyInvites() {
		int totalNotifications = 12; // must be even for tests to pass
		AsyncMockStubber.callSuccessWith((long)totalNotifications).when(mockSynapseJavascriptClient).getOpenMembershipInvitationCount(any(AsyncCallback.class));
		int inviteCount = totalNotifications;
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}
		setupUserTeams(0, 1);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		
		setPlaceMyProfile(userProfile.getOwnerId());
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), eq((CallbackP)null));
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class));
	}
	
	@Test
	public void testRefreshTeamsNotOwner() {
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("39843");
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		when(place.getUserId()).thenReturn("12345");
		profilePresenter.setIsOwner(false);
		setupUserTeams(3, 1);
		
		profilePresenter.setPlace(place);
		
		verifyLoadMoreTeamsConfiguredAndLoading();
		verify(mockView, never()).setTeamNotificationCount(anyString());
		verify(mockTeamInviteWidget, never()).configure(any(Callback.class), any(CallbackP.class));
		verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class));
	}
	
	@Test
	public void testTabClickedDefault(){
		profilePresenter.tabClicked(null);
		verify(mockView).showErrorMessage(anyString());
		verify(mockView, never()).setTabSelected(any(ProfileArea.class));
	}
	
	@Test
	public void testTabClickedTeams(){
		profilePresenter.setPlace(place);
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		verify(mockView).setTabSelected(eq(ProfileArea.TEAMS));
	}
	
	@Test
	public void testTabClickedSettings(){
		profilePresenter.setPlace(place);
		profilePresenter.tabClicked(ProfileArea.SETTINGS);
		verify(mockView).setTabSelected(eq(ProfileArea.SETTINGS));
		verify(mockSettingsPresenter).configure();
		verify(mockView).setSettingsWidget(any(Widget.class));
	}
	
	@Test
	public void testTabClickedWhileEditing(){
		profilePresenter.setPlace(place);
		when(mockGlobalApplicationState.isEditing()).thenReturn(true);
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		
		ArgumentCaptor<Callback> yesCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showConfirmDialog(anyString(), anyString(), yesCallback.capture());
		verify(mockView).setTabSelected(any(ProfileArea.class));
		
		//click yes
		yesCallback.getValue().invoke();
		verify(mockView, times(2)).setTabSelected(any(ProfileArea.class));
	}
	
	@Test
	public void testUpdateArea() {
		profilePresenter.setPlace(place);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		boolean pushState = true;
		profilePresenter.updateArea(ProfileArea.CHALLENGES, pushState);
		verify(mockGlobalApplicationState).pushCurrentPlace(any(Profile.class));
	}
	
	@Test
	public void testUpdateAreaReplacestate() {
		profilePresenter.setPlace(place);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		boolean pushState = false;
		profilePresenter.updateArea(ProfileArea.CHALLENGES, pushState);
		verify(mockGlobalApplicationState, times(2)).replaceCurrentPlace(any(Profile.class));
	}

	@Test
	public void testUpdateAreaNoChange() {
		profilePresenter.setPlace(place);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		boolean pushState = true;
		profilePresenter.updateArea(ProfileArea.PROJECTS, false);
		verify(mockPlaceChanger, never()).goTo(isA(Profile.class));
	}
	@Test
	public void testInitUserFavorites() {
		List<EntityHeader> favorites = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(favorites).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		Callback mockCallback = mock(Callback.class);
		profilePresenter.initUserFavorites(mockCallback);
		verify(mockGlobalApplicationState).setFavorites(favorites);
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testInitUserFavoritesFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		Callback mockCallback = mock(Callback.class);
		profilePresenter.initUserFavorites(mockCallback);
		verify(mockGlobalApplicationState, never()).setFavorites(anyList());
		verify(mockCallback).invoke();
	}	
	
	@Test
	public void testMessagePlaceToken() {
		String token = "oauth_bound";
		when(place.toToken()).thenReturn(token);
		profilePresenter.setPlace(place);
		verify(mockView).showInfo(DisplayConstants.SUCCESSFULLY_LINKED_OAUTH2_ACCOUNT);
	}

	@Test
	public void testVTokenAnonymous() {
		//go home if trying to access Profile:v while anonymous
		String token = "v";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(place.toToken()).thenReturn(token);
		profilePresenter.setPlace(place);
		verify(mockView).showLoginAlert();
		verify(mockPlaceChanger, never()).goTo(any(Place.class));
	}
	
	@Test
	public void testVTokenLoggedIn() {
		//go home if trying to access Profile:v while anonymous
		String token = "v";
		String currentUserId = "94837";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		when(place.toToken()).thenReturn(token);
		profilePresenter.setPlace(place);
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Profile capturedPlace = (Profile)captor.getValue();
		assertEquals(currentUserId, capturedPlace.getUserId());
		//default area, profile
		assertEquals(ProfileArea.PROFILE, capturedPlace.getArea());
	}
	@Test
	public void testVWithAreaTokenLoggedIn() {
		//go home if trying to access Profile:v while anonymous
		String token = "v/settings";
		String currentUserId = "94837";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		when(place.toToken()).thenReturn(token);
		profilePresenter.setPlace(place);
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Profile capturedPlace = (Profile)captor.getValue();
		assertEquals(currentUserId, capturedPlace.getUserId());
		assertEquals(ProfileArea.SETTINGS, capturedPlace.getArea());
	}
}