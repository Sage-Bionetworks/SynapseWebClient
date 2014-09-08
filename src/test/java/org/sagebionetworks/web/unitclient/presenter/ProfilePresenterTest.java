package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.team.TeamListWidgetTest;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public class ProfilePresenterTest {
	
	ProfilePresenter profilePresenter;
	ProfileView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	ProfileFormWidget mockProfileForm;
	PlaceChanger mockPlaceChanger;	
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	GWTWrapper mockGWTWrapper;
	SearchServiceAsync mockSearchService;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	RequestBuilderWrapper mockRequestBuilder;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	CookieProvider mockCookies;
	Profile place = Mockito.mock(Profile.class);
	
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	List<String> myProjectsJson;
	List<EntityHeader> myProjects;
	List<Team> myTeams;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockSearchService = mock(SearchServiceAsync.class);
		mockView = mock(ProfileView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		mockProfileForm = mock(ProfileFormWidget.class);
		mockRequestBuilder = mock(RequestBuilderWrapper.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockCookies = mock(CookieProvider.class);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, 
				mockSynapseClient, mockCookies, mockGWTWrapper, adapter, mockProfileForm, adapterFactory, mockSearchService, 
				mockSynapseJSNIUtils, mockRequestBuilder);	
		verify(mockView).setPresenter(profilePresenter);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		userProfile.setDisplayName("tester");
		userProfile.setOwnerId("1");
		userProfile.setEmail("original.email@sagebase.org");
		testUser.setProfile(userProfile);
		testUser.setSession(new Session());
		testUser.getSession().setSessionToken("token");
		testUser.setIsSSO(false);
		
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl().createNew();
		testUser.writeToJSONObject(adapter);
		testUserJson = adapter.toJSONString(); 
		
		myTeams = TeamListWidgetTest.setupUserTeams(adapter, mockSynapseClient);
		setupGetUserProfile();
		
		PassingRecord myPassingRecord = new PassingRecord();
		String passingRecordJson = myPassingRecord.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		AsyncMockStubber.callSuccessWith(passingRecordJson).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		
		//set up get user projects test
		EntityHeader project1 = new EntityHeader();
		project1.setId("syn1");
		EntityHeader project2 = new EntityHeader();
		project2.setId("syn2");
		
		myProjectsJson = new ArrayList<String>();
		myProjectsJson.add(project1.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		myProjectsJson.add(project2.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		myProjects = new ArrayList<EntityHeader>();
		myProjects.add(project1);
		myProjects.add(project2);
		AsyncMockStubber.callSuccessWith(myProjectsJson).when(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(myProjectsJson).when(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		
		//set up create project test
		AsyncMockStubber.callSuccessWith("new entity id").when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		//set up create team test
		AsyncMockStubber.callSuccessWith("new team id").when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		
		BatchResults<EntityHeader> testBatchResults = new BatchResults<EntityHeader>();
		List<EntityHeader> testEvaluationResults = new ArrayList<EntityHeader>();
		EntityHeader testEvaluation = new EntityHeader();
		testEvaluation.setId("eval project id 1");
		testEvaluation.setName("My Test Evaluation Project");
		testEvaluationResults.add(testEvaluation);
		testBatchResults.setTotalNumberOfResults(1);
		testBatchResults.setResults(testEvaluationResults);
		ArrayList<String> testBatchResultsList = new ArrayList<String>();
		for(EntityHeader eh : testBatchResults.getResults()) {
			testBatchResultsList.add(eh.writeToJSONObject(adapter.createNew()).toJSONString());
		}
		
		AsyncMockStubber.callSuccessWith(testBatchResultsList).when(mockSynapseClient).getEntityHeaderBatch(anyList(),any(AsyncCallback.class));
		when(mockGlobalApplicationState.isEditing()).thenReturn(false);
	}
	
	private void setupGetUserProfile() throws JSONObjectAdapterException {
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl().createNew();
		userProfile.writeToJSONObject(adapter);
		String userProfileJson = adapter.toJSONString(); 

		AsyncMockStubber.callSuccessWith(userProfileJson).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
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
	public void testUpdateProfileWithLinkedIn() {
		profilePresenter.setPlace(place);
		when(mockCookies.getCookie(CookieKeys.LINKEDIN)).thenReturn("secret");
		String requestToken = "token";
		String verifier = "12345";
		profilePresenter.updateProfileWithLinkedIn(requestToken, verifier);
		//pass-through
		verify(mockProfileForm).updateProfileWithLinkedIn(eq(requestToken), eq(verifier));
	}
	
	@Test
	public void testPublicView() throws JSONObjectAdapterException{
		//view another user profile
		String targetUserId = "12345";
		userProfile.setOwnerId(targetUserId);
		setupGetUserProfile();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		
		when(place.toToken()).thenReturn(targetUserId);
		when(place.getUserId()).thenReturn(targetUserId);
		profilePresenter.setPlace(place);
		verify(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		
		//also verify that it is asking for the correct teams
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).getTeamsForUser(captor.capture(),  any(AsyncCallback.class));
		
		assertEquals(targetUserId, captor.getValue());
	}

	@Test
	public void testViewMyProfileNoRedirect() throws JSONObjectAdapterException{
		//view another user profile
		String myPrincipalId = "456";
		userProfile.setOwnerId(myPrincipalId);
		setupGetUserProfile();
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(myPrincipalId);
		when(place.toToken()).thenReturn(myPrincipalId);
		profilePresenter.setPlace(place);
		verify(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		
		//also verify that it is asking for the correct teams
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).getTeamsForUser(captor.capture(),  any(AsyncCallback.class));
		assertEquals(myPrincipalId, captor.getValue());
	} 
	

	@Test
	public void testEditMyProfileNoRedirect() {
		//view another user profile
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("1");
		when(place.toToken()).thenReturn("2");
		profilePresenter.setPlace(place);
		verify(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testGetIsCertifiedAndUpdateView() throws JSONObjectAdapterException {
		profilePresenter.getIsCertifiedAndUpdateView(userProfile, true, ProfileArea.SETTINGS);
		verify(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		verify(mockView).updateView(any(UserProfile.class), anyBoolean(), any(PassingRecord.class), any(Widget.class));
		verify(mockView).setTabSelected(eq(ProfileArea.SETTINGS));
	}
	
	@Test
	public void testGetIsCertifiedAndUpdateViewQuizNotTaken() throws JSONObjectAdapterException {
		//have not taken the test
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));

		profilePresenter.getIsCertifiedAndUpdateView(userProfile, false, ProfileArea.TEAMS);
		verify(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		
		verify(mockView).updateView(any(UserProfile.class), anyBoolean(), eq((PassingRecord)null), any(Widget.class));
		verify(mockView).setTabSelected(eq(ProfileArea.TEAMS));
	}
	
	@Test
	public void testGetIsCertifiedAndUpdateViewError() throws JSONObjectAdapterException {
		//some other error occurred
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
	
		profilePresenter.getIsCertifiedAndUpdateView(userProfile, false, ProfileArea.PROJECTS);
		verify(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testGetUserProjects() {
		profilePresenter.getUserProjects("anyUserId");
		verify(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).setProjects(eq(myProjects));
	}
	
	@Test
	public void testGetUserProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getUserProjects("anyUserId");
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testCreateProject() {
		profilePresenter.createProject("valid name");
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		//inform user of success, and go to new project page
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockPlaceChanger).goTo(any(Synapse.class));
	}

	@Test
	public void testCreateProjectEmptyName() {
		profilePresenter.createProject("");
		verify(mockSynapseClient, Mockito.times(0)).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		Mockito.reset(mockView);
		
		profilePresenter.createProject(null);
		verify(mockSynapseClient, Mockito.times(0)).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testCreateProjectError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.createProject("valid name");
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testCreateProjectNameConflictError() {
		AsyncMockStubber.callFailureWith(new ConflictException("special handled exception type")).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.createProject("valid name");
		verify(mockView).showErrorMessage(eq(DisplayConstants.WARNING_PROJECT_NAME_EXISTS));
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
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		profilePresenter.createTeam("valid name");
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testCreateTeamNameConflictError() {
		AsyncMockStubber.callFailureWith(new ConflictException("special handled exception type")).when(mockSynapseClient).createTeam(anyString(), any(AsyncCallback.class));
		profilePresenter.createTeam("valid name");
		verify(mockView).showErrorMessage(eq(DisplayConstants.WARNING_TEAM_NAME_EXISTS));
	}

	
	//Challenge tests
	@Test
	public void testGetChallengeProjectHeaders() {
		profilePresenter.getChallengeProjectHeaders(new HashSet<String>());
		verify(mockView).setChallenges(anyList());
	}
	
	@Test
	public void testGetChallengeProjectHeadersFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getEntityHeaderBatch(anyList(),any(AsyncCallback.class));
		profilePresenter.getChallengeProjectHeaders(new HashSet<String>());
		verify(mockView).setChallengesError(anyString());
	}
	
	@Test
	public void testTeam2ChallengeEndToEnd() throws RequestException {
		Team t1 = new Team();
		t1.setId("2");
		List<Team> myTeams = new ArrayList<Team>();
		myTeams.add(t1);
		profilePresenter.getChallengeProjectIds(myTeams);
		//grab the request callback and invoke
		ArgumentCaptor<RequestCallback> arg = ArgumentCaptor.forClass(RequestCallback.class);
		verify(mockRequestBuilder).sendRequest(anyString(), arg.capture());
		RequestCallback callback = arg.getValue();
		Response testResponse = new Response() {
			@Override
			public String getText() {
				return "{\"1\":\"syn1\", \"2\" : \"syn2\"}";
			}
			
			@Override
			public String getStatusText() {
				return null;
			}
			
			@Override
			public int getStatusCode() {
				return 0;
			}
			
			@Override
			public String getHeadersAsString() {
				return null;
			}
			
			@Override
			public Header[] getHeaders() {
				return null;
			}
			
			@Override
			public String getHeader(String header) {
				return null;
			}
		};
		callback.onResponseReceived(null, testResponse);
		ArgumentCaptor<List> entityList = ArgumentCaptor.forClass(List.class);
		verify(mockRequestBuilder).sendRequest(anyString(), arg.capture());
		verify(mockView).setChallenges(entityList.capture());
		List<EntityHeader> capturedEntityList = entityList.getValue();
		assertEquals(1, capturedEntityList.size());
	}
	
	@Test
	public void testTeam2ChallengeProjectFileCache() {
		CallbackP callback = new CallbackP() {
			@Override
			public void invoke(Object param) {
			}
		};
		when(mockCookies.getCookie(eq(HomePresenter.TEAMS_2_CHALLENGE_ENTITIES_COOKIE))).thenReturn("{\"1\":\"syn1\", \"2\" : \"syn2\"}");
		profilePresenter.getTeamId2ChallengeIdWhitelist(callback);
		verify(mockRequestBuilder, times(0)).configure(any(RequestBuilder.Method.class), anyString());
		
		//but without the cookie, it should be called
		when(mockCookies.getCookie(eq(HomePresenter.TEAMS_2_CHALLENGE_ENTITIES_COOKIE))).thenReturn(null);
		profilePresenter.getTeamId2ChallengeIdWhitelist(callback);
		verify(mockRequestBuilder, times(1)).configure(any(RequestBuilder.Method.class), anyString());
	}
	
	@Test
	public void testGetTeams() {
		profilePresenter.getTeamsAndChallenges("anyUserId");
		verify(mockSynapseClient).getTeamsForUser(anyString(),  any(AsyncCallback.class));
		verify(mockView).setTeams(eq(myTeams), anyBoolean());
	}
	
	@Test
	public void testGetTeamsError() {
		String errorMessage = "error loading teams";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getTeamsForUser(anyString(), any(AsyncCallback.class));
		profilePresenter.getTeamsAndChallenges("anyUserId");
		verify(mockSynapseClient).getTeamsForUser(anyString(),  any(AsyncCallback.class));
		verify(mockView).setTeamsError(errorMessage);
	}
	
	@Test
	public void testGetFavorites() {
		profilePresenter.getFavorites();
		verify(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setFavorites(eq(myProjects));
	}
	
	@Test
	public void testGetFavoritesError() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		profilePresenter.getFavorites();
		verify(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setFavoritesError(anyString());
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
		assertNull(capturedPlace.getArea());
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
		//reset team notification count
		profilePresenter.setTeamNotificationCount(0);
		int inviteCount = 3;
		List<MembershipInvitationBundle> invites = new ArrayList<MembershipInvitationBundle>();
		for (int i = 0; i < inviteCount; i++) {
			invites.add(new MembershipInvitationBundle());	
		}
		profilePresenter.updateTeamInvites(invites);
		
		assertEquals(inviteCount, profilePresenter.getTeamNotificationCount());
		verify(mockView).setTeamNotificationCount(eq(Integer.toString(inviteCount)));
	}
	
	@Test
	public void testAddMembershipRequests() {
		int beforeNotificationCount = 12; 
		profilePresenter.setTeamNotificationCount(beforeNotificationCount);
		
		profilePresenter.addMembershipRequests(1);
		
		int expectedAfterNotificationCount = beforeNotificationCount+1;
		assertEquals(expectedAfterNotificationCount, profilePresenter.getTeamNotificationCount());
		verify(mockView).setTeamNotificationCount(eq(Integer.toString(expectedAfterNotificationCount)));
	}
	
	@Test
	public void testUpdateTeamInvitesZero() {
		profilePresenter.setTeamNotificationCount(0);
		profilePresenter.updateTeamInvites(new ArrayList<MembershipInvitationBundle>());
		
		assertEquals(0, profilePresenter.getTeamNotificationCount());
		verify(mockView, never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testAddMembershipRequestsZero() {
		profilePresenter.setTeamNotificationCount(0);
		profilePresenter.addMembershipRequests(0);
		assertEquals(0, profilePresenter.getTeamNotificationCount());
		verify(mockView, never()).setTeamNotificationCount(anyString());
	}
	
	@Test
	public void testRefreshTeams() {
		profilePresenter.setTeamNotificationCount(10);
		profilePresenter.setIsOwner(true);
		profilePresenter.refreshTeams();
		verify(mockView).refreshTeamInvites();
		assertEquals(0, profilePresenter.getTeamNotificationCount());
		verify(mockView).clearTeamNotificationCount();
	}
	
	@Test
	public void testRefreshTeamsNotOwner() {
		profilePresenter.setIsOwner(false);
		profilePresenter.refreshTeams();
		verify(mockView, never()).refreshTeamInvites();
	}

	
	@Test
	public void testTabClickedDefault(){
		profilePresenter.tabClicked(null);
		verify(mockView).setTabSelected(eq(ProfileArea.PROJECTS));
	}
	
	@Test
	public void testTabClickedTeams(){
		profilePresenter.tabClicked(ProfileArea.TEAMS);
		verify(mockView).setTabSelected(eq(ProfileArea.TEAMS));
	}
	
	@Test
	public void testTabClickedWhileEditing(){
		when(mockGlobalApplicationState.isEditing()).thenReturn(true);
		profilePresenter.tabClicked(ProfileArea.PROJECTS);
		
		ArgumentCaptor<Callback> yesCallback = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showConfirmDialog(anyString(), anyString(), yesCallback.capture());
		verify(mockView, never()).setTabSelected(any(ProfileArea.class));
		
		//click yes
		yesCallback.getValue().invoke();
		verify(mockProfileForm).rollback();
		verify(mockView).setTabSelected(any(ProfileArea.class));
	}
	
}