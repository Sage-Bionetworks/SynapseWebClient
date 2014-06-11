package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.RSSFeed;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
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
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
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
		AsyncMockStubber.callSuccessWith("success").when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("NotTemporary");
		setMyProfile(profile);

		loginPresenter.setPlace(place);
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
		verify(mockPlaceChanger).goTo(any(Place.class));
	}
	
//	@Test 
//	public void testSetPlaceSSOLoginTempUsername() throws JSONObjectAdapterException {
//		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
//		LoginPlace place = new LoginPlace(fakeToken);
//		AsyncMockStubber.callSuccessWith("success").when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
//		
//		UserProfile profile = new UserProfile();
//		profile.setOwnerId("1233");
//		profile.setUserName(WebConstants.TEMPORARY_USERNAME_PREFIX + "222");
//		setMyProfile(profile);
//		
//		loginPresenter.setPlace(place);
//		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
//		verify(mockView).showSetUsernameUI();
//	}
	
	
	@Test
	public void testUpdateProfile() {
		setPlace();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		AsyncCallback<Void> mockCallback = mock(AsyncCallback.class);
		loginPresenter.updateProfile(profile, mockCallback);
		verify(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		verify(mockAuthenticationController).updateCachedProfile(eq(profile));
		verify(mockCallback).onSuccess(any(Void.class));
	}
	
	@Test
	public void testUpdateProfileFailed() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		setPlace();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		AsyncCallback<Void> mockCallback = mock(AsyncCallback.class);
		loginPresenter.updateProfile(profile, mockCallback);
		verify(mockSynapseClient).updateUserProfile(anyString(), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}
	
//	@Test
//	public void testSetNewUserTempUsername() throws JSONObjectAdapterException {
//		//if the user has a temp username, then it should show the UI to set the username
//		setPlace();
//		UserProfile profile = new UserProfile();
//		profile.setOwnerId("1233");
//		profile.setUserName(WebConstants.TEMPORARY_USERNAME_PREFIX + "222");
//		setMyProfile(profile);
//		loginPresenter.postLoginStep1();
//		verify(mockAuthenticationController).updateCachedProfile(eq(profile));
//		verify(mockView).showLoggingInLoader();
//		verify(mockView).showSetUsernameUI();
//		verify(mockView).hideLoggingInLoader();
//	}
	
//	@Test
//	public void testCheckForTempUsernameAndContinueFailure() throws JSONObjectAdapterException {
//		setPlace();
//		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
//		loginPresenter.postLoginStep1();
//		verify(mockView, times(2)).showLoggingInLoader();
//		//hides loading UI and continue (go to last place) 
//		verify(mockView).hideLoggingInLoader();
//		verify(mockEventBus).fireEvent(any(PlaceChangeEvent.class));
//	}
	
	@Test 
	public void testSetPlaceChangeUsername()throws JSONObjectAdapterException {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("222");
		setMyProfile(profile);
		LoginPlace place = new LoginPlace(LoginPlace.CHANGE_USERNAME);
		loginPresenter.setPlace(place);
		verify(mockView).showSetUsernameUI();
	}
	
	@Test 
	public void testSetPlaceChangeAnonymousUsername()throws JSONObjectAdapterException {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("222");
		setMyProfile(profile);
		LoginPlace place = new LoginPlace(LoginPlace.CHANGE_USERNAME);
		loginPresenter.setPlace(place);
		verify(mockView, never()).showSetUsernameUI();
		verify(mockView).showLogin(anyString(), anyString());
	}
	
	@Test 
	public void testSetUsernameSuccess()throws JSONObjectAdapterException {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("222");
		setMyProfile(profile);
		loginPresenter.updateProfile(profile, mock(AsyncCallback.class));
		//TODO: mock notification suppression setting for certification, once in place
		loginPresenter.setUsername("newname");
		
		verify(mockSynapseClient, times(2)).updateUserProfile(anyString(), any(AsyncCallback.class));
	}

	
	@Test 
	public void testSetPlaceChangeUsernameFailure()throws JSONObjectAdapterException {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String exceptionMessage = "unhandled";
		AsyncMockStubber.callFailureWith(new Exception(exceptionMessage)).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		LoginPlace place = new LoginPlace(LoginPlace.CHANGE_USERNAME);
		loginPresenter.setPlace(place);
		verify(mockView).showErrorMessage(eq(exceptionMessage));
	}

	@Test
	public void testOpenInvitations() throws JSONObjectAdapterException {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		LoginPlace place = new LoginPlace(fakeToken);
		AsyncMockStubber.callSuccessWith("success").when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		
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
		AsyncMockStubber.callSuccessWith("success").when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		usd.getSession().setAcceptsTermsOfUse(false);
		AsyncMockStubber.callSuccessWith("tou").when(mockAuthenticationController).getTermsOfUse(any(AsyncCallback.class));
		
		//run the test
		loginPresenter.setPlace(place);
		
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
		
		ArgumentCaptor<AcceptTermsOfUseCallback> argument = ArgumentCaptor.forClass(AcceptTermsOfUseCallback.class);
		//shows terms of use
		verify(mockView).showTermsOfUse(anyString(), argument.capture());
	}

	
}
