package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.LinkedInServiceAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProfileView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ProfilePresenterTest {
	
	ProfilePresenter profilePresenter;
	ProfileView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	LinkedInServiceAsync mockLinkedInService;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	CookieProvider mockCookieProvider;
	GWTWrapper mockGWTWrapper;
	Profile place = Mockito.mock(Profile.class);
	
	UserSessionData testUser = new UserSessionData();
	UserProfile userProfile = new UserProfile();
	String testUserJson;
	String password = "password";
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(ProfileView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockLinkedInService = mock(LinkedInServiceAsync.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockUserService, mockLinkedInService, mockGlobalApplicationState, mockSynapseClient, mockNodeModelCreator, mockCookieProvider, mockGWTWrapper);	
		verify(mockView).setPresenter(profilePresenter);

		profilePresenter.setPlace(place);
		userProfile.setDisplayName("tester");
		userProfile.setOwnerId("1");
		testUser.setProfile(userProfile);
		testUser.setSessionToken("token");
		testUser.setIsSSO(false);
		
		JSONObjectAdapter adapter = new JSONObjectAdapterImpl().createNew();
		testUser.writeToJSONObject(adapter);
		testUserJson = adapter.toJSONString(); 
	}
	
	@Test
	public void testStart() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockSynapseClient);
		reset(mockNodeModelCreator);
		reset(mockGWTWrapper);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockUserService, mockLinkedInService, mockGlobalApplicationState, mockSynapseClient, mockNodeModelCreator, mockCookieProvider, mockGWTWrapper);	
		profilePresenter.setPlace(place);

		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		
		profilePresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);
	}
	
	@Test
	public void testSetPlace() {
		Profile newPlace = Mockito.mock(Profile.class);
		profilePresenter.setPlace(newPlace);
	}
	
	@Test
	public void testUpdateProfile() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		reset(mockSynapseClient);
		reset(mockNodeModelCreator);
		reset(mockGWTWrapper);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockUserService, mockLinkedInService, mockGlobalApplicationState, mockSynapseClient, mockNodeModelCreator,mockCookieProvider, mockGWTWrapper);	
		profilePresenter.setPlace(place);

		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		String firstName = "John";
		String lastName = "Smith";
		String summary = "A career summary";
		String position = "Senior Director of writing code";
		String location = "Seattle Area";
		String industry = "Biotech";
		String company = "Sage Bionetworks";
		String email = "johnsmith@sagebio.org";
		AttachmentData pic = new AttachmentData();
		profilePresenter.updateProfile(firstName, lastName, summary, position, location, industry, company, email, pic);
	}
	
	@Test
	public void testRedirectToLinkedIn() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		reset(mockSynapseClient);
		reset(mockNodeModelCreator);
		reset(mockGWTWrapper);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockUserService, mockLinkedInService, mockGlobalApplicationState, mockSynapseClient, mockNodeModelCreator,mockCookieProvider, mockGWTWrapper);	
		profilePresenter.setPlace(place);
	
		profilePresenter.redirectToLinkedIn();
	}
	
	@Test
	public void testUpdateProfileWithLinkedIn() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		reset(mockSynapseClient);
		reset(mockNodeModelCreator);
		reset(mockGWTWrapper);
		profilePresenter = new ProfilePresenter(mockView, mockAuthenticationController, mockUserService, mockLinkedInService, mockGlobalApplicationState, mockSynapseClient, mockNodeModelCreator,mockCookieProvider, mockGWTWrapper);	
		profilePresenter.setPlace(place);

		when(mockCookieProvider.getCookie(CookieKeys.LINKEDIN)).thenReturn("secret");
		String requestToken = "token";
		String verifier = "12345";
		profilePresenter.updateProfileWithLinkedIn(requestToken, verifier);
	}
	
}