package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
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
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.presenter.SortOptionEnum;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.client.view.TeamRequestBundle;
import org.sagebionetworks.web.client.widget.WikiModalWidget;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidget;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.team.TeamListWidgetTest;
import org.sagebionetworks.web.unitserver.ChallengeClientImplTest;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public class ProfilePresenterTest {
	
	ProfilePresenter profilePresenter;
	ProfileView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	SynapseClientAsync mockSynapseClient;
	ChallengeClientAsync mockChallengeClient;
	LinkedInServiceAsync mockLinkedInServic;
	GWTWrapper mockGwt;
	PortalGinInjector mockInjector;
	
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	Profile place = Mockito.mock(Profile.class);
	CookieProvider mockCookies;
	UserProfileModalWidget mockUserProfileModalWidget;
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	SortOptionEnum sort = SortOptionEnum.LATEST_ACTIVITY;
	List<EntityHeader> myFavorites;
	List<Team> myTeams;
	List<TeamRequestBundle> myTeamBundles;
	ProjectPagedResults projects;
	List<ProjectHeader> myProjects;
	ChallengePagedResults testChallengePagedResults;
	List<ChallengeBundle> testChallenges;
	ProjectBadge mockProjectBadge;
	ChallengeBadge mockChallengeBadge;
	TeamListWidget mockTeamListWidget;
	SynapseAlert mockSynAlert;
	OpenTeamInvitationsWidget mockTeamInviteWidget;
	String targetUserId = "12345";
	String targetUsername = "jediknight";
	List<VerificationState> verificationStateList;
	
	@Mock
	UserProfileClientAsync mockUserProfileClient;
	@Mock
	UserBundle mockUserBundle;
	@Mock
	UserBundle mockCurrentUserBundle;
	@Mock
	VerificationSubmissionWidget mockVerificationSubmissionModal;
	@Mock
	VerificationSubmission mockVerificationSubmission;
	@Mock
	WikiModalWidget mockWikiModalWidget;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(ProfileView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockChallengeClient = mock(ChallengeClientAsync.class);
		mockCookies = mock(CookieProvider.class);
		mockLinkedInServic = mock(LinkedInServiceAsync.class);
		mockGwt = mock(GWTWrapper.class);
		mockInjector = mock(PortalGinInjector.class);
		mockUserProfileModalWidget = mock(UserProfileModalWidget.class);
		mockProjectBadge = mock(ProjectBadge.class);
		mockChallengeBadge = mock(ChallengeBadge.class);
		mockTeamListWidget = mock(TeamListWidget.class);
		mockTeamInviteWidget = mock(OpenTeamInvitationsWidget.class);
		mockSynAlert = mock(SynapseAlert.class);
		when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, 
				mockSynapseClient, adapterFactory, mockChallengeClient, mockCookies, mockUserProfileModalWidget, mockLinkedInServic, mockGwt, mockTeamListWidget, mockTeamInviteWidget, 
				mockInjector, mockUserProfileClient,mockVerificationSubmissionModal, mockWikiModalWidget);	
		verify(mockView).setPresenter(profilePresenter);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockInjector.getProjectBadgeWidget()).thenReturn(mockProjectBadge);
		when(mockInjector.getChallengeBadgeWidget()).thenReturn(mockChallengeBadge);
		userProfile.setDisplayName("tester");
		userProfile.setOwnerId("1");
		userProfile.setEmail("original.email@sagebase.org");
		testUser.setProfile(userProfile);
		testUser.setSession(new Session());
		testUser.getSession().setSessionToken("token");
		testUser.setIsSSO(false);
		
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockUserProfileClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockCurrentUserBundle).when(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserProfileClient).unbindOAuthProvidersUserId(any(OAuthProvider.class), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(targetUserId).when(mockSynapseClient).getUserIdFromUsername(eq(targetUsername), any(AsyncCallback.class));
		when(mockUserBundle.getUserProfile()).thenReturn(userProfile);
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl().createNew();
		testUser.writeToJSONObject(adapter);
		testUserJson = adapter.toJSONString(); 
		
		myTeams = TeamListWidgetTest.setupUserTeams(adapter, mockSynapseClient);
		myTeamBundles = new ArrayList<TeamRequestBundle>();
		for (int i = 0; i < myTeams.size(); i++) {
			myTeamBundles.add(new TeamRequestBundle(myTeams.get(i), Long.valueOf(i)));
		}
		//test bundle has two teams in it, with 1 open request
		AsyncMockStubber.callSuccessWith(myTeamBundles).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		
		//set up get user projects test
		EntityHeader project1 = new EntityHeader();
		project1.setId("syn1");
		EntityHeader project2 = new EntityHeader();
		project2.setId("syn2");
		
		myFavorites = new ArrayList<EntityHeader>();
		myFavorites.add(project1);
		myFavorites.add(project2);
		
		projects = new ProjectPagedResults();
		ProjectHeader projectHeader1 = new ProjectHeader();
		projectHeader1.setId("syn1");
		ProjectHeader projectHeader2 = new ProjectHeader();
		projectHeader2.setId("syn2");
		
		myProjects = new ArrayList<ProjectHeader>();
		myProjects.add(projectHeader1);
		myProjects.add(projectHeader2);
		projects.setResults(myProjects);
		projects.setTotalNumberOfResults(2);
		
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getMyProjects(any(ProjectListType.class), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getProjectsForTeam(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(myFavorites).when(mockSynapseClient).getFavorites(any(AsyncCallback.class));
		
		//set up create project test
		AsyncMockStubber.callSuccessWith("new entity id").when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		
		//set up create team test
		AsyncMockStubber.callSuccessWith("new team id").when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		
		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> testBatchResults = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		List<EntityHeader> testEvaluationResults = new ArrayList<EntityHeader>();
		EntityHeader testEvaluation = new EntityHeader();
		testEvaluation.setId("eval project id 1");
		testEvaluation.setName("My Test Evaluation Project");
		testEvaluationResults.add(testEvaluation);
		testBatchResults.setTotalNumberOfResults(1);
		testBatchResults.setResults(testEvaluationResults);
		ArrayList<EntityHeader> testBatchResultsList = new ArrayList<EntityHeader>();
		testBatchResultsList.addAll(testBatchResults.getResults());
		
		AsyncMockStubber.callSuccessWith(testBatchResultsList).when(mockSynapseClient).getEntityHeaderBatch(anyList(),any(AsyncCallback.class));
		when(mockGlobalApplicationState.isEditing()).thenReturn(false);
		setupTestChallengePagedResults();
		
		when(place.toToken()).thenReturn(targetUserId);
		when(mockUserBundle.getVerificationSubmission()).thenReturn(mockVerificationSubmission);
		verificationStateList = new ArrayList<VerificationState>();
		VerificationState oldState = new VerificationState();
		oldState.setState(VerificationStateEnum.SUSPENDED);
		oldState.setReason("numerous violations of the terms of use");
		verificationStateList.add(oldState);
		when(mockVerificationSubmission.getStateHistory()).thenReturn(verificationStateList);
		when(mockVerificationSubmissionModal.setResubmitCallback(any(Callback.class))).thenReturn(mockVerificationSubmissionModal);
		when(mockVerificationSubmissionModal.configure(any(VerificationSubmission.class), anyBoolean(), anyBoolean())).thenReturn(mockVerificationSubmissionModal);
	}
	
	public void setupTestChallengePagedResults() {
		testChallengePagedResults = new ChallengePagedResults();
		testChallenges = new ArrayList<ChallengeBundle>();
		ChallengeBundle bundle = new ChallengeBundle(ChallengeClientImplTest.getTestChallenge(), "my challenge project");
		testChallenges.add(bundle);
		testChallengePagedResults.setResults(testChallenges);
		testChallengePagedResults.setTotalNumberOfResults(1L);
		AsyncMockStubber.callSuccessWith(testChallengePagedResults).when(mockChallengeClient).getChallenges(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
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
		verify(mockSynapseClient).getFavorites(any(AsyncCallback.class));
	}
	
	@Test
	public void testStart() {
		verify(mockInjector, times(3)).getSynapseAlertWidget();
		profilePresenter.setPlace(place);
		verify(mockInjector, times(3)).getSynapseAlertWidget();
		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		
		profilePresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);
	}
	
	@Test
	public void testRedirectToLinkedIn() {
		profilePresenter.redirectToLinkedIn();
		verify(mockLinkedInServic).returnAuthUrl(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testUpdateProfileWithLinkedIn() {
		String secret = "secret";
		when(mockCookies.getCookie(CookieKeys.LINKEDIN)).thenReturn(secret);
		String requestToken = "token";
		String verifier = "12345";
		profilePresenter.updateProfileWithLinkedIn(requestToken, verifier);
		verify(mockLinkedInServic).getCurrentUserInfo(eq(requestToken), eq(secret), eq(verifier), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testPublicView() throws JSONObjectAdapterException{
		//view another user profile
		userProfile.setOwnerId(targetUserId);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		
		when(place.toToken()).thenReturn(targetUserId);
		when(place.getUserId()).thenReturn(targetUserId);
		profilePresenter.setPlace(place);
		verify(mockUserProfileClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).getTeamsForUser(captor.capture(), anyBoolean(), any(AsyncCallback.class));
		
		assertEquals(targetUserId, captor.getValue());
		verifyProfileShown(false);
		
		//not logged in, should not ask for this user favs
		verify(mockSynapseClient, never()).getFavorites(any(AsyncCallback.class));
	}
	
	@Test
	public void testViewUsername() {
		//view another user profile based on username
		userProfile.setOwnerId(targetUserId);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(place.toToken()).thenReturn(targetUsername);
		profilePresenter.setPlace(place);
		
		verify(mockSynapseClient).getUserIdFromUsername(eq(targetUsername), any(AsyncCallback.class));
		verify(mockUserProfileClient).getUserBundle(eq(Long.parseLong(targetUserId)), anyInt(), any(AsyncCallback.class));
		verify(place).setUserId(targetUserId);
	}
	
	@Test
	public void testViewUsernameFailure() {
		//view another user profile based on username
		userProfile.setOwnerId(targetUserId);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(place.toToken()).thenReturn(targetUsername);
		Exception ex = new Exception("an error");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient)
			.getUserIdFromUsername(anyString(), any(AsyncCallback.class));
		
		profilePresenter.setPlace(place);
		
		verify(mockSynapseClient).getUserIdFromUsername(eq(targetUsername), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}


	@Test
	public void testViewMyProfileNoRedirect() throws JSONObjectAdapterException{
		//view another user profile
		String myPrincipalId = "456";
		userProfile.setOwnerId(myPrincipalId);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(myPrincipalId);
		when(place.getUserId()).thenReturn(myPrincipalId);
		profilePresenter.setPlace(place);
		verify(mockUserProfileClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		
		//also verify that it is asking for the correct teams
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).getTeamsForUser(captor.capture(), anyBoolean(), any(AsyncCallback.class));
		assertEquals(myPrincipalId, captor.getValue());
		
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
		verify(mockUserProfileClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSettingsNotOwner() {
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("2");
		when(mockUserBundle.getIsCertified()).thenReturn(true);
		when(place.getUserId()).thenReturn("4");
		when(place.getArea()).thenReturn(ProfileArea.SETTINGS);
		profilePresenter.setPlace(place);
		verify(mockView).setTabSelected(eq(ProfileArea.PROJECTS));
		verify(mockView).addCertifiedBadge();
		verify(mockView, never()).setGetCertifiedVisible(anyBoolean());
	}		
	
	@Test
	public void testGetProfileError() {
		//some other error occurred
		Exception ex = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(ex).when(mockUserProfileClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
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
		verify(mockView, times(2)).clearProjects();
	}
	
	@Test
	public void testGetAllMyProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		//when setting the filter to all, it should ask for all of my projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).setAllProjectsFilterSelected();
		verify(mockView).showProjectFiltersUI();
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
		//should have refreshed teams too, since this is the owner
		verify(mockView).clearTeamNotificationCount();
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class), any(Long.class));
		verify(mockView).setTeamNotificationCount(String.valueOf(1));	
	}
	
	@Test
	public void testGetAllTheirProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(false);
		//when setting the filter to all, it should ask for all of their projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
		//should not have refreshed team invites, since this is not the owner
	}
	
	@Test
	public void testGetProjectsDefaultFilter() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(false);
		//when setting the filter to all, it should ask for all of their projects
		profilePresenter.setProjectFilterAndRefresh(null, null);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
	}


	
	@Test
	public void testGetProjectsCreatedByMe() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		//when setting the filter to my projects, it should query for projects created by me
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setMyProjectsFilterSelected();
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_CREATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
	}
	

	@Test
	public void testGetFavorites() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.FAVORITES, null);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setFavoritesFilterSelected();
		verify(mockSynapseClient).getFavorites(any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
		verify(mockView).setProjectSortVisible(false);
	}
	
	@Test
	public void testGetSharedDirectlyWithMeProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setSharedDirectlyWithMeFilterSelected();
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_PARTICIPATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testGetFavoritesEmpty() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		myFavorites.clear();
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.FAVORITES, null);
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).setFavoritesFilterSelected();
		verify(mockView).setFavoritesHelpPanelVisible(true);
		verify(mockSynapseClient).getFavorites(any(AsyncCallback.class));
		verify(mockView, never()).addProjectWidget(any(Widget.class));
		verify(mockView).setProjectSortVisible(false);
	}

	
	@Test
	public void testGetProjectsByTeam() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		String teamId = "39448";
		
		//when setting the filter to all, it should ask for all of my projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, teamId);
		verify(mockView, times(2)).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setTeamsFilterSelected();
		verify(mockSynapseClient).getProjectsForTeam(eq(teamId), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testGetProjectsByTeamFailure() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		AsyncMockStubber.callFailureWith(new Exception("failed")).when(mockSynapseClient).getProjectsForTeam(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, "123");
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testGetProjectCreatedByMeFailure() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("111");
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_CREATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),   any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testApplyFilterClickedAll() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL, null);
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMine() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.CREATED_BY_ME, null);
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_CREATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMyParticipatedProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME, null);
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_PARTICIPATED_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}
	
	@Test
	public void testApplyFilterClickedMyTeamProjects() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS, null);
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_TEAM_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(true);
	}

	
	@Test
	public void testApplyFilterClickedFavorites() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.FAVORITES, null);
		verify(mockSynapseClient).getFavorites(any(AsyncCallback.class));
		verify(mockView).setProjectSortVisible(false);
	}
	
	
	@Test
	public void testGetMoreOfTheirProjects() {
		profilePresenter.setIsOwner(false);
		//when asking for more projects, it should get their if I am not the owner
		profilePresenter.getMoreProjects();
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
	}
	
	
	@Test
	public void testGetMyProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		profilePresenter.getMyProjects(ProjectListType.MY_PROJECTS, ProjectFilterEnum.ALL, 0);
		verify(mockSynapseClient).getMyProjects(eq(ProjectListType.MY_PROJECTS), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testGetUserProjects() {
		profilePresenter.getUserProjects(1);
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
		verify(mockView, times(2)).addProjectWidget(any(Widget.class));
	}
	
	@Test
	public void testGetUserProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),   any(AsyncCallback.class));
		profilePresenter.getUserProjects(1);
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(ProjectListSortColumn.class), any(SortDirection.class),  any(AsyncCallback.class));
		verify(mockView).setProjectsError(anyString());
	}

	@Test
	public void testProjectPageAdded() {
		//add a page (where the full project list fits in a single page
		profilePresenter.setCurrentOffset(0);
		profilePresenter.projectPageAdded(ProfilePresenter.PROJECT_PAGE_SIZE - 10);
		assertEquals(ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockView).setIsMoreProjectsVisible(eq(false));
	}
	
	@Test
	public void testProjectPageAddedZeroResults() {
		profilePresenter.setCurrentOffset(0);
		profilePresenter.projectPageAdded(0);
		verify(mockView).setIsMoreProjectsVisible(eq(false));
	}
	
	@Test
	public void testProjectPageAddedWithMoreResults() {
		profilePresenter.setCurrentOffset(0);
		profilePresenter.projectPageAdded(ProfilePresenter.PROJECT_PAGE_SIZE + 10);
		assertEquals(ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockView).setIsMoreProjectsVisible(eq(true));
	}
	
	@Test
	public void testProjectPageTwoAddedWithMoreResults() {
		profilePresenter.setCurrentOffset(ProfilePresenter.PROJECT_PAGE_SIZE);
		profilePresenter.projectPageAdded(2*ProfilePresenter.PROJECT_PAGE_SIZE + 10);
		assertEquals(2*ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockView).setIsMoreProjectsVisible(eq(true));
	}

	
	@Test
	public void testCreateProject() {
		profilePresenter.createProject("valid name");
		verify(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		//inform user of success, and go to new project page
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}

	@Test
	public void testCreateProjectEmptyName() {
		profilePresenter.createProject("");
		verify(mockSynapseClient, Mockito.times(0)).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		Mockito.reset(mockView);
		
		profilePresenter.createProject(null);
		verify(mockSynapseClient, Mockito.times(0)).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testCreateProjectError() {
		Exception caught = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.createProject("valid name");
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testCreateProjectNameConflictError() {
		AsyncMockStubber.callFailureWith(new ConflictException("special handled exception type")).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.createProject("valid name");
		verify(mockView).showErrorMessage(eq(DisplayConstants.WARNING_PROJECT_NAME_EXISTS));
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
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(latestActivity.sortBy), eq(latestActivity.sortDir), any(AsyncCallback.class));
	}
	
	@Test
	public void testSortedByEarliestActivity() {
		SortOptionEnum earliestActivity = SortOptionEnum.EARLIEST_ACTIVITY;
		profilePresenter.resort(earliestActivity);
		verify(mockView).setSortText(eq(earliestActivity.sortText));
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(earliestActivity.sortBy), eq(earliestActivity.sortDir), any(AsyncCallback.class));
	}
	
	@Test
	public void testSortedByNameAZ() {
		SortOptionEnum nameAZ = SortOptionEnum.NAME_A_Z;
		profilePresenter.resort(nameAZ);
		verify(mockView).setSortText(eq(nameAZ.sortText));
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(nameAZ.sortBy), eq(nameAZ.sortDir), any(AsyncCallback.class));	
	}
	
	@Test
	public void testSortedByLastNameZA() {
		SortOptionEnum nameZA = SortOptionEnum.NAME_Z_A;
		profilePresenter.resort(nameZA);
		verify(mockView).setSortText(nameZA.sortText);
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), eq(nameZA.sortBy), eq(nameZA.sortDir), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateTeam() {
		profilePresenter.createTeam("valid name");
		verify(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		//inform user of success, and go to new team page
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockPlaceChanger).goTo(any(org.sagebionetworks.web.client.place.Team.class));
	}

	@Test
	public void testCreateTeamEmptyName() {
		profilePresenter.createTeam("");
		verify(mockSynapseClient, Mockito.times(0)).createTeam(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		Mockito.reset(mockView);	
		profilePresenter.createTeam(null);
		verify(mockSynapseClient, Mockito.times(0)).createTeam(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testCreateTeamError() {
		Exception caught = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		profilePresenter.createTeam("valid name");
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testCreateTeamNameConflictError() {
		AsyncMockStubber.callFailureWith(new ConflictException("special handled exception type")).when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		profilePresenter.createTeam("valid name");
		verify(mockView).showErrorMessage(eq(DisplayConstants.WARNING_TEAM_NAME_EXISTS));
	}
	
	
	//Challenge tests
	@Test
	public void testRefreshChallenges() {
		profilePresenter.setPlace(place);
		profilePresenter.tabClicked(ProfileArea.CHALLENGES);
		verify(mockView, times(2)).clearChallenges();
		assertEquals(ProfilePresenter.CHALLENGE_PAGE_SIZE, profilePresenter.getCurrentChallengeOffset());
		verify(mockView, times(2)).showChallengesLoading(anyBoolean());
		verify(mockView).addChallengeWidget(any(Widget.class));
	}
	
	public ArrayList<TeamRequestBundle> setupUserTeamBundles(SynapseClientAsync mockSynapseClient, long openRequestNumberPerTeam, int numTeams) {
		ArrayList<TeamRequestBundle> teamBundleList = new ArrayList<TeamRequestBundle>();
		for (int i = 0; i < numTeams; i++) {
			Team testTeam = new Team();
			testTeam.setId(String.valueOf(i));
			testTeam.setName("My Test Team " + i);
			teamBundleList.add(new TeamRequestBundle(testTeam, openRequestNumberPerTeam));
		}
		AsyncMockStubber.callSuccessWith(teamBundleList).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		return teamBundleList;
	}
	
	@Test
	public void testGetTeamBundlesNoRequests() throws Exception {
		AsyncMockStubber.callSuccessWith(setupUserTeamBundles(mockSynapseClient,3,1)).
		when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345",false);
		verify(mockSynapseClient).getTeamsForUser(eq("12345"), anyBoolean(), any(AsyncCallback.class));
		verify(mockTeamListWidget).addTeam(any(Team.class), any(Long.class));
		verify(mockView, Mockito.never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testGetTeamBundlesWithRequests() throws Exception {
		AsyncMockStubber.callSuccessWith(setupUserTeamBundles(mockSynapseClient,3,1)).
			when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345",true);
		verify(mockSynapseClient).getTeamsForUser(eq("12345"), anyBoolean(), any(AsyncCallback.class));
		verify(mockTeamListWidget).addTeam(any(Team.class), any(Long.class));
		verify(mockView).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testGetTeamBundlesFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345",true);
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).setTeamsError(ex.getMessage());
	}
	
	@Test
	public void testGetQueryForRequestCount() throws Exception {
		//when request count is null, should do nothing
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345", true);
		verify(mockView, times(0)).setTeamNotificationCount(anyString());
		verify(mockView, times(0)).showErrorMessage(anyString());

		//when request count is 0, should do nothing

		AsyncMockStubber.callSuccessWith(setupUserTeamBundles(mockSynapseClient, 0,1)).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345", true);
		verify(mockView, times(0)).setTeamNotificationCount(anyString());
		verify(mockView, times(0)).showErrorMessage(anyString());

		//when request count is >0, should set the request count in the view
		AsyncMockStubber.callSuccessWith(setupUserTeamBundles(mockSynapseClient, 5L, 1)).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345", true);
		verify(mockView).setTeamNotificationCount("5");
		verify(mockView, times(0)).showErrorMessage(anyString());
	}
	
	@Test
	public void testgetTeamsForUserFailure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).
				getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getTeamBundles("12345", true);
		verify(mockView).setTeamsError(ex.getMessage());
	}
	
	
	@Test
	public void testTeamsTabNotOwner() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(false);
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		verify(mockTeamListWidget).showLoading();
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView, Mockito.never()).setTeamNotificationCount(anyString());
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class), any(Long.class));
	}
	
	
	@Test
	public void testTeamsTabOwner() {
		profilePresenter.setPlace(place);
		profilePresenter.setIsOwner(true);
		profilePresenter.tabClicked(ProfileArea.TEAMS);	
		verify(mockTeamListWidget).showLoading();
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class), any(Long.class));
	}
	
	@Test
	public void testGetTeamsError() {
		profilePresenter.setPlace(place);
		String errorMessage = "error loading teams";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).setTeamsError(errorMessage);
	}
	
	@Test
	public void testGetTeamFilters() {
		profilePresenter.setPlace(place);
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).setTeamsFilterVisible(true);
		verify(mockTeamListWidget, times(2)).addTeam(any(Team.class), any(Long.class));
	}
	
	@Test
	public void testGetTeamFiltersEmpty() {
		profilePresenter.setPlace(place);
		AsyncMockStubber.callSuccessWith(new ArrayList()).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).setTeamsFilterVisible(false);
	}
	
	@Test
	public void testGetTeamFiltersError() {
		profilePresenter.setPlace(place);
		String errorMessage = "error loading teams";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		verify(mockSynapseClient).getTeamsForUser(anyString(), anyBoolean(), any(AsyncCallback.class));
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
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
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
		assertEquals(ProfileArea.PROJECTS, capturedPlace.getArea());
		assertEquals(testUserId, capturedPlace.getUserId());
	}
	@Test
	public void testViewMyProfileAsAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		//verify forces login if anonymous and trying to view own anonymous profile
		profilePresenter.viewMyProfile();
		
		verify(mockView).showErrorMessage(eq(DisplayConstants.ERROR_LOGIN_REQUIRED));
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}

	@Test
	public void testUpdateTeamInvites() {
		//reset team invite count
		profilePresenter.setInviteCount(0);
		int inviteCount = 3;
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}
		profilePresenter.updateTeamInvites(invites);
		
		assertEquals(inviteCount, profilePresenter.getInviteCount());
		verify(mockView).setTeamNotificationCount(eq(Integer.toString(inviteCount)));
	}
	
	@Test
	public void testAddMembershipRequests() {
		int beforeNotificationCount = 12; 
		profilePresenter.setOpenRequestCount(beforeNotificationCount);
		int expectedAfterNotificationCount = 1;
		profilePresenter.addMembershipRequests(expectedAfterNotificationCount);
		
		assertEquals(expectedAfterNotificationCount, profilePresenter.getOpenRequestCount());
		verify(mockView).setTeamNotificationCount(eq(Integer.toString(expectedAfterNotificationCount)));
	}
	
	@Test
	public void testUpdateTeamInvitesZero() {
		profilePresenter.setInviteCount(0);
		profilePresenter.updateTeamInvites(new ArrayList<OpenUserInvitationBundle>());
		
		assertEquals(0, profilePresenter.getInviteCount());
		verify(mockView, never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testAddMembershipRequestsZero() {
		profilePresenter.setOpenRequestCount(0);
		profilePresenter.addMembershipRequests(0);
		assertEquals(0, profilePresenter.getOpenRequestCount());
		verify(mockView, never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testRefreshTeamsOwnerTeamsAndInvitesAddingNotifications() {
		int totalNotifications = 12; // must be even for tests to pass
		int inviteCount = totalNotifications/2;
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}	
		setupUserTeamBundles(mockSynapseClient, totalNotifications/2, 1);
		profilePresenter.setIsOwner(true);
		profilePresenter.refreshTeams();
		ArgumentCaptor<CallbackP> updateTeamInvitesCallback = ArgumentCaptor.forClass(CallbackP.class);
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), updateTeamInvitesCallback.capture());
		updateTeamInvitesCallback.getValue().invoke(invites);
		//updates total notifications when finding team request updates and team invite updates
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications/2));
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockView).clearTeamNotificationCount();
		verify(mockView).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class), any(Long.class));
		//heretofore, have verified proper behavior without adding
		
		//doubling the notifications from invitations
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}
		//doubling number of teams
		setupUserTeamBundles(mockSynapseClient, totalNotifications/2, 2);
		profilePresenter.refreshTeams();
		verify(mockTeamInviteWidget, times(2)).configure(refreshTeamsCallback.capture(), updateTeamInvitesCallback.capture());
		updateTeamInvitesCallback.getValue().invoke(invites);
		//invites are still set
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications * 3 / 2));
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications * 2));
		verify(mockView, times(2)).clearTeamNotificationCount();
		//called twice more, one for each added team
		verify(mockView, times(3)).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget, times(3)).addTeam(any(Team.class), any(Long.class));
	}
	
	@Test
	public void testRefreshTeamsEmpty() {
		int totalNotifications = 0; // must be even for tests to pass
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		setupUserTeamBundles(mockSynapseClient, 0, 0);
		profilePresenter.setIsOwner(true);
		profilePresenter.refreshTeams();
		ArgumentCaptor<CallbackP> updateTeamInvitesCallback = ArgumentCaptor.forClass(CallbackP.class);
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), updateTeamInvitesCallback.capture());
		updateTeamInvitesCallback.getValue().invoke(invites);
		verify(mockView, never()).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockTeamListWidget).showEmpty();
	}
	
	@Test
	public void testRefreshTeamsOwnerOnlyTeams() {
		int totalNotifications = 12; // must be even for tests to pass
		int inviteCount = 0;
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}	
		setupUserTeamBundles(mockSynapseClient, totalNotifications, 1);
		profilePresenter.setIsOwner(true);
		profilePresenter.refreshTeams();
		ArgumentCaptor<CallbackP> updateTeamInvitesCallback = ArgumentCaptor.forClass(CallbackP.class);
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), updateTeamInvitesCallback.capture());
		updateTeamInvitesCallback.getValue().invoke(invites);
		//called by updateTeamInvites due to second switch, even if invites doesn't change
		verify(mockView, times(2)).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockView).clearTeamNotificationCount();
		verify(mockView).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class), any(Long.class));
	}
	
	@Test
	public void testRefreshTeamsOwnerOnlyInvites() {
		int totalNotifications = 12; // must be even for tests to pass
		int inviteCount = totalNotifications;
		List<OpenUserInvitationBundle> invites = new ArrayList<OpenUserInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new OpenUserInvitationBundle());	
		}	
		setupUserTeamBundles(mockSynapseClient, 0, 1);
		profilePresenter.setIsOwner(true);
		profilePresenter.refreshTeams();
		ArgumentCaptor<CallbackP> updateTeamInvitesCallback = ArgumentCaptor.forClass(CallbackP.class);
		ArgumentCaptor<Callback> refreshTeamsCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockTeamInviteWidget).configure(refreshTeamsCallback.capture(), updateTeamInvitesCallback.capture());
		updateTeamInvitesCallback.getValue().invoke(invites);
		verify(mockView).setTeamNotificationCount(String.valueOf(totalNotifications));
		verify(mockView).clearTeamNotificationCount();
		verify(mockView).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class), any(Long.class));
	}
	
	@Test
	public void testRefreshTeamsNotOwner() {
		profilePresenter.setIsOwner(false);
		setupUserTeamBundles(mockSynapseClient, 3, 1);
		profilePresenter.refreshTeams();
		verify(mockView, never()).setTeamNotificationCount(anyString());
		verify(mockTeamInviteWidget, never()).configure(any(Callback.class), any(CallbackP.class));
		verify(mockView).addTeamsFilterTeam(any(Team.class));
		verify(mockTeamListWidget).addTeam(any(Team.class), any(Long.class));
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
	public void testTabClickedWhileEditing(){
		profilePresenter.setPlace(place);
		when(mockGlobalApplicationState.isEditing()).thenReturn(true);
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		
		ArgumentCaptor<Callback> yesCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showConfirmDialog(anyString(), anyString(), yesCallback.capture());
		verify(mockView, never()).setTabSelected(any(ProfileArea.class));
		
		//click yes
		yesCallback.getValue().invoke();
		verify(mockView).setTabSelected(any(ProfileArea.class));
	}
	
	
	@Test
	public void testCertificationBadgeClicked() {
		profilePresenter.certificationBadgeClicked();
		verify(mockPlaceChanger).goTo(any(Certificate.class));
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
		verify(mockGlobalApplicationState).replaceCurrentPlace(any(Profile.class));
	}

	@Test
	public void testUpdateAreaNoChange() {
		profilePresenter.setPlace(place);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		boolean pushState = true;
		profilePresenter.updateArea(ProfileArea.PROJECTS, false);
		verify(mockPlaceChanger, never()).goTo(any(Profile.class));
	}
	
	private void verifyProfileHidden(boolean isCookieExpected) {
		verify(mockView).hideProfile();
		verify(mockView).setShowProfileButtonVisible(true);
		verify(mockView).setHideProfileButtonVisible(false);
		if (isCookieExpected)
			verify(mockCookies).setCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY), eq(Boolean.toString(false)), any(Date.class));
		else
			verify(mockCookies, never()).setCookie(anyString(), anyString(), any(Date.class));
	}
	private void verifyProfileShown(boolean isCookieExpected) {
		verify(mockView).showProfile();
		verify(mockView).setShowProfileButtonVisible(false);
		verify(mockView, Mockito.atLeastOnce()).setHideProfileButtonVisible(true);
		if (isCookieExpected)
			verify(mockCookies).setCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY), eq(Boolean.toString(true)), any(Date.class));
		else
			verify(mockCookies, never()).setCookie(anyString(), anyString(), any(Date.class));
	}
	
	@Test
	public void testInitShowHideProfileNotOwner() {
		profilePresenter.initializeShowHideProfile(false);
		verifyProfileShown(false);
		//and verify that hide profile button was hidden
		verify(mockView, Mockito.atLeastOnce()).setHideProfileButtonVisible(false);
	}
	
	@Test
	public void testInitShowHideProfileNullCookieValue() {
		//return null
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY))).thenReturn(null);
		profilePresenter.initializeShowHideProfile(true);
		verifyProfileShown(false);
	}
	
	@Test
	public void testInitShowHideProfileEmptyCookieValue() {
		//return null
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY))).thenReturn("");
		profilePresenter.initializeShowHideProfile(true);
		verifyProfileShown(false);
	}
	@Test
	public void testInitShowHideProfileTrueCookieValue() {
		//return null
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY))).thenReturn("true");
		profilePresenter.initializeShowHideProfile(true);
		verifyProfileShown(false);
	}
	@Test
	public void testInitShowHideProfileFalseCookieValue() {
		//return null
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY))).thenReturn("false");
		profilePresenter.initializeShowHideProfile(true);
		verifyProfileHidden(false);
	}
	
	@Test
	public void testHideProfileButtonClicked() {
		//return null
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY))).thenReturn("true");
		profilePresenter.hideProfileButtonClicked();
		verifyProfileHidden(true);
	}
	@Test
	public void testShowProfileButtonClicked() {
		//return null
		when(mockCookies.getCookie(eq(ProfilePresenter.USER_PROFILE_VISIBLE_STATE_KEY))).thenReturn("false");
		profilePresenter.showProfileButtonClicked();
		verifyProfileShown(true);
	}
	
	@Test
	public void testInitUserFavorites() {
		List<EntityHeader> favorites = new ArrayList<EntityHeader>();
		AsyncMockStubber.callSuccessWith(favorites).when(mockSynapseClient).getFavorites(any(AsyncCallback.class));
		Callback mockCallback = mock(Callback.class);
		profilePresenter.initUserFavorites(mockCallback);
		verify(mockGlobalApplicationState).setFavorites(favorites);
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testInitUserFavoritesFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getFavorites(any(AsyncCallback.class));
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
		verify(mockUserProfileClient).getMyOwnUserBundle(eq(ProfilePresenter.IS_ACT_MEMBER), any(AsyncCallback.class));
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
		verify(mockView).setVerificationRejectedButtonVisible(true);
		verify(mockView).setResubmitVerificationButtonVisible(true);
	}
	
	@Test
	public void testVerificationUIInitSuspendedIsOwner() {
		setupVerificationState(VerificationStateEnum.SUSPENDED, "missing documents");
		when(mockUserBundle.getIsVerified()).thenReturn(false);
		//is the owner of the profile
		viewProfile("123", "123");
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
		verify(mockUserProfileClient).getMyOwnUserBundle(eq(ProfilePresenter.IS_ACT_MEMBER), any(AsyncCallback.class));
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
		
		when(mockGwt.getFormattedDateString(any(Date.class))).thenReturn(friendlyDate);
		//not the owner of this profile, but is ACT
		viewProfile("123", "456");
		
		//user bundle reported that target user is verified
		verify(mockView).showVerifiedBadge(fName, lName, location, company, orcId, friendlyDate);
		verify(mockUserProfileClient).getMyOwnUserBundle(eq(ProfilePresenter.IS_ACT_MEMBER), any(AsyncCallback.class));
		verify(mockView).setVerificationDetailsButtonVisible(true);
	}
	
	@Test
	public void testVerificationApprovedAsAnonymous() {
		setupVerificationState(VerificationStateEnum.APPROVED, null);
		when(mockUserBundle.getIsVerified()).thenReturn(true);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		viewProfile("123", null);

		verify(mockView).showVerifiedBadge(null, null, null, null, null, null);
		//no need to check for act membership for anonymous
		verify(mockUserProfileClient, never()).getMyOwnUserBundle(eq(ProfilePresenter.IS_ACT_MEMBER), any(AsyncCallback.class));
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
		verify(mockUserProfileClient).getMyOwnUserBundle(eq(ProfilePresenter.IS_ACT_MEMBER), any(AsyncCallback.class));
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
		verify(mockView).showInfo("", DisplayConstants.SUCCESSFULLY_LINKED_OAUTH2_ACCOUNT);
	}

	@Test
	public void testVTokenAnonymous() {
		//go home if trying to access Profile:v while anonymous
		String token = "v";
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(place.toToken()).thenReturn(token);
		profilePresenter.setPlace(place);
		verify(mockPlaceChanger).goTo(any(Home.class));
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
		//default area, projects
		assertEquals(ProfileArea.PROJECTS, capturedPlace.getArea());
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
		verify(mockVerificationSubmissionModal).configure(eq(mockVerificationSubmission), anyBoolean(), eq(true));
		verify(mockVerificationSubmissionModal).setResubmitCallback(any(Callback.class));
		verify(mockVerificationSubmissionModal).show();
	}

	@Test
	public void testUnbindOrcId() {
		viewProfile("123", "456");
		profilePresenter.unbindOrcIdAfterConfirmation();
		//success message and page refresh
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockGlobalApplicationState).refreshPage();
	}
	
	@Test
	public void testUnbindOrcIdFailure() {
		Exception ex = new Exception("bad things happened");
		AsyncMockStubber.callFailureWith(ex).when(mockUserProfileClient).unbindOAuthProvidersUserId(any(OAuthProvider.class), anyString(), any(AsyncCallback.class));
		viewProfile("123", "456");
		profilePresenter.unbindOrcIdAfterConfirmation();
		//error is shown
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testonVerifyMoreInfoClicked() {
		profilePresenter.onVerifyMoreInfoClicked();
		verify(mockWikiModalWidget).show(anyString());
	}
	
	@Test
	public void testSetVerifyUndismissed() {
		profilePresenter.setCurrentUserId(userProfile.getOwnerId());
		profilePresenter.setVerifyUndismissed();
		verify(mockCookies).removeCookie(ProfilePresenter.USER_PROFILE_VERIFICATION_VISIBLE_STATE_KEY + "." + userProfile.getOwnerId());
	}
}