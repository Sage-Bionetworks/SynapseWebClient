package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LoginPresenterTest {
	
	LoginPresenter loginPresenter;
	LoginView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	CookieProvider mockCookieProvier;
	GWTWrapper mockGwtWrapper;
	SynapseJSNIUtils mockJSNIUtils;
	SynapseClientAsync mockSynapseClient;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	PlaceChanger mockPlaceChanger;
	AcceptsOneWidget mockPanel;
	EventBus mockEventBus;
	UserSessionData usd;
	String userId = "007";
	
	@Before
	public void setup(){
		mockView = mock(LoginView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookieProvier = mock(CookieProvider.class);
		mockGwtWrapper = mock(GWTWrapper.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		mockPlaceChanger = mock(PlaceChanger.class);		
		mockPanel = mock(AcceptsOneWidget.class);
		mockEventBus = mock(EventBus.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		usd = new UserSessionData();
		Session session = new Session();
		session.setAcceptsTermsOfUse(true);
		usd.setSession(session);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockJSNIUtils.getLocationPath()).thenReturn("/Portal.html");
		when(mockJSNIUtils.getLocationQueryString()).thenReturn("?foo=bar");
		loginPresenter = new LoginPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockNodeModelCreator,mockCookieProvier, mockGwtWrapper, mockJSNIUtils, jsonObjectAdapter, mockSynapseClient, adapterFactory);
		loginPresenter.start(mockPanel, mockEventBus);
		verify(mockView).setPresenter(loginPresenter);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
	}	
	
	private void setPlace() {
		LoginPlace place = Mockito.mock(LoginPlace.class);
		loginPresenter.setPlace(place);
	}
	
	private void setMyProfile(UserProfile profile) throws JSONObjectAdapterException {
		jsonObjectAdapter = profile.writeToJSONObject(jsonObjectAdapter.createNew());
		String userProfileJson = jsonObjectAdapter.toJSONString();
		AsyncMockStubber.callSuccessWith(userProfileJson).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSetPlaceDefault() {
		setPlace();
		Assert.assertEquals("/Portal.html?foo=bar#!LoginPlace", loginPresenter.getOpenIdReturnUrl());
	}
	
	@Test 
	public void testSetPlaceLogout() {
		LoginPlace place = new LoginPlace(LoginPlace.LOGOUT_TOKEN);
		loginPresenter.setPlace(place);
		verify(mockView).showLogout();
		verify(mockAuthenticationController).logoutUser();
	}

	@Test 
	public void testSetPlaceUnknownSSOUser() {
		LoginPlace place = new LoginPlace(WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN);
		loginPresenter.setPlace(place);
		ArgumentCaptor<Place> argument = ArgumentCaptor.forClass(Place.class);				   
		verify(mockView).showErrorMessage(anyString());
		verify(mockPlaceChanger).goTo(argument.capture());
		assertTrue(argument.getValue() instanceof RegisterAccount);
	}

	@Test 
	public void testSetPlaceUnknownSSOError() {
		LoginPlace place = new LoginPlace(WebConstants.OPEN_ID_ERROR_TOKEN);
		loginPresenter.setPlace(place);
		verify(mockView).showErrorMessage(anyString());
		verify(mockView).showLogin(anyString(), anyString());
	}
	
	@Test 
	public void testSetPlaceSSOLogin() throws JSONObjectAdapterException {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		LoginPlace place = new LoginPlace(fakeToken);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("NotTemporary");
		setMyProfile(profile);

		loginPresenter.setPlace(place);
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(any(Place.class));
	}
	
	@Test 
	public void testCheckTempUsername() throws JSONObjectAdapterException {
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName(WebConstants.TEMPORARY_USERNAME_PREFIX + "222");
		usd.setProfile(profile);
		loginPresenter.checkForTempUsername();
		verify(mockPlaceChanger).goTo(any(ChangeUsername.class));
	}
	
	@Test 
	public void testCheckTempUsernameNotTemp() throws JSONObjectAdapterException {
		Place mockLastPlace = Mockito.mock(Place.class);
		when(mockGlobalApplicationState.getLastPlace(any(Place.class))).thenReturn(mockLastPlace);
		
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("not-temp");
		usd.setProfile(profile);
		loginPresenter.checkForTempUsername();
		//should go to the last place, since this is not a temporary username
		verify(mockPlaceChanger).goTo(eq(mockLastPlace));
	}
	
	
	@Test 
	public void testSetPlaceChangeUsername()throws JSONObjectAdapterException {
		LoginPlace place = new LoginPlace(LoginPlace.CHANGE_USERNAME);
		loginPresenter.setPlace(place);
		verify(mockPlaceChanger).goTo(any(ChangeUsername.class));
	}

	@Test
	public void testOpenInvitations() throws JSONObjectAdapterException {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		LoginPlace place = new LoginPlace(fakeToken);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("valid-username");
		setMyProfile(profile);
		
		loginPresenter.setPlace(place);
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
	}
	
	
	@Test
	public void testSetNewUserSSO() throws JSONObjectAdapterException {
		UserSessionData sessionData = new UserSessionData();
		Session session = new Session();
		session.setSessionToken("my session token");
		sessionData.setSession(session);
		sessionData.setIsSSO(true);
		loginPresenter.setNewUser(sessionData);
		verify(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSetNewUser() throws JSONObjectAdapterException {
		UserSessionData sessionData = new UserSessionData();
		Session session = new Session();
		session.setSessionToken("my session token");
		sessionData.setSession(session);
		sessionData.setIsSSO(false);
		loginPresenter.setNewUser(sessionData);
		verify(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
	}
	
	@Test 
	public void testSetPlaceSSOLoginNotSignedToU() throws JSONObjectAdapterException {
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("valid-username");
		setMyProfile(profile);

		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		LoginPlace place = new LoginPlace(fakeToken);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		usd.getSession().setAcceptsTermsOfUse(false);
		AsyncMockStubber.callSuccessWith("tou").when(mockAuthenticationController).getTermsOfUse(any(AsyncCallback.class));
		
		//run the test
		loginPresenter.setPlace(place);
		
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
		
		ArgumentCaptor<AcceptTermsOfUseCallback> argument = ArgumentCaptor.forClass(AcceptTermsOfUseCallback.class);
		//shows terms of use
		verify(mockView).showTermsOfUse(anyString(), argument.capture());
	}

	@Test
	public void testUserAuthenticated() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		loginPresenter.userAuthenticated();
		verify(mockView).showErrorMessage(anyString());
		verify(mockView).showLogin(anyString(), anyString());
	}
	
	@Test
	public void testValidWidgetName() {
		assertTrue(LoginPresenter.isValidWidgetName("a widget name"));
		assertTrue(LoginPresenter.isValidWidgetName("special characters allowed (-+) and 01239"));
		
		assertFalse(LoginPresenter.isValidWidgetName("special characters disallowed like *$"));
		assertFalse(LoginPresenter.isValidWidgetName(null));
		assertFalse(LoginPresenter.isValidWidgetName(""));
	}

	@Test
	public void testIsValidUrl() {
		assertTrue(LoginPresenter.isValidUrl("https://www.youtube.com/watch?v=m86ae_e_ptU", false));
		assertTrue(LoginPresenter.isValidUrl("http://www.google.com", false));
		assertTrue(LoginPresenter.isValidUrl("#!Synapse:syn123", false));
		
		assertFalse(LoginPresenter.isValidUrl("http:/www.google.com", false));
		assertFalse(LoginPresenter.isValidUrl("missingprotocol.com", false));
		
		//undefined url handling
		assertTrue(LoginPresenter.isValidUrl("", true));
		assertFalse(LoginPresenter.isValidUrl("", false));
		
		assertTrue(LoginPresenter.isValidUrl(null, true));
		assertFalse(LoginPresenter.isValidUrl(null, false));
	}
	
	@Test
	public void testLastPlaceAfterLogin() {
		//this should send to this user's profile (dashboard) by default
		Place mockPlace = mock(Place.class);
		when(mockGlobalApplicationState.getLastPlace(any(Place.class))).thenReturn(mockPlace);
		loginPresenter.goToLastPlace();
		
		verify(mockPlaceChanger).goTo(eq(mockPlace));
		ArgumentCaptor<Place> defaultPlaceCaptor = ArgumentCaptor.forClass(Place.class);
		verify(mockGlobalApplicationState).getLastPlace(defaultPlaceCaptor.capture());
		Place defaultPlace = defaultPlaceCaptor.getValue();
		assertTrue(defaultPlace instanceof Profile);
		assertEquals(userId, ((Profile)defaultPlace).getUserId());
	}
	
	
}
