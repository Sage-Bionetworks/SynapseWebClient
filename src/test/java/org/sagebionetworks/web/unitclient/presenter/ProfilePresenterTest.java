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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.principal.PrincipalAliasRequest;
import org.sagebionetworks.repo.model.principal.PrincipalAliasResponse;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.presenter.SortOptionEnum;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
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
	CookieProvider mockCookies;
	@Mock
	UserProfileModalWidget mockUserProfileModalWidget;
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String password = "password";
	SortOptionEnum sort = SortOptionEnum.LATEST_ACTIVITY;
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
	List<VerificationState> verificationStateList;
	
	@Mock
	UserBundle mockUserBundle;
	@Mock
	UserBundle mockCurrentUserBundle;
	@Mock
	Team mockTeam;
	@Mock
	VerificationSubmissionWidget mockVerificationSubmissionModal;
	@Mock
	VerificationSubmission mockVerificationSubmission;
	@Mock
	SettingsPresenter mockSettingsPresenter;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	PromptModalView mockPromptModalView;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PaginatedTeamIds mockPaginatedTeamIds;
	@Mock
	Project mockProject;
	@Mock
	PrincipalAliasResponse mockPrincipalAliasResponse;
	@Mock
	DownloadListWidget mockDownloadListWidget;
	
	List<Team> myTeams;
	List<String> teamIds;
	public static final String NEXT_PAGE_TOKEN = "19282";
	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		profilePresenter = new ProfilePresenter(mockView, 
				mockAuthenticationController, 
				mockGlobalApplicationState,
				mockCookies,
				mockGwt, 
				mockTeamListWidget, 
				mockTeamInviteWidget, 
				mockInjector,
				mockIsACTMemberAsyncHandler,
				mockDateTimeUtils,
				mockSynapseJavascriptClient
				);
		verify(mockView).setPresenter(profilePresenter);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockInjector.getPromptModal()).thenReturn(mockPromptModalView);
		when(mockInjector.getUserProfileModalWidget()).thenReturn(mockUserProfileModalWidget);
		when(mockInjector.getVerificationSubmissionWidget()).thenReturn(mockVerificationSubmissionModal);
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
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).unbindOAuthProvidersUserId(any(OAuthProvider.class), anyString(), any(AsyncCallback.class));
		when(mockPrincipalAliasResponse.getPrincipalId()).thenReturn(targetUserIdLong);
		AsyncMockStubber.callSuccessWith(mockPrincipalAliasResponse).when(mockSynapseJavascriptClient).getPrincipalAlias(any(PrincipalAliasRequest.class), any(AsyncCallback.class));
		when(mockUserBundle.getUserProfile()).thenReturn(userProfile);
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		when(mockPaginatedTeamIds.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockSynapseJavascriptClient.getUserTeams(anyString(), anyBoolean(), anyString())).thenReturn(getDoneFuture(mockPaginatedTeamIds));
		myTeams = getUserTeams();
		when(mockSynapseJavascriptClient.listTeams(anyList())).thenReturn(getDoneFuture(myTeams));
		teamIds = getTeamIds(myTeams);
		when(mockPaginatedTeamIds.getTeamIds()).thenReturn(teamIds);
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
		when(mockUserBundle.getVerificationSubmission()).thenReturn(mockVerificationSubmission);
		verificationStateList = new ArrayList<VerificationState>();
		VerificationState oldState = new VerificationState();
		oldState.setState(VerificationStateEnum.SUSPENDED);
		oldState.setReason("numerous violations of the terms of use");
		verificationStateList.add(oldState);
		when(mockVerificationSubmission.getStateHistory()).thenReturn(verificationStateList);
		when(mockVerificationSubmissionModal.setResubmitCallback(any(Callback.class))).thenReturn(mockVerificationSubmissionModal);
		when(mockVerificationSubmissionModal.configure(any(VerificationSubmission.class), anyBoolean(), anyBoolean())).thenReturn(mockVerificationSubmissionModal);
		
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
	
	public void verifyAndInvokeACTMember(Boolean isACTMember) {
		verify(mockIsACTMemberAsyncHandler, atLeastOnce()).isACTActionAvailable(callbackPCaptor.capture());
		callbackPCaptor.getValue().invoke(isACTMember);
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
		verify(mockView).setSortText(SortOptionEnum.LATEST_ACTIVITY.sortText);
		verify(mockView).setProfileEditButtonVisible(isOwner);
		verify(mockView).setOrcIDLinkButtonVisible(isOwner);
		verify(mockView).showTabs(isOwner);
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
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
		
		verify(mockView).setOrcIdVisible(false);
		verify(mockView).setUnbindOrcIdVisible(false);
		
		verify(mockView, never()).setOrcIdVisible(true);
		verify(mockView, never()).setUnbindOrcIdVisible(true);
	} 
	

	@Test
	public void testEditMyProfileNoRedirect() {
		//view another user profile
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("1");
		when(place.getUserId()).thenReturn("2");
		profilePresenter.setPlace(place);
		verify(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSettingsNotOwner() {
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("2");
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(place.getUserId()).thenReturn("4");
		when(place.getArea()).thenReturn(ProfileArea.SETTINGS);
		profilePresenter.setPlace(place);
		verify(mockView).setTabSelected(eq(ProfileArea.PROFILE));
		verify(mockView).addCertifiedBadge();
		verify(mockView, never()).setGetCertifiedVisible(anyBoolean());
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
	public void testNotCertifiedAlertShownAndUserNotCertifiedCookieNull() throws JSONObjectAdapterException {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userProfile.getOwnerId());
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn(null);
		profilePresenter.updateProfileView(userProfile.getOwnerId());
		verify(mockView).setGetCertifiedVisible(true);
		verify(mockView, never()).addCertifiedBadge();
		
		//also verify that when orc id is not set in the user bundle (for the owner), then orc id is not visible
		verify(mockView).setOrcIdVisible(false);
		verify(mockView).setUnbindOrcIdVisible(false);
		verify(mockView, never()).setOrcIdVisible(true);
		verify(mockView, never()).setUnbindOrcIdVisible(true);
		verify(mockView).setOrcIDLinkButtonVisible(true); 
		verify(mockView, never()).setOrcIDLinkButtonVisible(false); 
	}
	
	@Test
	public void testNotCertifiedAlertShownAndUserNotCertifiedCookieTrue() throws JSONObjectAdapterException {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userProfile.getOwnerId());
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("true");
		when(mockUserBundle.getORCID()).thenReturn("an orc id");
		profilePresenter.updateProfileView(userProfile.getOwnerId());
		verify(mockView).setGetCertifiedVisible(true);
		verify(mockView, never()).addCertifiedBadge();
		
		//also verify that when orc id is set in the user bundle (for the owner), then orc id is visible (and can be unbound)
		verify(mockView).setOrcIdVisible(false);
		verify(mockView).setUnbindOrcIdVisible(false);
		verify(mockView).setOrcIdVisible(true);
		verify(mockView).setUnbindOrcIdVisible(true);
		//link ORC ID button initially visible because this is the owner, but hidden because orc id is set
		verify(mockView).setOrcIDLinkButtonVisible(true); 
		verify(mockView).setOrcIDLinkButtonVisible(false); 
	}
	
	@Test
	public void testUnbindHiddenIfNotOwner() {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("true");
		when(mockUserBundle.getORCID()).thenReturn("an orc id");
		//view another user profile
		profilePresenter.updateProfileView("12937");

		verify(mockView, times(2)).setUnbindOrcIdVisible(false);
	}
	
	@Test
	public void testNotCertifiedAlertHiddenAndUserNotCertifiedCookieFalse() throws JSONObjectAdapterException {
		when(mockUserBundle.getIsCertified()).thenReturn(false);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userProfile.getOwnerId());
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("false");
		profilePresenter.updateProfileView(userProfile.getOwnerId());
		verify(mockView).setGetCertifiedVisible(false);
		verify(mockView, never()).addCertifiedBadge();
	}
	
	@Test
	public void testLinkOrcIdClicked() throws JSONObjectAdapterException {
		when(place.toToken()).thenReturn(targetUserId);
		when(place.getUserId()).thenReturn(targetUserId);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		profilePresenter.setPlace(place);
		when(mockUserBundle.getORCID()).thenReturn("a value");
		profilePresenter.linkOrcIdClicked();
		verify(mockView).showErrorMessage(anyString());
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
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
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
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_CREATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
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
		verify(mockView).setProjectSortVisible(false);
	}
	
	@Test
	public void testGetSharedDirectlyWithMeProjects() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null);
		verify(mockView).setProjectContainer(any(Widget.class));
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setSharedDirectlyWithMeFilterSelected();
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_PARTICIPATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockLoadMoreContainer, times(2)).add(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
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
		verify(mockView).setProjectSortVisible(false);
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
		verify(mockView).setProjectSortVisible(true);
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
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_CREATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),   any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockSynAlert).handleException(any(Exception.class));
	}
	
	@Test
	public void testApplyFilterClickedAll() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
		verify(mockGlobalApplicationState).pushCurrentPlace(any(Place.class));
	}
	
	@Test
	public void testApplyFilterClickedMine() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_CREATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMyParticipatedProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_PARTICIPATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMyTeamProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, null);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_TEAM_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}

	
	@Test
	public void testApplyFilterClickedFavorites() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.FAVORITES, null);
		verify(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(false);
	}
	
	@Test
	public void testGetMyProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		profilePresenter.getMyProjects(ProjectListType.MY_PROJECTS, ProjectFilterEnum.ALL, 0);
		verify(mockSynapseJavascriptClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
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
		when(mockPromptModalView.getValue()).thenReturn("valid name");
		profilePresenter.createProjectAfterPrompt();
		verify(mockSynapseJavascriptClient).createEntity(any(Entity.class));
		//inform user of success, and go to new project page
		verify(mockView).showInfo(anyString());
		verify(mockPlaceChanger).goTo(isA(Synapse.class));
	}

	@Test
	public void testCreateProjectEmptyName() {
		when(mockPromptModalView.getValue()).thenReturn("");
		profilePresenter.createProjectAfterPrompt();
		verify(mockSynapseJavascriptClient, never()).createEntity(any(Entity.class));
		verify(mockPromptModalView).showError(anyString());
		Mockito.reset(mockPromptModalView);
		
		when(mockPromptModalView.getValue()).thenReturn(null);
		profilePresenter.createProjectAfterPrompt();

		verify(mockSynapseJavascriptClient, never()).createEntity(any(Entity.class));
		verify(mockPromptModalView).showError(anyString());
	}

	@Test
	public void testCreateProjectError() {
		when(mockPromptModalView.getValue()).thenReturn("valid name");
		profilePresenter.createProjectAfterPrompt();
		String errorMessage = "unhandled";
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new Exception(errorMessage)));
		
		profilePresenter.createProjectAfterPrompt();
		verify(mockPromptModalView).showError(errorMessage);
	}
	
	@Test
	public void testCreateProjectNameConflictError() {
		when(mockSynapseJavascriptClient.createEntity(any(Entity.class))).thenReturn(getFailedFuture(new ConflictException("special handled exception type")));
		when(mockPromptModalView.getValue()).thenReturn("valid name");
		profilePresenter.createProjectAfterPrompt();
		verify(mockPromptModalView).showError(eq(DisplayConstants.WARNING_PROJECT_NAME_EXISTS));
	}
	
	// Project Sorting tests
	@Test
	public void testOptionsAdded() {
		for (SortOptionEnum sort: SortOptionEnum.values())
			verify(mockView).addSortOption(sort);	
	}
	
	@Test
	public void testDefaultSortOption() throws JSONObjectAdapterException {
		profilePresenter.updateProfileView("4443");
		verify(mockView).setSortText(SortOptionEnum.LATEST_ACTIVITY.sortText);
	}
	
	@Test
	public void testSortedByLatestActivity() {
		SortOptionEnum latestActivity = SortOptionEnum.LATEST_ACTIVITY;
		profilePresenter.resort(SortOptionEnum.LATEST_ACTIVITY);
		verify(mockView).setSortText(eq(latestActivity.sortText));
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(latestActivity.sortBy), eq(latestActivity.sortDir), any(AsyncCallback.class));
	}
	
	@Test
	public void testSortedByEarliestActivity() {
		SortOptionEnum earliestActivity = SortOptionEnum.EARLIEST_ACTIVITY;
		profilePresenter.resort(earliestActivity);
		verify(mockView).setSortText(eq(earliestActivity.sortText));
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(earliestActivity.sortBy), eq(earliestActivity.sortDir), any(AsyncCallback.class));
	}
	
	@Test
	public void testSortedByNameAZ() {
		SortOptionEnum nameAZ = SortOptionEnum.NAME_A_Z;
		profilePresenter.resort(nameAZ);
		verify(mockView).setSortText(eq(nameAZ.sortText));
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(nameAZ.sortBy), eq(nameAZ.sortDir), any(AsyncCallback.class));	
	}
	
	@Test
	public void testSortedByLastNameZA() {
		SortOptionEnum nameZA = SortOptionEnum.NAME_Z_A;
		profilePresenter.resort(nameZA);
		verify(mockView).setSortText(nameZA.sortText);
		verify(mockSynapseJavascriptClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(nameZA.sortBy), eq(nameZA.sortDir), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateTeam() {
		when(mockPromptModalView.getValue()).thenReturn("valid name");
		profilePresenter.createTeamAfterPrompt();
		verify(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		//inform user of success, and go to new team page
		verify(mockView).showInfo(anyString());
		verify(mockPlaceChanger).goTo(isA(org.sagebionetworks.web.client.place.Team.class));
	}

	@Test
	public void testCreateTeamEmptyName() {
		when(mockPromptModalView.getValue()).thenReturn("");
		profilePresenter.createTeamAfterPrompt();
		verify(mockSynapseJavascriptClient, never()).createTeam(any(Team.class), any(AsyncCallback.class));
		verify(mockPromptModalView).showError(anyString());
		Mockito.reset(mockPromptModalView);
		when(mockPromptModalView.getValue()).thenReturn(null);
		profilePresenter.createTeamAfterPrompt();
		verify(mockSynapseJavascriptClient, never()).createTeam(any(Team.class), any(AsyncCallback.class));
		verify(mockPromptModalView).showError(anyString());
	}

	@Test
	public void testCreateTeamError() {
		String errorMessage = "unhandled";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		when(mockPromptModalView.getValue()).thenReturn("valid name");
		profilePresenter.createTeamAfterPrompt();
		verify(mockPromptModalView).showError(errorMessage);
	}
	
	@Test
	public void testCreateTeamNameConflictError() {
		AsyncMockStubber.callFailureWith(new ConflictException("special handled exception type")).when(mockSynapseJavascriptClient).createTeam(any(Team.class), any(AsyncCallback.class));
		when(mockPromptModalView.getValue()).thenReturn("valid name");
		profilePresenter.createTeamAfterPrompt();
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
		
		profilePresenter.getTeamBundles(true);
		
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
		
		profilePresenter.getTeamBundles(true);
		
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
		
		verify(mockTeamListWidget).setNotificationValue(anyString(), eq(0L));
		verify(mockView, never()).showErrorMessage(anyString());

		//when request count is >0, should set the request count in the view
		setupUserTeams(5,1);
		profilePresenter.getTeamBundles(true);
		verify(mockTeamListWidget).setNotificationValue(anyString(), eq(5L));
		verify(mockView, never()).showErrorMessage(anyString());
	}
	
	
	@Test
	public void testTeamsTabNotOwner() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(false);
		when(place.getArea()).thenReturn(ProfileArea.TEAMS);
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		
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
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		setPlaceMyProfile("456");
		invokeGetMyTeamsCallback();
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		verify(mockSynapseJavascriptClient).getUserTeams(anyString(), anyBoolean(), anyString());
		verify(mockSynapseJavascriptClient).listTeams(anyList());
		verify(mockView).setTeamsFilterVisible(true);
		// filters are added, but teams not added to team list (in teams tab).
		verify(mockTeamListWidget, never()).addTeam(any(Team.class));
		verify(mockView, times(2)).addTeamsFilterTeam(any(Team.class));
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
		
		verify(mockView).showErrorMessage(eq(DisplayConstants.ERROR_LOGIN_REQUIRED));
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}
	
	@Test
	public void testViewMyProfile() {
		String testUserId = "9981";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(testUserId);
		
		profilePresenter.viewMyProfile();
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
		profilePresenter.viewMyProfile();
		
		verify(mockView).showErrorMessage(eq(DisplayConstants.ERROR_LOGIN_REQUIRED));
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
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
		profilePresenter.refreshTeams();
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
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), eq((CallbackP)null));
		verify(mockView, never()).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockTeamListWidget).showEmpty();
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
	public void testCertificationBadgeClicked() {
		profilePresenter.certificationBadgeClicked();
		verify(mockPlaceChanger).goTo(isA(Certificate.class));
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
	public void testInitShowHideGetCertifiedNotOwner() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn(null);
		profilePresenter.initializeShowHideCertification(false);
		verify(mockView).setGetCertifiedVisible(false);
	}
	
	@Test
	public void testInitShowHideGetCertifiedNullCookieValue() {
		//return null
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn(null);
		profilePresenter.initializeShowHideCertification(true);
		verify(mockView).setGetCertifiedVisible(true);
	}
	
	@Test
	public void testInitShowHideGetCertifiedEmptyCookieValue() {
		//return null
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("");
		profilePresenter.initializeShowHideCertification(true);
		verify(mockView).setGetCertifiedVisible(true);
	}
	
	@Test
	public void testInitShowHideGetCertifiedTrueCookieValue() {
		//return null
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("true");
		profilePresenter.initializeShowHideCertification(true);
		verify(mockView).setGetCertifiedVisible(true);
	}
	@Test
	public void testInitShowHideGetCertifiedFalseCookieValue() {
		//return null
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("false");
		profilePresenter.initializeShowHideCertification(true);
		verify(mockView).setGetCertifiedVisible(false);
	}
	@Test
	public void testGetCertifiedDismissed() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		profilePresenter.setGetCertifiedDismissed();
		verify(mockCookies).setCookie(eq(ProfilePresenter.USER_PROFILE_CERTIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()), eq(Boolean.FALSE.toString()), any(Date.class));
	}
	
	@Test
	public void testInitShowHideGetVerifiedNotOwner() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn(null);
		profilePresenter.initializeShowHideVerification(false);
		//ui elements are hidden by default, so there should be no interactions
		verify(mockView, never()).setVerificationAlertVisible(anyBoolean());
		verify(mockView, never()).setVerificationButtonVisible(anyBoolean());
	}
	
	@Test
	public void testInitShowHideGetVerifiedNullCookieValue() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn(null);
		profilePresenter.initializeShowHideVerification(true);
		verify(mockView).setVerificationAlertVisible(true);
		verify(mockView).setVerificationButtonVisible(false);
	}
	
	@Test
	public void testInitShowHideGetVerifiedEmptyCookieValue() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("");
		profilePresenter.initializeShowHideVerification(true);
		verify(mockView).setVerificationAlertVisible(true);
		verify(mockView).setVerificationButtonVisible(false);
	}
	
	@Test
	public void testInitShowHideGetVerifiedTrueCookieValue() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("true");
		profilePresenter.initializeShowHideVerification(true);
		verify(mockView).setVerificationAlertVisible(true);
		verify(mockView).setVerificationButtonVisible(false);
	}
	@Test
	public void testInitShowHideGetVerifiedFalseCookieValue() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()))).thenReturn("false");
		profilePresenter.initializeShowHideVerification(true);
		verify(mockView).setVerificationAlertVisible(false);
		verify(mockView).setVerificationButtonVisible(true);
	}

	@Test
	public void testVerifiedDismissed() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		profilePresenter.setVerifyDismissed();
		verify(mockCookies).setCookie(eq(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId()), eq(Boolean.FALSE.toString()), any(Date.class));
		verify(mockView).setVerificationButtonVisible(true);
	}
	
	private void setupVerificationState(VerificationStateEnum s, String reason) {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockCurrentUserBundle.getIsACTMember()).thenReturn(true);
		VerificationState state = new VerificationState();
		state.setState(s);
		state.setReason(reason);
		verificationStateList.add(state);
		//TODO: remove alpha mode website mock below after Validation has been exposed
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("true");
	}
	
	private void viewProfile(String targetUserId, String currentUserId) {
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		profilePresenter.updateProfileView(targetUserId);
	}
	
	@Test
	public void testVerificationUIInitRejected() {
		setupVerificationState(VerificationStateEnum.REJECTED, "bad behavior");
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		
		//not the owner of this profile, but is ACT
		viewProfile("123", "456");
		
		//user bundle reported that target user is not verified, should show badge
		verify(mockView, never()).showVerifiedBadge(null, null, null, null, null, null);
		verifyAndInvokeACTMember(true);
		verify(mockView).setVerificationRejectedButtonVisible(true);
		verify(mockView).setResubmitVerificationButtonVisible(false);
		//since this is ACT, should not see a way to submit a new validation request
		verify(mockView, never()).setVerificationButtonVisible(anyBoolean());
	}
	
	@Test
	public void testVerificationUIInitRejectedIsOwner() {
		setupVerificationState(VerificationStateEnum.REJECTED, "bad behavior");
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		//is the owner of the profile
		viewProfile("123", "123");
		verifyAndInvokeACTMember(false);
		verify(mockView).setVerificationRejectedButtonVisible(true);
		verify(mockView).setResubmitVerificationButtonVisible(true);
	}
	
	@Test
	public void testVerificationUIInitSuspendedIsOwner() {
		setupVerificationState(VerificationStateEnum.SUSPENDED, "missing documents");
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		//is the owner of the profile
		viewProfile("123", "123");
		verifyAndInvokeACTMember(false);
		verify(mockView).setVerificationSuspendedButtonVisible(true);
		verify(mockView).setResubmitVerificationButtonVisible(true);
	}
	
	
	@Test
	public void testVerificationUIInitSubmitted() {
		setupVerificationState(VerificationStateEnum.SUBMITTED, null);
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		
		//is not the owner of this profile, but is ACT
		//not the owner of this profile, but is ACT
		viewProfile("123", "456");
		
		//user bundle reported that target user is not verified
		verify(mockView, never()).showVerifiedBadge(null, null, null, null, null, null);
		verifyAndInvokeACTMember(true);
		verify(mockView).setVerificationSubmittedButtonVisible(true);
	}	
	
	@Test
	public void testVerificationUIInitApproved() {
		setupVerificationState(VerificationStateEnum.APPROVED, null);
		when(mockUserBundle.getIsVerified()).thenReturn(true);
		String fName = "Luke";
		String lName = "Skywalker";
		String company = "Rebel Alliance";
		String orcId = "http://orcid/address";
		String location= "Jundland Wastes, Tatooine";
		String friendlyDate= "October 2nd";
		when(mockVerificationSubmission.getFirstName()).thenReturn(fName);
		when(mockVerificationSubmission.getLastName()).thenReturn(lName);
		when(mockVerificationSubmission.getCompany()).thenReturn(company);
		when(mockVerificationSubmission.getOrcid()).thenReturn(orcId);
		when(mockVerificationSubmission.getLocation()).thenReturn(location);
		
		when(mockDateTimeUtils.getLongFriendlyDate(any(Date.class))).thenReturn(friendlyDate);
		//not the owner of this profile, but is ACT
		viewProfile("123", "456");
		
		//user bundle reported that target user is verified
		verify(mockView).showVerifiedBadge(fName, lName, location, company, orcId, friendlyDate);
		verifyAndInvokeACTMember(true);
		verify(mockView).setVerificationDetailsButtonVisible(true);
	}
	
	@Test
	public void testVerificationApprovedAsAnonymous() {
		setupVerificationState(VerificationStateEnum.APPROVED, null);
		when(mockUserBundle.getIsVerified()).thenReturn(true);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		viewProfile("123", null);

		verify(mockView).showVerifiedBadge(null, null, null, null, null, null);
		//validation details button is not visible to anonymous
		verify(mockView, never()).setVerificationDetailsButtonVisible(anyBoolean());
	}
	
	@Test
	public void testVerificationApprovedAsNonACT() {
		setupVerificationState(VerificationStateEnum.APPROVED, null);
		when(mockUserBundle.getIsVerified()).thenReturn(true);
		when(mockCurrentUserBundle.getIsACTMember()).thenReturn(false);
		viewProfile("123", "456");

		verify(mockView).showVerifiedBadge(null, null, null, null, null, null);
		//no need to check for act membership for anonymous
		verifyAndInvokeACTMember(false);
		//validation details button is not visible to a person who is not the owner and not part of the ACT
		verify(mockView, never()).setVerificationDetailsButtonVisible(anyBoolean());
	}
	
	@Test
	public void testShowEmailIfLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("123");
		profilePresenter.updateProfileView(userProfile.getOwnerId());
		verify(mockView).setSynapseEmailVisible(true);
	}
	@Test
	public void testHideEmailIfAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		profilePresenter.updateProfileView(userProfile.getOwnerId());
		verify(mockView).setSynapseEmailVisible(false);
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
		verify(mockPlaceChanger).goTo(isA(Home.class));
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
	@Test
	public void testNewVerificationSubmissionClicked() {
		//view my own profile.  submit a new verification submission, verify that modal is shown
		String currentUserId = "94837";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		when(mockVerificationSubmissionModal.configure(any(UserProfile.class), anyString(), anyBoolean(), anyList())).thenReturn(mockVerificationSubmissionModal);
		viewProfile(currentUserId, currentUserId);
		profilePresenter.newVerificationSubmissionClicked();
		verify(mockVerificationSubmissionModal).configure(any(UserProfile.class), anyString(), eq(true), eq(new ArrayList()));
		verify(mockVerificationSubmissionModal).show();
	}
	
	@Test
	public void testNewVerificationSubmissionClickedWithExistingAttachments() {
		//view my own profile.  submit a new verification submission, verify that modal is shown
		String currentUserId = "94837";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		AttachmentMetadata attachment = mock(AttachmentMetadata.class);
		List<AttachmentMetadata> attachmentList = Collections.singletonList(attachment);
		when(mockVerificationSubmission.getAttachments()).thenReturn(attachmentList);
		when(mockVerificationSubmissionModal.configure(any(UserProfile.class), anyString(), anyBoolean(), anyList())).thenReturn(mockVerificationSubmissionModal);
		viewProfile(currentUserId, currentUserId);
		profilePresenter.newVerificationSubmissionClicked();
		verify(mockVerificationSubmissionModal).configure(any(UserProfile.class), anyString(), eq(true), eq(attachmentList));
		verifyAndInvokeACTMember(false);
		verify(mockVerificationSubmissionModal).show();
	}
	
	@Test
	public void testEditVerificationSubmissionClicked() {
		//view my own profile.  submit a new verification submission, verify that modal is shown
		String currentUserId = "94837";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(currentUserId);
		viewProfile(currentUserId, currentUserId);
		profilePresenter.editVerificationSubmissionClicked();
		verifyAndInvokeACTMember(false);
		verify(mockVerificationSubmissionModal).configure(eq(mockVerificationSubmission), anyBoolean(), eq(true));
		verify(mockVerificationSubmissionModal).setResubmitCallback(any(Callback.class));
		verify(mockVerificationSubmissionModal).show();
	}

	@Test
	public void testUnbindOrcId() {
		viewProfile("123", "456");
		profilePresenter.unbindOrcIdAfterConfirmation();
		//success message and page refresh
		verify(mockView).showInfo(anyString());
		verify(mockGlobalApplicationState).refreshPage();
	}
	
	@Test
	public void testUnbindOrcIdFailure() {
		Exception ex = new Exception("bad things happened");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).unbindOAuthProvidersUserId(any(OAuthProvider.class), anyString(), any(AsyncCallback.class));
		viewProfile("123", "456");
		profilePresenter.unbindOrcIdAfterConfirmation();
		//error is shown
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testSetVerifyUndismissed() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		profilePresenter.setVerifyUndismissed();
		verify(mockCookies).removeCookie(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId());
	}
}