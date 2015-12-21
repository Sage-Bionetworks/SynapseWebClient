package org.sagebionetworks.web.client.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.server.servlet.UserLoginBundle;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * @author dburdick
 *
 */
public class AuthenticationControllerImplTest {

	AuthenticationController authenticationController;
	CookieProvider mockCookieProvider;
	UserProfileClientAsync mockUserProfileClient;
	UserAccountServiceAsync mockUserAccountService;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	UserSessionData sessionData;
	UserLoginBundle loginBundle;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockCookieProvider = mock(CookieProvider.class);
		mockUserAccountService = mock(UserAccountServiceAsync.class);
		
		//by default, return a valid user session data if asked
		sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		sessionData.setProfile(new UserProfile());
		sessionData.setSession(new Session());
		sessionData.getSession().setSessionToken("1234");
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("1234");
		
		loginBundle = new UserLoginBundle(sessionData, new UserBundle());
		
		AsyncMockStubber.callSuccessWith(loginBundle).when(mockUserAccountService).getUserLoginBundle(anyString(), any(AsyncCallback.class));

		authenticationController = new AuthenticationControllerImpl(mockCookieProvider, mockUserAccountService, adapterFactory, mockUserProfileClient);
	}
	
	@Test
	public void testReloadUserSessionData() {
		AsyncCallback<UserSessionData> mockCallback = mock(AsyncCallback.class);
		authenticationController.reloadUserSessionData(mockCallback);
		
		verify(mockCallback).onSuccess(eq(sessionData));
		//should have set the token when successful
		verify(mockCookieProvider).setCookie(eq(CookieKeys.USER_LOGIN_TOKEN), anyString(), any(Date.class));
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("1234");
		assertTrue(authenticationController.isLoggedIn());
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn(null);
		assertFalse(authenticationController.isLoggedIn());
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("");
		assertFalse(authenticationController.isLoggedIn());
		
		//and if we log out, then our full session has been lost and isLoggedIn always reports false
		authenticationController.logoutUser();
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("1234");
		assertFalse(authenticationController.isLoggedIn());
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn(null);
		assertFalse(authenticationController.isLoggedIn());
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("");
		assertFalse(authenticationController.isLoggedIn());
		
		verify(mockCookieProvider).setCookie(eq(CookieKeys.USER_LOGGED_IN_RECENTLY), anyString(), any(Date.class));
	}
	@Test
	public void testReloadUserSessionDataFailure() {
		Exception testException = new UnauthorizedException("Test failure");
		AsyncMockStubber.callFailureWith(testException).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));
		AsyncCallback<UserSessionData> mockCallback = mock(AsyncCallback.class);
		authenticationController.reloadUserSessionData(mockCallback);
		//should remove cookie
		verify(mockCookieProvider).removeCookie(eq(CookieKeys.USER_LOGIN_TOKEN));
		//and notify the callback
		verify(mockCallback).onFailure(any(Exception.class));
	}
	
	@Test
	public void testReloadUserSessionDataNullToken() {
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn(null);
		AsyncCallback<UserSessionData> mockCallback = mock(AsyncCallback.class);
		authenticationController.reloadUserSessionData(mockCallback);
		//notify the callback
		verify(mockCallback).onFailure(any(Exception.class));
		//should not call user account service
		verify(mockUserAccountService, times(0)).getUserSessionData(anyString(), any(AsyncCallback.class));
	}

		
	@SuppressWarnings("unchecked")
	@Test
	public void testGetCurrentUserPrincipalId() throws Exception {
		String principalId = "4321";
		UserSessionData sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		UserProfile profile = new UserProfile();
		profile.setOwnerId(principalId);
		sessionData.setProfile(profile);
		sessionData.setSession(new Session());
		sessionData.getSession().setSessionToken("1234");
		
		AsyncCallback<UserSessionData> callback = mock(AsyncCallback.class);
		AsyncMockStubber.callSuccessWith(sessionData).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));	
		
		// not logged in
		assertNull(authenticationController.getCurrentUserPrincipalId());
		
		// logged in
		authenticationController.revalidateSession("token", callback);
		assertEquals(principalId, authenticationController.getCurrentUserPrincipalId());	
		
		// empty user profile
		sessionData.setProfile(null);
		AsyncMockStubber.callSuccessWith(sessionData).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));	
		authenticationController.revalidateSession("token", callback);
		assertNull(authenticationController.getCurrentUserPrincipalId());
	}

	
}
