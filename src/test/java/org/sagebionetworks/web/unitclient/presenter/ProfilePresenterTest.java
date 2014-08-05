package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.ProfileFormWidget;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ProfileView;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.team.TeamListWidgetTest;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public class ProfilePresenterTest {
	
	ProfilePresenter profilePresenter;
	ProfileView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	LinkedInServiceAsync mockLinkedInService;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	ProfileFormWidget mockProfileForm;
	PlaceChanger mockPlaceChanger;	
	CookieProvider mockCookieProvider;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	GWTWrapper mockGWTWrapper;
	SearchServiceAsync mockSearchService;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	
	Profile place = Mockito.mock(Profile.class);
	
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	List<String> myProjectsJson;
	List<EntityHeader> myProjects;
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockSearchService = mock(SearchServiceAsync.class);
		mockView = mock(ProfileView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockLinkedInService = mock(LinkedInServiceAsync.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		mockProfileForm = mock(ProfileFormWidget.class);
		
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockLinkedInService, mockGlobalApplicationState, mockSynapseClient, mockCookieProvider, mockGWTWrapper, adapter, mockProfileForm, adapterFactory, mockSearchService);	
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
		
		TeamListWidgetTest.setupUserTeams(adapter, mockSynapseClient);
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
	public void testRedirectToLinkedIn() {
		profilePresenter.setPlace(place);
		profilePresenter.redirectToLinkedIn();
	}
	
	@Test
	public void testUpdateProfileWithLinkedIn() {
		profilePresenter.setPlace(place);
		when(mockCookieProvider.getCookie(CookieKeys.LINKEDIN)).thenReturn("secret");
		String requestToken = "token";
		String verifier = "12345";
		profilePresenter.updateProfileWithLinkedIn(requestToken, verifier);
	}
	
	@Test
	public void testPublicView() {
		//view another user profile
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		String targetUserId = "12345";
		when(place.toToken()).thenReturn(targetUserId);
		profilePresenter.setPlace(place);
		verify(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		
		//also verify that it is asking for the correct teams
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).getTeamsForUser(captor.capture(),  any(AsyncCallback.class));
		
		assertEquals(targetUserId, captor.getValue());
	}

	@Test
	public void testViewMyProfileNoRedirect() {
		//view another user profile
		String myPrincipalId = "456";
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
		profilePresenter.getIsCertifiedAndUpdateView(userProfile, new ArrayList(), false, true);
		verify(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		verify(mockView).updateView(any(UserProfile.class), anyList(), anyBoolean(), anyBoolean(), any(PassingRecord.class), any(Widget.class));
	}
	
	@Test
	public void testGetIsCertifiedAndUpdateViewQuizNotTaken() throws JSONObjectAdapterException {
		//have not taken the test
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));

		profilePresenter.getIsCertifiedAndUpdateView(userProfile, new ArrayList(), false, true);
		verify(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		
		verify(mockView).updateView(any(UserProfile.class), anyList(), anyBoolean(), anyBoolean(), eq((PassingRecord)null), any(Widget.class));
	}
	
	@Test
	public void testGetIsCertifiedAndUpdateViewError() throws JSONObjectAdapterException {
		//some other error occurred
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
	
		profilePresenter.getIsCertifiedAndUpdateView(userProfile, new ArrayList(), false, true);
		verify(mockSynapseClient).getCertifiedUserPassingRecord(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testGetUserProjects() {
		profilePresenter.getUserProjects("anyUserId");
		verify(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockView).setMyProjects(eq(myProjects));
	}
	
	@Test
	public void testGetUserProjectsError() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSearchService).searchEntities(anyString(), anyList(), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		profilePresenter.getUserProjects("anyUserId");
		verify(mockView).setMyProjectsError(anyString());
	}
}