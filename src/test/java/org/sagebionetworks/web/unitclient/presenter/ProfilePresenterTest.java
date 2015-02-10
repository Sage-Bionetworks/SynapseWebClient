package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
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
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Certificate;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
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
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	RequestBuilderWrapper mockRequestBuilder;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	CookieProvider mockCookies;
	Profile place = Mockito.mock(Profile.class);
	
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	List<String> myFavoritesJson;
	List<EntityHeader> myFavorites;
	List<Team> myTeams;
	ProjectPagedResults projects;
	List<ProjectHeader> myProjects;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
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
				mockSynapseClient, mockCookies, mockGWTWrapper, adapter, mockProfileForm, adapterFactory, 
				mockSynapseJSNIUtils, mockRequestBuilder);	
		verify(mockView).setPresenter(profilePresenter);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
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
		
		myFavoritesJson = new ArrayList<String>();
		myFavoritesJson.add(project1.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		myFavoritesJson.add(project2.writeToJSONObject(adapterFactory.createNew()).toJSONString());
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
		
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getMyProjects(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(projects).when(mockSynapseClient).getProjectsForTeam(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(myFavoritesJson).when(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		
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
		ArrayList<EntityHeader> testBatchResultsList = new ArrayList<EntityHeader>();
		testBatchResultsList.addAll(testBatchResults.getResults());
		
		AsyncMockStubber.callSuccessWith(testBatchResultsList).when(mockSynapseClient).getEntityHeaderBatch(anyList(),any(AsyncCallback.class));
		when(mockGlobalApplicationState.isEditing()).thenReturn(false);
	}
	
	private void setupGetUserProfile() throws JSONObjectAdapterException {
		AsyncMockStubber.callSuccessWith(userProfile).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
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
		
		//by default, it should load ALL projects for the current user
		assertEquals(ProjectFilterEnum.ALL, profilePresenter.getFilterType());
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
	public void testRefreshProjects() {
		profilePresenter.setCurrentOffset(22);
		//on refresh, current project offset should be reset to 0
		profilePresenter.refreshProjects();
		assertEquals(ProfilePresenter.PROJECT_PAGE_SIZE, profilePresenter.getCurrentOffset());
		verify(mockView).clearProjects();
	}
	
	@Test
	public void testGetAllMyProjects() {
		profilePresenter.setIsOwner(true);
		//when setting the filter to all, it should ask for all of my projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.ALL, null);
		verify(mockView).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).setAllProjectsFilterSelected();
		verify(mockView).showProjectFiltersUI();
		verify(mockSynapseClient).getMyProjects(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).addProjects(eq(myProjects));
	}
	
	@Test
	public void testGetProjectsCreatedByMe() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		
		EntityQueryResults results = new EntityQueryResults();
		results.setTotalEntityCount(222L);
		List<EntityQueryResult> resultList = new ArrayList<EntityQueryResult>();
		results.setEntities(resultList);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		
		//when setting the filter to my projects, it should query for projects created by me
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.MINE, null);
		verify(mockView).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setMyProjectsFilterSelected();
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		verify(mockView).addProjects(anyList());
	}
	

	@Test
	public void testGetFavorites() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.FAVORITES, null);
		verify(mockView).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setFavoritesFilterSelected();
		verify(mockView).setFavoritesHelpPanelVisible(false);
		verify(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).addProjects(anyList());
	}
	
	@Test
	public void testGetFavoritesEmpty() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("125");
		myFavorites.clear();
		myFavoritesJson.clear();
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.FAVORITES, null);
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).setFavoritesFilterSelected();
		verify(mockView, Mockito.times(2)).setFavoritesHelpPanelVisible(anyBoolean());
		verify(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView, never()).addProjects(anyList());
	}

	
	@Test
	public void testGetProjectsByTeam() {
		profilePresenter.setIsOwner(true);
		Team testTeam = new Team();
		String teamId = "39448";
		testTeam.setId(teamId);
		
		//when setting the filter to all, it should ask for all of my projects
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, testTeam);
		verify(mockView).clearProjects();
		verify(mockView, Mockito.times(2)).showProjectsLoading(anyBoolean());
		verify(mockView).showProjectFiltersUI();
		verify(mockView).setTeamsFilterSelected();
		verify(mockSynapseClient).getProjectsForTeam(eq(teamId), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).addProjects(eq(myProjects));
	}
	
	@Test
	public void testGetProjectsByTeamFailure() {
		profilePresenter.setIsOwner(true);
		AsyncMockStubber.callFailureWith(new Exception("failed")).when(mockSynapseClient).getProjectsForTeam(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.TEAM, new Team());
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testGetProjectCreatedByMeFailure() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("111");
		AsyncMockStubber.callFailureWith(new Exception("failed")).when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		profilePresenter.setProjectFilterAndRefresh(ProjectFilterEnum.MINE, null);
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testApplyFilterClickedAll() {
		profilePresenter.setIsOwner(true);
		profilePresenter.applyFilterClicked(ProjectFilterEnum.ALL, null);
		verify(mockSynapseClient).getMyProjects(anyInt(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testApplyFilterClickedMine() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.MINE, null);
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testApplyFilterClickedFavorites() {
		profilePresenter.setIsOwner(true);
		profilePresenter.setCurrentUserId("007");
		profilePresenter.applyFilterClicked(ProjectFilterEnum.FAVORITES, null);
		verify(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateProjectQuery(){
		String creatorId = "5575";
		Long limit = 5L;
		Long offset = 22L;
		EntityQuery query = profilePresenter.createGetProjectsQuery(creatorId, limit, offset);
		assertNotNull(query);
		assertNotNull(query.getConditions());
		assertEquals(1, (query.getConditions().size()));
		Condition expectedCondition = EntityQueryUtils.buildCondition(EntityFieldName.createdByPrincipalId, Operator.EQUALS, creatorId);
		assertEquals(expectedCondition, query.getConditions().get(0));
		assertEquals(EntityType.project, query.getFilterByType());
		assertEquals(offset, query.getOffset());
		assertEquals(limit, query.getLimit());
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		assertEquals(sort, query.getSort());
	}
	
	@Test
	public void testGetHeadersFromQueryResults() {
		EntityQueryResults testResults = new EntityQueryResults();
		EntityQueryResult result1 = new EntityQueryResult();
		String id1 = "38383";
		String name1 = "hello project";
		result1.setId(id1);
		result1.setName(name1);
		List<EntityQueryResult> resultList = new ArrayList<EntityQueryResult>();
		resultList.add(result1);
		testResults.setEntities(resultList);
		List<ProjectHeader> projectHeaders = profilePresenter.getHeadersFromQueryResults(testResults);
		assertEquals(1, projectHeaders.size());
		ProjectHeader header1 = projectHeaders.get(0);
		assertEquals(id1, header1.getId());
		assertEquals(name1, header1.getName());
	}
	
	@Test
	public void testGetMoreOfTheirProjects() {
		profilePresenter.setIsOwner(false);
		//when asking for more projects, it should get their if I am not the owner
		profilePresenter.getMoreProjects();
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}
	
	
	@Test
	public void testGetMyProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getMyProjects(anyInt(), anyInt(),  any(AsyncCallback.class));
		profilePresenter.getAllMyProjects(1);
		verify(mockSynapseClient).getMyProjects(anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setProjectsError(anyString());
	}
	
	@Test
	public void testGetUserProjects() {
		profilePresenter.getUserProjects(1);
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).addProjects(eq(myProjects));
	}
	
	@Test
	public void testGetUserProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(),  any(AsyncCallback.class));
		profilePresenter.getUserProjects(1);
		verify(mockSynapseClient).getUserProjects(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
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
		verify(mockView).showErrorMessage(anyString());
		verify(mockView, never()).setTabSelected(any(ProfileArea.class));
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
	
	
	@Test
	public void testCertificationBadgeClicked() {
		profilePresenter.certificationBadgeClicked();
		verify(mockPlaceChanger).goTo(any(Certificate.class));
	}
	
	@Test
	public void testUpdateArea() {
		profilePresenter.setPlace(place);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		profilePresenter.updateArea(ProfileArea.CHALLENGES);
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}

	@Test
	public void testUpdateAreaNoChange() {
		profilePresenter.setPlace(place);
		when(place.getArea()).thenReturn(ProfileArea.PROJECTS);
		profilePresenter.updateArea(ProfileArea.PROJECTS);
		verify(mockPlaceChanger, never()).goTo(any(Profile.class));
	}
	
}