package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LoginPresenterTest {
	
	LoginPresenter loginPresenter;
	@Mock
	LoginView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	AcceptsOneWidget mockPanel;
	@Mock
	EventBus mockEventBus;
	@Mock
	LoginPlace mockLoginPlace;
	@Mock
	Callback mockTouCallback;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	@Captor
	ArgumentCaptor<Callback> touCallbackCaptor;
	
	UserSessionData usd;
	String userId = "007";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		usd = new UserSessionData();
		Session session = new Session();
		session.setAcceptsTermsOfUse(true);
		usd.setSession(session);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		loginPresenter = new LoginPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockSynAlert);
		loginPresenter.start(mockPanel, mockEventBus);
		verify(mockView).setPresenter(loginPresenter);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
	}	
	
	@Test 
	public void testSetPlaceLogout() {
		when(mockLoginPlace.toToken()).thenReturn(LoginPlace.LOGOUT_TOKEN);
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockView).showLogout();
		verify(mockAuthenticationController).logoutUser();
	}

	@Test 
	public void testSetPlaceUnknownSSOUser() {
		when(mockLoginPlace.toToken()).thenReturn(WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN);
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockView).showErrorMessage(anyString());
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		assertTrue(placeCaptor.getValue() instanceof RegisterAccount);
	}

	@Test 
	public void testSetPlaceUnknownSSOError() {
		when(mockLoginPlace.toToken()).thenReturn(WebConstants.OPEN_ID_ERROR_TOKEN);
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockView).showErrorMessage(anyString());
		verify(mockView).showLogin();
	}
	
	@Test 
	public void testSetPlaceShowAndAcceptToU() {
		when(mockLoginPlace.toToken()).thenReturn(LoginPlace.SHOW_TOU);
		
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		usd.getSession().setAcceptsTermsOfUse(false);
		
		//method under test
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockView).showTermsOfUse(touCallbackCaptor.capture());
		Callback touCallback = touCallbackCaptor.getValue();
		//set up revalidateSession response such that user has now accepted the tou
		usd.getSession().setAcceptsTermsOfUse(true);
		AsyncMockStubber.callSuccessWith(null).when(mockAuthenticationController).signTermsOfUse(anyBoolean(), any(AsyncCallback.class));
		
		touCallback.invoke();
		
		verify(mockAuthenticationController).signTermsOfUse(eq(true), any(AsyncCallback.class));
		verify(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));
		// verify we only showed this once:
		verify(mockView).showTermsOfUse(any(Callback.class));
		//go to the last place (or the user dashboard Profile place if last place is not set)
		verify(mockGlobalApplicationState).gotoLastPlace(any(Profile.class));
	}
	
	@Test 
	public void testSetPlaceShowTouUserAlreadyAccepted() {
		when(mockLoginPlace.toToken()).thenReturn(LoginPlace.SHOW_TOU);
		
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		usd.getSession().setAcceptsTermsOfUse(true);
		
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockView, never()).showTermsOfUse(any(Callback.class));
		verify(mockGlobalApplicationState).gotoLastPlace();
	}
	
	@Test 
	public void testSetPlaceSSOLogin() throws JSONObjectAdapterException {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		when(mockLoginPlace.toToken()).thenReturn(fakeToken);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
		verify(mockGlobalApplicationState).gotoLastPlace(any(Place.class));
	}
	
	@Test 
	public void testCheckTempUsername() {
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName(WebConstants.TEMPORARY_USERNAME_PREFIX + "222");
		usd.setProfile(profile);
		loginPresenter.checkForTempUsername();
		verify(mockPlaceChanger).goTo(isA(ChangeUsername.class));
	}
	
	@Test 
	public void testCheckTempUsernameNotTemp() {
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("not-temp");
		usd.setProfile(profile);
		loginPresenter.checkForTempUsername();
		//should go to the last place, since this is not a temporary username
		verify(mockGlobalApplicationState).gotoLastPlace(any(Place.class));
	}
	
	@Test 
	public void testSetPlaceChangeUsername() {
		when(mockLoginPlace.toToken()).thenReturn(LoginPlace.CHANGE_USERNAME);
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockPlaceChanger).goTo(isA(ChangeUsername.class));
	}

	@Test
	public void testOpenInvitations() {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		when(mockLoginPlace.toToken()).thenReturn(fakeToken);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		
		loginPresenter.setPlace(mockLoginPlace);
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
	}
	
	
	@Test
	public void testSetNewUserSSO() {
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
	public void testSetPlaceSSOLoginNotSignedToU() {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		when(mockLoginPlace.toToken()).thenReturn(fakeToken);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthenticationController).revalidateSession(anyString(), any(AsyncCallback.class));		
		usd.getSession().setAcceptsTermsOfUse(false);
		
		//run the test
		loginPresenter.setPlace(mockLoginPlace);
		
		verify(mockAuthenticationController).revalidateSession(eq(fakeToken), any(AsyncCallback.class));
		
		//shows terms of use
		verify(mockView).showTermsOfUse(any(Callback.class));
	}

	@Test
	public void testUserAuthenticated() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		loginPresenter.userAuthenticated();
		verify(mockView).showErrorMessage(anyString());
		verify(mockView).showLogin();
	}
	
	@Test
	public void testLastPlaceAfterLogin() {
		//this should send to this user's profile (dashboard) by default
		loginPresenter.goToLastPlace();
		
		verify(mockGlobalApplicationState).gotoLastPlace(placeCaptor.capture());
		Place defaultPlace = placeCaptor.getValue();
		assertTrue(defaultPlace instanceof Profile);
		assertEquals(userId, ((Profile)defaultPlace).getUserId());
	}
	
	@Test
	public void testGotoPlace() {
		loginPresenter.goTo(mockLoginPlace);
		verify(mockPlaceChanger).goTo(mockLoginPlace);
	}
}
