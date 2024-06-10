package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.*;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.principal.PrincipalAliasRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.*;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitserver.ChallengeClientImplTest;

@RunWith(GwtMockitoTestRunner.class)
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
  SynapseJSNIUtilsImpl mockJSNIUtils;

  @Mock
  Profile place;

  @Mock
  UserProfileWidget mockUserProfileWidget;

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
  Team mockTeam;

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
  ProjectHeaderList mockProjectHeaderList;

  List<Team> myTeams;
  List<String> teamIds;
  public static final String NEXT_PAGE_TOKEN = "19282";
  public static final String ORC_ID = "https://orcid.goes.here";

  @Before
  public void setup() throws JSONObjectAdapterException {
    when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
    profilePresenter =
      new ProfilePresenter(
        mockView,
        mockAuthenticationController,
        mockGlobalApplicationState,
        mockGwt,
        mockTeamListWidget,
        mockTeamInviteWidget,
        mockInjector,
        mockSynapseJavascriptClient
      );
    verify(mockView).setPresenter(profilePresenter);
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    when(mockInjector.getPromptForValuesModal())
      .thenReturn(mockPromptModalView);
    when(mockInjector.getUserProfileWidget()).thenReturn(mockUserProfileWidget);
    when(mockInjector.getProjectBadgeWidget()).thenReturn(mockProjectBadge);
    when(mockInjector.getChallengeBadgeWidget()).thenReturn(mockChallengeBadge);
    when(mockInjector.getSynapseJSNIUtils()).thenReturn(mockJSNIUtils);
    userProfile.setDisplayName("tester");
    userProfile.setOwnerId("1");
    userProfile.setEmail("original.email@sagebase.org");
    userProfile.setUserName("mr.t");
    testUser.setProfile(userProfile);
    testUser.setSession(new Session());
    testUser.getSession().setSessionToken("token");
    testUser.setIsSSO(false);

    AsyncMockStubber
      .callSuccessWith(mockUserBundle)
      .when(mockSynapseJavascriptClient)
      .getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
    when(mockPrincipalAliasResponse.getPrincipalId())
      .thenReturn(targetUserIdLong);
    AsyncMockStubber
      .callSuccessWith(mockPrincipalAliasResponse)
      .when(mockSynapseJavascriptClient)
      .getPrincipalAlias(
        any(PrincipalAliasRequest.class),
        any(AsyncCallback.class)
      );
    when(mockUserBundle.getUserProfile()).thenReturn(userProfile);
    when(mockUserBundle.getIsCertified()).thenReturn(true);
    when(mockUserBundle.getIsVerified()).thenReturn(false);
    when(mockUserBundle.getORCID()).thenReturn(ORC_ID);
    // by default, we only have a single page of results
    when(mockPaginatedTeamIds.getNextPageToken()).thenReturn(null);
    when(
      mockSynapseJavascriptClient.getUserTeams(
        anyString(),
        anyBoolean(),
        anyString()
      )
    )
      .thenReturn(
        getDoneFuture(mockPaginatedTeamIds),
        getDoneFuture(mockPaginatedTeamIdsPage2)
      );
    myTeams = getUserTeams();
    when(mockSynapseJavascriptClient.listTeams(anyList()))
      .thenReturn(getDoneFuture(myTeams));
    teamIds = getTeamIds(myTeams);
    when(mockPaginatedTeamIds.getTeamIds()).thenReturn(teamIds);
    when(mockPaginatedTeamIdsPage2.getTeamIds()).thenReturn(teamIds);

    // set up get user projects test
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
    when(mockProjectHeaderList.getResults()).thenReturn(myProjects);
    AsyncMockStubber
      .callSuccessWith(mockProjectHeaderList)
      .when(mockSynapseJavascriptClient)
      .getMyProjects(
        any(ProjectListType.class),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    AsyncMockStubber
      .callSuccessWith(mockProjectHeaderList)
      .when(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    AsyncMockStubber
      .callSuccessWith(mockProjectHeaderList)
      .when(mockSynapseJavascriptClient)
      .getProjectsForTeam(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );

    AsyncMockStubber
      .callSuccessWith(myFavorites)
      .when(mockSynapseJavascriptClient)
      .getFavorites(any(AsyncCallback.class));

    // set up create project test
    when(mockProject.getId()).thenReturn("syn88888888");
    when(mockSynapseJavascriptClient.createEntity(any(Entity.class)))
      .thenReturn(getDoneFuture(mockProject));

    // set up create team test
    when(mockTeam.getId()).thenReturn("new team id");
    AsyncMockStubber
      .callSuccessWith(mockTeam)
      .when(mockSynapseJavascriptClient)
      .createTeam(any(Team.class), any(AsyncCallback.class));

    org.sagebionetworks.reflection.model.PaginatedResults<
      EntityHeader
    > testBatchResults =
      new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
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

    when(mockInjector.getLoadMoreProjectsWidgetContainer())
      .thenReturn(mockLoadMoreContainer);
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
    AsyncMockStubber
      .callSuccessWith(testChallenges)
      .when(mockSynapseJavascriptClient)
      .getChallenges(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
  }

  @Test
  public void testUpdateProfileView() {
    boolean isOwner = true;
    String userId = userProfile.getOwnerId();
    when(mockAuthenticationController.isLoggedIn()).thenReturn(isOwner);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn(userId);
    profilePresenter.updateProfileView(userId);

    verify(mockView).clear();
    verify(mockTeamListWidget, Mockito.atLeastOnce()).clear();
    verify(mockView).showLoading();
    verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
    verify(mockUserProfileWidget)
      .configure(eq(userProfile), eq(ORC_ID), any(Callback.class));
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
  public void testPublicView() throws JSONObjectAdapterException {
    // view another user profile
    userProfile.setOwnerId(targetUserId);
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);

    when(place.toToken()).thenReturn(targetUserId);
    when(place.getUserId()).thenReturn(targetUserId);
    profilePresenter.setPlace(place);
    verify(mockSynapseJavascriptClient)
      .getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));

    verify(mockSynapseJavascriptClient, never())
      .getUserTeams(anyString(), anyBoolean(), anyString());

    // not logged in, should not ask for this user favs
    verify(mockSynapseJavascriptClient, never())
      .getFavorites(any(AsyncCallback.class));
  }

  @Test
  public void testViewUsername() {
    // view another user profile based on username
    userProfile.setOwnerId(targetUserId);
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
    when(place.toToken()).thenReturn(targetUsername);
    when(place.getUserId()).thenReturn(null);
    profilePresenter.setPlace(place);

    verify(mockSynapseJavascriptClient)
      .getPrincipalAlias(
        principalAliasRequestCaptor.capture(),
        any(AsyncCallback.class)
      );
    assertEquals(
      targetUsername,
      principalAliasRequestCaptor.getValue().getAlias()
    );
    verify(mockSynapseJavascriptClient)
      .getUserBundle(
        eq(Long.parseLong(targetUserId)),
        anyInt(),
        any(AsyncCallback.class)
      );
    verify(place).setUserId(targetUserId);
  }

  @Test
  public void testViewUsernameFailure() {
    // view another user profile based on username
    userProfile.setOwnerId(targetUserId);
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
    when(place.toToken()).thenReturn(targetUsername);
    when(place.getUserId()).thenReturn(null);
    Exception ex = new Exception("an error");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockSynapseJavascriptClient)
      .getPrincipalAlias(
        any(PrincipalAliasRequest.class),
        any(AsyncCallback.class)
      );

    profilePresenter.setPlace(place);

    verify(mockSynapseJavascriptClient)
      .getPrincipalAlias(
        any(PrincipalAliasRequest.class),
        any(AsyncCallback.class)
      );
    verify(mockSynAlert).handleException(ex);
  }

  private void invokeGetMyTeamsCallback() {
    ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(
      Callback.class
    );
    verify(mockGwt)
      .scheduleExecution(
        callbackCaptor.capture(),
        eq(ProfilePresenter.DELAY_GET_MY_TEAMS)
      );
    callbackCaptor.getValue().invoke();
  }

  @Test
  public void testViewMyProfileNoRedirect() throws JSONObjectAdapterException {
    // view another user profile
    setPlaceMyProfile("456");
    verify(mockSynapseJavascriptClient)
      .getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));

    verify(mockSynapseJavascriptClient, never())
      .getUserTeams(anyString(), anyBoolean(), anyString());
    // should attempt to get my teams, but delayed.
    invokeGetMyTeamsCallback();
    // also verify that it is asking for the correct teams
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockSynapseJavascriptClient)
      .getUserTeams(captor.capture(), anyBoolean(), anyString());
    verify(mockSynapseJavascriptClient).listTeams(anyList());
    assertEquals("456", captor.getValue());
  }

  @Test
  public void testEditMyProfileNoRedirect() {
    // view another user profile
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn("1");
    when(place.getUserId()).thenReturn("2");
    profilePresenter.setPlace(place);
    verify(mockView).setTabSelected(ProfileArea.PROFILE);
    verify(mockSynapseJavascriptClient)
      .getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
    verify(mockView, never()).showLoginAlert();
  }

  @Test
  public void testGetProfileError() {
    // some other error occurred
    Exception ex = new Exception("unhandled");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockSynapseJavascriptClient)
      .getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
    profilePresenter.updateProfileView("1");
    verify(mockView).hideLoading();
    verify(mockSynAlert).handleException(ex);
  }

  @Test
  public void testRefreshProjects() {
    String nextPageToken = "5ef2abc";
    profilePresenter.setProjectNextPageToken(nextPageToken);
    // on refresh, current project next page token should be reset to null
    profilePresenter.refreshProjects();
    assertNull(profilePresenter.getProjectNextPageToken());
    verify(mockInjector).getLoadMoreProjectsWidgetContainer();
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockLoadMoreContainer, times(2)).setIsMore(false);
    verify(mockLoadMoreContainer).configure(any(Callback.class));
  }

  @Test
  public void testGetAllMyProjects() {
    profilePresenter.setIsOwner(true);
    // when setting the filter to all, it should ask for all of my projects
    profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockView).setAllProjectsFilterSelected();
    verify(mockView).showProjectFiltersUI();
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.ALL),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
    verify(mockView).setLastActivityOnColumnVisible(true);
    verify(mockLoadMoreContainer, never()).clear();

    // if we set the place and get projects again, verify existing project load more container is
    // cleared (deregisters callback)
    profilePresenter.setPlace(place);
    verify(mockLoadMoreContainer).clear();
  }

  @Test
  public void testGetAllTheirProjects() {
    profilePresenter.setIsOwner(false);
    // when setting the filter to all, it should ask for all of their projects
    profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
  }

  @Test
  public void testGetProjectsDefaultFilter() {
    profilePresenter.setIsOwner(false);
    // when setting the filter to all, it should ask for all of their projects
    profilePresenter.setProjectFilterAndRefresh(null, null);
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
  }

  @Test
  public void testGetProjectsDefaultFilterFailure() {
    AsyncMockStubber
      .callFailureWith(new Exception("unhandled"))
      .when(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    profilePresenter.setIsOwner(false);
    // when setting the filter to all, it should ask for all of their projects
    profilePresenter.setProjectFilterAndRefresh(null, null);
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockSynAlert).handleException(any(Exception.class));
  }

  @Test
  public void testGetProjectsCreatedByMe() {
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("125");
    // when setting the filter to my projects, it should query for projects created by me
    profilePresenter.setProjectFilterAndRefresh(
      ProjectFilterEnum.CREATED_BY_ME,
      null
    );
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockView).showProjectFiltersUI();
    verify(mockView).setMyProjectsFilterSelected();
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.CREATED),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
    verify(mockView).setLastActivityOnColumnVisible(true);
  }

  @Test
  public void testGetFavorites() {
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("125");
    profilePresenter.setProjectFilterAndRefresh(
      ProjectFilterEnum.FAVORITES,
      null
    );
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockView).showProjectFiltersUI();
    verify(mockView).setFavoritesFilterSelected();
    verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
    verify(mockView).setLastActivityOnColumnVisible(false);
  }

  @Test
  public void testGetSharedDirectlyWithMeProjects() {
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("125");
    profilePresenter.setProjectFilterAndRefresh(
      ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME,
      null
    );
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockView).showProjectFiltersUI();
    verify(mockView).setSharedDirectlyWithMeFilterSelected();
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.PARTICIPATED),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
    verify(mockView).setLastActivityOnColumnVisible(true);
  }

  @Test
  public void testGetFavoritesEmpty() {
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("125");
    myFavorites.clear();
    profilePresenter.setProjectFilterAndRefresh(
      ProjectFilterEnum.FAVORITES,
      null
    );
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

    // when setting the filter to all, it should ask for all of my projects
    profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, teamId);
    verify(mockView).setProjectContainer(any(Widget.class));
    verify(mockView).showProjectFiltersUI();
    verify(mockView).setTeamsFilterSelected();
    verify(mockSynapseJavascriptClient)
      .getProjectsForTeam(
        eq(teamId),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
    verify(mockView).setLastActivityOnColumnVisible(true);
  }

  @Test
  public void testGetProjectsByTeamFailure() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    AsyncMockStubber
      .callFailureWith(new Exception("failed"))
      .when(mockSynapseJavascriptClient)
      .getProjectsForTeam(
        anyString(),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, "123");
    verify(mockSynAlert).handleException(any(Exception.class));
  }

  @Test
  public void testGetProjectCreatedByMeFailure() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("111");
    AsyncMockStubber
      .callFailureWith(new Exception("unhandled"))
      .when(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.CREATED),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    profilePresenter.setProjectFilterAndRefresh(
      ProjectFilterEnum.CREATED_BY_ME,
      null
    );
    verify(mockSynAlert).handleException(any(Exception.class));
  }

  @Test
  public void testApplyFilterClickedAll() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL, null);
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.ALL),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockView).setLastActivityOnColumnVisible(true);
    verify(mockGlobalApplicationState).pushCurrentPlace(any(Place.class));
  }

  @Test
  public void testApplyFilterClickedMine() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("007");
    profilePresenter.applyFilterClicked(ProjectFilterEnum.CREATED_BY_ME, null);
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.CREATED),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockView).setLastActivityOnColumnVisible(true);
  }

  @Test
  public void testApplyFilterClickedMyParticipatedProjects() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("007");
    profilePresenter.applyFilterClicked(
      ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME,
      null
    );
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.PARTICIPATED),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockView).setLastActivityOnColumnVisible(true);
  }

  @Test
  public void testApplyFilterClickedMyTeamProjects() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    profilePresenter.setCurrentUserId("007");
    profilePresenter.applyFilterClicked(
      ProjectFilterEnum.ALL_MY_TEAM_PROJECTS,
      null
    );
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.TEAM),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
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
    AsyncMockStubber
      .callFailureWith(new Exception("unhandled"))
      .when(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.ALL),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    profilePresenter.getMyProjects(
      ProjectListType.ALL,
      ProjectFilterEnum.ALL,
      null
    );
    verify(mockSynapseJavascriptClient)
      .getMyProjects(
        eq(ProjectListType.ALL),
        anyInt(),
        anyString(),
        any(ProjectListSortColumn.class),
        any(SortDirection.class),
        any(AsyncCallback.class)
      );
    verify(mockSynAlert).handleException(any(Exception.class));
  }

  @Test
  public void testProjectPageAddedNoMoreResults() {
    profilePresenter.setLoadMoreProjectsWidgetContainer(mockLoadMoreContainer);
    profilePresenter.projectPageAdded(null);
    verify(mockLoadMoreContainer).setIsMore(false);
  }

  @Test
  public void testProjectPageAddedWithMoreResults() {
    profilePresenter.setLoadMoreProjectsWidgetContainer(mockLoadMoreContainer);
    String nextPageToken = "abcdef";
    profilePresenter.projectPageAdded(nextPageToken);
    assertEquals(nextPageToken, profilePresenter.getProjectNextPageToken());
    verify(mockLoadMoreContainer).setIsMore(true);
  }

  @Test
  public void testCreateProject() {
    profilePresenter.createProjectAfterPrompt("valid name");
    verify(mockSynapseJavascriptClient).createEntity(any(Entity.class));
    // inform user of success, and go to new project page
    verify(mockView).showInfo(anyString());
    verify(mockPlaceChanger).goTo(isA(Synapse.class));
  }

  @Test
  public void testCreateProjectEmptyName() {
    profilePresenter.createProjectAfterPrompt("");
    verify(mockSynapseJavascriptClient, never())
      .createEntity(any(Entity.class));
    verify(mockPromptModalView).showError(anyString());
    reset(mockPromptModalView);

    profilePresenter.createProjectAfterPrompt(null);

    verify(mockSynapseJavascriptClient, never())
      .createEntity(any(Entity.class));
    verify(mockPromptModalView).showError(anyString());
  }

  @Test
  public void testCreateProjectError() {
    String errorMessage = "unhandled";
    when(mockSynapseJavascriptClient.createEntity(any(Entity.class)))
      .thenReturn(getFailedFuture(new Exception(errorMessage)));

    profilePresenter.createProjectAfterPrompt("valid name");
    verify(mockPromptModalView).showError(errorMessage);
  }

  @Test
  public void testCreateProjectNameConflictError() {
    when(mockSynapseJavascriptClient.createEntity(any(Entity.class)))
      .thenReturn(
        getFailedFuture(new ConflictException("special handled exception type"))
      );
    profilePresenter.createProjectAfterPrompt("valid name");
    verify(mockPromptModalView)
      .showError(eq(DisplayConstants.WARNING_PROJECT_NAME_EXISTS));
  }

  @Test
  public void testSortedByLatestActivity() {
    profilePresenter.sort(ProjectListSortColumn.LAST_ACTIVITY);
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        eq(ProjectListSortColumn.LAST_ACTIVITY),
        eq(SortDirection.ASC),
        any(AsyncCallback.class)
      );

    profilePresenter.sort(ProjectListSortColumn.LAST_ACTIVITY);
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        eq(ProjectListSortColumn.LAST_ACTIVITY),
        eq(SortDirection.DESC),
        any(AsyncCallback.class)
      );

    profilePresenter.sort(ProjectListSortColumn.PROJECT_NAME);
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        eq(ProjectListSortColumn.PROJECT_NAME),
        eq(SortDirection.ASC),
        any(AsyncCallback.class)
      );

    profilePresenter.sort(ProjectListSortColumn.PROJECT_NAME);
    verify(mockSynapseJavascriptClient)
      .getUserProjects(
        anyString(),
        anyInt(),
        anyString(),
        eq(ProjectListSortColumn.PROJECT_NAME),
        eq(SortDirection.DESC),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testCreateTeam() {
    profilePresenter.createTeamAfterPrompt("valid name");
    verify(mockSynapseJavascriptClient)
      .createTeam(any(Team.class), any(AsyncCallback.class));
    // inform user of success, and go to new team page
    verify(mockView).showInfo(anyString());
    verify(mockPlaceChanger)
      .goTo(isA(org.sagebionetworks.web.client.place.Team.class));
  }

  @Test
  public void testCreateTeamEmptyName() {
    profilePresenter.createTeamAfterPrompt("");
    verify(mockSynapseJavascriptClient, never())
      .createTeam(any(Team.class), any(AsyncCallback.class));
    verify(mockPromptModalView).showError(anyString());
    reset(mockPromptModalView);
    profilePresenter.createTeamAfterPrompt(null);
    verify(mockSynapseJavascriptClient, never())
      .createTeam(any(Team.class), any(AsyncCallback.class));
    verify(mockPromptModalView).showError(anyString());
  }

  @Test
  public void testCreateTeamError() {
    String errorMessage = "unhandled";
    AsyncMockStubber
      .callFailureWith(new Exception(errorMessage))
      .when(mockSynapseJavascriptClient)
      .createTeam(any(Team.class), any(AsyncCallback.class));
    profilePresenter.createTeamAfterPrompt("valid name");
    verify(mockPromptModalView).showError(errorMessage);
  }

  @Test
  public void testCreateTeamNameConflictError() {
    AsyncMockStubber
      .callFailureWith(new ConflictException("special handled exception type"))
      .when(mockSynapseJavascriptClient)
      .createTeam(any(Team.class), any(AsyncCallback.class));
    profilePresenter.createTeamAfterPrompt("valid name");
    verify(mockPromptModalView)
      .showError(DisplayConstants.WARNING_TEAM_NAME_EXISTS);
  }

  @Test
  public void testRedirectToProfile() {
    // when visiting someone else's profile, always show their profile (sub-areas removed in design)
    when(place.getArea()).thenReturn(ProfileArea.CHALLENGES);
    profilePresenter.setPlace(place);
    profilePresenter.getMoreChallenges();
    verify(mockView, atLeastOnce()).setTabSelected(ProfileArea.PROFILE);
  }

  // Challenge tests

  @Test
  public void testChallengePlace() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn("1");
    when(place.getUserId()).thenReturn("1");
    when(place.getArea()).thenReturn(ProfileArea.CHALLENGES);
    profilePresenter.setPlace(place);
    profilePresenter.getMoreChallenges();
    verify(mockView, atLeastOnce()).setTabSelected(ProfileArea.CHALLENGES);
  }

  @Test
  public void testRefreshChallenges() {
    profilePresenter.showTab(ProfileArea.CHALLENGES, true);
    verify(mockView).clearChallenges();
    assertEquals(
      ProfilePresenter.CHALLENGE_PAGE_SIZE,
      profilePresenter.getCurrentChallengeOffset()
    );
    verify(mockView, times(2)).showChallengesLoading(anyBoolean());
    verify(mockView).addChallengeWidget(any(Widget.class));
  }

  public ArrayList<Team> setupUserTeams(
    long openRequestNumberPerTeam,
    int numTeams
  ) {
    ArrayList<Team> teams = new ArrayList<Team>();
    for (int i = 0; i < numTeams; i++) {
      Team testTeam = new Team();
      testTeam.setId(String.valueOf(i));
      testTeam.setName("My Test Team " + i);
      teams.add(testTeam);
    }

    teamIds = getTeamIds(teams);
    when(mockPaginatedTeamIds.getTeamIds()).thenReturn(teamIds);
    when(
      mockSynapseJavascriptClient.getUserTeams(
        anyString(),
        anyBoolean(),
        anyString()
      )
    )
      .thenReturn(getDoneFuture(mockPaginatedTeamIds));
    when(mockSynapseJavascriptClient.listTeams(anyList()))
      .thenReturn(getDoneFuture(teams));
    AsyncMockStubber
      .callSuccessWith(openRequestNumberPerTeam)
      .when(mockSynapseJavascriptClient)
      .getOpenMembershipRequestCount(anyString(), any(AsyncCallback.class));
    return teams;
  }

  @Test
  public void testGetTeamBundlesNoRequests() throws Exception {
    setupUserTeams(3, 1);
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);

    setPlaceMyProfile("12345");

    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockSynapseJavascriptClient)
      .getUserTeams(eq("12345"), eq(true), eq(null));
    verify(mockSynapseJavascriptClient).listTeams(teamIds);
    verify(mockTeamListWidget).addTeam(any(Team.class));
    verify(mockLoadMoreContainer, never()).clear();

    // if we set the place and get teams again, verify existing teams load more container is cleared
    // (deregisters callback)
    profilePresenter.setPlace(place);
    verify(mockLoadMoreContainer).clear();
  }

  @Test
  public void testGetTeamBundlesWithRequests() throws Exception {
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);

    setPlaceMyProfile(userProfile.getOwnerId());

    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockSynapseJavascriptClient)
      .getUserTeams(userProfile.getOwnerId(), true, null);
    verify(mockSynapseJavascriptClient).listTeams(teamIds);
    verify(mockTeamListWidget, times(2)).addTeam(any(Team.class));
  }

  @Test
  public void testGetUserTeamsFailure() throws Exception {
    String userId = "23938473";
    profilePresenter.setCurrentUserId(userId);
    Exception ex = new Exception("unhandled exception");
    when(
      mockSynapseJavascriptClient.getUserTeams(
        anyString(),
        anyBoolean(),
        anyString()
      )
    )
      .thenReturn(getFailedFuture(ex));

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
    when(mockSynapseJavascriptClient.listTeams(anyList()))
      .thenReturn(getFailedFuture(ex));

    profilePresenter.getTeamBundles();

    verify(mockSynapseJavascriptClient).getUserTeams(userId, true, null);
    verify(mockSynapseJavascriptClient).listTeams(anyList());
    verify(mockSynAlert).handleException(ex);
  }

  @Test
  public void testGetQueryForRequestCount() throws Exception {
    // when request count is 0, should do nothing
    setupUserTeams(0, 1);
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);

    setPlaceMyProfile(userProfile.getOwnerId());

    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockTeamListWidget).setNotificationValue(anyString(), eq(0L));
    verify(mockView, never()).showErrorMessage(anyString());

    // when request count is >0, should set the request count in the view
    setupUserTeams(5, 1);
    reset(mockLoadMoreContainer);
    profilePresenter.getTeamBundles();
    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockTeamListWidget).setNotificationValue(anyString(), eq(5L));
    verify(mockView, never()).showErrorMessage(anyString());
  }

  private void verifyLoadMoreTeamsConfiguredAndLoading() {
    verify(mockLoadMoreContainer).configure(callbackCaptor.capture());
    verify(mockLoadMoreContainer).onLoadMore();
    // simulate loadMoreContainer calling back to get more results
    callbackCaptor.getValue().invoke();
  }

  @Test
  public void testTeamsTabNotOwner() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(false);
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);
    profilePresenter.showTab(ProfileArea.TEAMS, true);

    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockSynapseJavascriptClient)
      .getUserTeams(anyString(), anyBoolean(), anyString());
    verify(mockSynapseJavascriptClient).listTeams(anyList());
    verify(mockTeamListWidget, times(2)).addTeam(any(Team.class));
    verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
  }

  @Test
  public void testTeamsTabOwner() {
    profilePresenter.setPlace(place);
    profilePresenter.setIsOwner(true);
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);

    profilePresenter.showTab(ProfileArea.TEAMS, true);

    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockSynapseJavascriptClient)
      .getUserTeams(anyString(), anyBoolean(), anyString());
    verify(mockSynapseJavascriptClient).listTeams(anyList());
    verify(mockTeamListWidget, times(2)).addTeam(any(Team.class));
  }

  @Test
  public void testGetTeamsError() {
    profilePresenter.setPlace(place);
    String errorMessage = "error loading teams";

    Exception ex = new Exception(errorMessage);
    when(
      mockSynapseJavascriptClient.getUserTeams(
        anyString(),
        anyBoolean(),
        anyString()
      )
    )
      .thenReturn(getFailedFuture(ex));
    profilePresenter.showTab(ProfileArea.TEAMS, true);
    verifyLoadMoreTeamsConfiguredAndLoading();
    verify(mockSynapseJavascriptClient)
      .getUserTeams(anyString(), anyBoolean(), anyString());
    verify(mockSynAlert).handleException(ex);
  }

  private void setPlaceMyProfile(String myPrincipalId) {
    userProfile.setOwnerId(myPrincipalId);

    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn(myPrincipalId);
    when(place.getUserId()).thenReturn(myPrincipalId);
    profilePresenter.setPlace(place);
  }

  @Test
  public void testGetTeamFilters() {
    // SWC-4858: if there's more than one page, verify that it shows all pages
    when(mockPaginatedTeamIds.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
    when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
    setPlaceMyProfile("456");
    invokeGetMyTeamsCallback();
    verify(mockJSNIUtils).setPageTitle(userProfile.getUserName());
    profilePresenter.showTab(ProfileArea.PROJECTS, true);
    verify(mockJSNIUtils, times(2))
      .setPageTitle(
        userProfile.getUserName() +
        " - " +
        ProfileArea.PROJECTS.name().toLowerCase()
      );
    verify(mockSynapseJavascriptClient, times(2))
      .getUserTeams(anyString(), anyBoolean(), anyString());
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
    verify(mockSynapseJavascriptClient)
      .getUserTeams(anyString(), anyBoolean(), anyString());
    verify(mockSynapseJavascriptClient, never()).listTeams(anyList());

    verify(mockView).setTeamsFilterVisible(false);
  }

  @Test
  public void testGetTeamFiltersError() {
    String errorMessage = "error loading teams";
    Exception ex = new Exception(errorMessage);
    when(
      mockSynapseJavascriptClient.getUserTeams(
        anyString(),
        anyBoolean(),
        anyString()
      )
    )
      .thenReturn(getFailedFuture(ex));
    setPlaceMyProfile("456");
    invokeGetMyTeamsCallback();
    verify(mockSynapseJavascriptClient)
      .getUserTeams(anyString(), anyBoolean(), anyString());
    verify(mockView).setTeamsFilterVisible(false);
  }

  @Test
  public void testViewMyProfileAsAnonymous() {
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);

    // verify forces login if anonymous and trying to view own anonymous profile
    profilePresenter.viewMyProfile();

    verify(mockView).showLoginAlert();
    verify(mockPlaceChanger, never()).goTo(any(Place.class));
  }

  @Test
  public void testRefreshTeamsEmpty() {
    int totalNotifications = 0; // must be even for tests to pass
    List<OpenUserInvitationBundle> invites = new ArrayList<
      OpenUserInvitationBundle
    >();
    setupUserTeams(0, 0);
    profilePresenter.setIsOwner(true);
    profilePresenter.refreshTeams();
    verifyLoadMoreTeamsConfiguredAndLoading();
    ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(
      Callback.class
    );
    verify(mockTeamInviteWidget)
      .configure(refreshTeamsCallback.capture(), eq((CallbackP) null));
    verify(mockTeamListWidget).showEmpty();
    verify(mockLoadMoreContainer, times(2)).setIsMore(false);
  }

  @Test
  public void testRefreshTeamsOwnerOnlyTeams() {
    int totalNotifications = 12; // must be even for tests to pass
    AsyncMockStubber
      .callSuccessWith((long) totalNotifications)
      .when(mockSynapseJavascriptClient)
      .getOpenMembershipInvitationCount(any(AsyncCallback.class));
    int inviteCount = 0;
    List<OpenUserInvitationBundle> invites = new ArrayList<
      OpenUserInvitationBundle
    >();
    for (int i = 0; i < inviteCount; i++) {
      invites.add(new OpenUserInvitationBundle());
    }
    setupUserTeams(totalNotifications, 1);
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);

    setPlaceMyProfile(targetUserId);

    verifyLoadMoreTeamsConfiguredAndLoading();
    ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(
      Callback.class
    );
    verify(mockTeamInviteWidget)
      .configure(refreshTeamsCallback.capture(), eq((CallbackP) null));
    verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
    verify(mockTeamListWidget).addTeam(any(Team.class));
  }

  @Test
  public void testRefreshTeamsOwnerOnlyInvites() {
    int totalNotifications = 12; // must be even for tests to pass
    AsyncMockStubber
      .callSuccessWith((long) totalNotifications)
      .when(mockSynapseJavascriptClient)
      .getOpenMembershipInvitationCount(any(AsyncCallback.class));
    int inviteCount = totalNotifications;
    List<OpenUserInvitationBundle> invites = new ArrayList<
      OpenUserInvitationBundle
    >();
    for (int i = 0; i < inviteCount; i++) {
      invites.add(new OpenUserInvitationBundle());
    }
    setupUserTeams(0, 1);
    when(place.getArea()).thenReturn(ProfileArea.TEAMS);

    setPlaceMyProfile(userProfile.getOwnerId());

    verifyLoadMoreTeamsConfiguredAndLoading();
    ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(
      Callback.class
    );
    verify(mockTeamInviteWidget)
      .configure(refreshTeamsCallback.capture(), eq((CallbackP) null));
    verify(mockView, never()).addTeamsFilterTeam(any(Team.class));
    verify(mockTeamListWidget).addTeam(any(Team.class));
  }

  @Test
  public void testTabClickedTeams() {
    profilePresenter.setPlace(place);
    profilePresenter.showTab(ProfileArea.TEAMS, true);
    verify(mockView).setTabSelected(eq(ProfileArea.TEAMS));
  }

  @Test
  public void testTabClickedSettings() {
    profilePresenter.setPlace(place);
    profilePresenter.showTab(ProfileArea.SETTINGS, true);
    verify(mockView).setTabSelected(eq(ProfileArea.SETTINGS));
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
    verify(mockGlobalApplicationState, times(2))
      .replaceCurrentPlace(any(Profile.class));
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
    AsyncMockStubber
      .callSuccessWith(favorites)
      .when(mockSynapseJavascriptClient)
      .getFavorites(any(AsyncCallback.class));
    Callback mockCallback = mock(Callback.class);
    profilePresenter.initUserFavorites(mockCallback);
    verify(mockGlobalApplicationState).setFavorites(favorites);
    verify(mockCallback).invoke();
  }

  @Test
  public void testInitUserFavoritesFailure() {
    AsyncMockStubber
      .callFailureWith(new Exception("unhandled"))
      .when(mockSynapseJavascriptClient)
      .getFavorites(any(AsyncCallback.class));
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
    verify(mockView)
      .showInfo(DisplayConstants.SUCCESSFULLY_LINKED_OAUTH2_ACCOUNT);
  }

  @Test
  public void testVTokenAnonymous() {
    // go home if trying to access Profile:v while anonymous
    String token = "v";
    when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
    when(place.toToken()).thenReturn(token);
    profilePresenter.setPlace(place);
    verify(mockView).showLoginAlert();
    verify(place, never()).setUserId(anyString());
    verify(mockGlobalApplicationState, never())
      .replaceCurrentPlace(any(Place.class));
  }

  @Test
  public void testVWithAreaTokenLoggedIn() {
    // go home if trying to access Profile:v while anonymous
    String token = "v/settings";
    String currentUserId = "94837";
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    when(mockAuthenticationController.getCurrentUserPrincipalId())
      .thenReturn(currentUserId);
    when(place.toToken()).thenReturn(token);

    profilePresenter.setPlace(place);

    verify(place).setUserId(currentUserId);
    verify(mockGlobalApplicationState, atLeastOnce())
      .replaceCurrentPlace(place);
  }
}
