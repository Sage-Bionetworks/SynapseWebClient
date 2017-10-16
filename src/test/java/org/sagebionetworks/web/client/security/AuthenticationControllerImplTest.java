package org.sagebionetworks.web.client.security;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author dburdick
 *
 */
public class AuthenticationControllerImplTest {

	AuthenticationControllerImpl authenticationController;
	@Mock
	CookieProvider mockCookieProvider;
	@Mock
	UserAccountServiceAsync mockUserAccountService;
	UserSessionData sessionData;
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	ClientCache mockClientCache;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	HashMap<String, String> serverProperties;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		//by default, return a valid user session data if asked
		sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		sessionData.setProfile(new UserProfile());
		sessionData.setSession(new Session());
		sessionData.getSession().setSessionToken("1111");
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("1234");
		
		AsyncMockStubber.callSuccessWith(sessionData).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));
		authenticationController = new AuthenticationControllerImpl(mockCookieProvider, mockUserAccountService, mockSessionStorage, mockClientCache, adapterFactory, mockSynapseClient, mockGinInjector);
		serverProperties = new HashMap<String, String>();
		AsyncMockStubber.callSuccessWith(serverProperties).when(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		when(mockGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}
	
	@Test
	public void testReloadUserSessionData() {
		authenticationController.reloadUserSessionData();
		//look for user session data in local cache
		verify(mockClientCache).get(AuthenticationControllerImpl.USER_SESSION_DATA_CACHE_KEY);
		
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
		
	}
	
	@Test
	public void testReloadUserSessionDataNullToken() {
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn(null);
		authenticationController.reloadUserSessionData();
		//should not attempt to load session from cache
		verify(mockClientCache, times(0)).get(AuthenticationControllerImpl.USER_SESSION_DATA_CACHE_KEY);
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
		
		//try updating the cached profile (verify it updates the local storage cached value).
		reset(mockClientCache);
		UserProfile updatedProfile = new UserProfile();
		updatedProfile.setOwnerId("888888888");
		authenticationController.updateCachedProfile(updatedProfile);
		assertEquals(updatedProfile, authenticationController.getCurrentUserSessionData().getProfile());
		verify(mockClientCache).put(eq(AuthenticationControllerImpl.USER_SESSION_DATA_CACHE_KEY), anyString(), anyLong());
		
		// empty user profile
		sessionData.setProfile(null);
		AsyncMockStubber.callSuccessWith(sessionData).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));	
		authenticationController.revalidateSession("token", callback);
		assertNull(authenticationController.getCurrentUserPrincipalId());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetCurrentUserBundle() throws Exception {
		String principalId = "4321";
		UserSessionData sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		UserProfile profile = new UserProfile();
		profile.setOwnerId(principalId);
		sessionData.setProfile(profile);
		sessionData.setSession(new Session());
		sessionData.getSession().setSessionToken("4321");
		
		
		AsyncCallback<UserSessionData> callback = mock(AsyncCallback.class);
		AsyncMockStubber.callSuccessWith(sessionData).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));	
		
		// not logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn(null);
		assertNull(authenticationController.getCurrentUserSessionData());
		
		// logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_TOKEN)).thenReturn("1234");
		authenticationController.revalidateSession("token", callback);
		assertEquals(sessionData, authenticationController.getCurrentUserSessionData());	
		
		// empty user profile
		sessionData.setProfile(null);
		AsyncMockStubber.callSuccessWith(sessionData).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));	
		authenticationController.revalidateSession("token", callback);
		assertNull(authenticationController.getCurrentUserPrincipalId());
	}
	
	@Test
	public void testLogout() {
		String propKey = "foo";
		String propValue = "bar";
		serverProperties.put(propKey, propValue);
		authenticationController.logoutUser();
		verify(mockCookieProvider).removeCookie(CookieKeys.USER_LOGIN_TOKEN);
		verify(mockSessionStorage).clear();
		verify(mockClientCache).clear();
		verify(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		verify(mockClientCache).put(eq(propKey), eq(propValue), anyLong());
	}
	
	
	@Test
	public void testStoreLoginReceipt() {
		String username = "testusername";
		String receipt = "31416";
		authenticationController.storeAuthenticationReceipt(username, receipt);
		verify(mockClientCache).put(eq(username + AuthenticationControllerImpl.USER_AUTHENTICATION_RECEIPT), eq(receipt), anyLong());
	}
	
	@Test
	public void testGetLoginRequest() {
		String username = "testusername";
		String password = "pw";
		
		LoginRequest request = authenticationController.getLoginRequest(username, password);
		assertNull(request.getAuthenticationReceipt());
		
		String cachedReceipt = "12345";
		when(mockClientCache.get(username + AuthenticationControllerImpl.USER_AUTHENTICATION_RECEIPT)).thenReturn(cachedReceipt);
		request = authenticationController.getLoginRequest(username, password);
		assertEquals(cachedReceipt, request.getAuthenticationReceipt());
	}
	
	@Test
	public void testLoginUser() {
		String username = "testusername";
		String password = "pw";
		String oldAuthReceipt = "1234";
		String newSessionToken = "abcdzxcvbn";
		String newAuthReceipt = "5678";
		when(mockClientCache.get(username + AuthenticationControllerImpl.USER_AUTHENTICATION_RECEIPT)).thenReturn(oldAuthReceipt);
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setAcceptsTermsOfUse(true);
		loginResponse.setAuthenticationReceipt(newAuthReceipt);
		loginResponse.setSessionToken(newSessionToken);
		AsyncMockStubber.callSuccessWith(loginResponse).when(mockUserAccountService).initiateSession(any(LoginRequest.class), any(AsyncCallback.class));
		AsyncCallback loginCallback = mock(AsyncCallback.class);
		
		//make the actual call
		authenticationController.loginUser(username, password, loginCallback);
		
		//verify input arguments (including the cached receipt)
		ArgumentCaptor<LoginRequest> loginRequestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
		verify(mockUserAccountService).initiateSession(loginRequestCaptor.capture(), any(AsyncCallback.class));
		LoginRequest request = loginRequestCaptor.getValue();
		assertEquals(username, request.getUsername());
		assertEquals(password, request.getPassword());
		assertEquals(oldAuthReceipt, request.getAuthenticationReceipt());
		
		//verify the new receipt is cached
		verify(mockClientCache).put(eq(username + AuthenticationControllerImpl.USER_AUTHENTICATION_RECEIPT), eq(newAuthReceipt), anyLong());
		
		verify(loginCallback).onSuccess(any(UserSessionData.class));
	}
	
	@Test
	public void testLoginUserFailure() {
		Exception ex = new Exception("invalid login");
		AsyncMockStubber.callFailureWith(ex).when(mockUserAccountService).initiateSession(any(LoginRequest.class), any(AsyncCallback.class));
		String username = "testusername";
		String password = "pw";
		AsyncCallback loginCallback = mock(AsyncCallback.class);
		
		//make the actual call
		authenticationController.loginUser(username, password, loginCallback);
		
		verify(loginCallback).onFailure(ex);
	}
	
	@Test
	public void testLoginUserFailureReadOnlyMode() {
		//successfully log the user in, but put the stack into read only mode after login (so session token validation fails with a ReadOnlyException).
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setSessionToken("213344");
		AsyncMockStubber.callSuccessWith(loginResponse).when(mockUserAccountService).initiateSession(any(LoginRequest.class), any(AsyncCallback.class));
		ReadOnlyModeException ex = new ReadOnlyModeException();
		AsyncMockStubber.callFailureWith(ex).when(mockUserAccountService).getUserSessionData(anyString(), any(AsyncCallback.class));
		String username = "testusername";
		String password = "pw";
		AsyncCallback loginCallback = mock(AsyncCallback.class);
		
		authenticationController.loginUser(username, password, loginCallback);
		
		verify(loginCallback, never()).onFailure(ex);
		verify(mockPlaceChanger).goTo(any(Down.class));
	}
}
