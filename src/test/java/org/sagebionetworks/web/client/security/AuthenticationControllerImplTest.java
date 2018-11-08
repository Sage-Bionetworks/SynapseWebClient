package org.sagebionetworks.web.client.security;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.sagebionetworks.web.client.SessionDetector;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AuthenticationControllerImplTest {

	AuthenticationControllerImpl authenticationController;
	@Mock
	CookieProvider mockCookieProvider;
	@Mock
	UserAccountServiceAsync mockUserAccountService;
	@Mock
	ClientCache mockClientCache;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	Header mockHeader;
	@Mock
	SessionDetector mockSessionDetector;
	@Mock
	Callback mockCallback;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	UserProfile profile;
	UserSessionData usd;
	public static final String USER_ID = "98208";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		//by default, return a valid user session data if asked
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).initSession(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("1111").when(mockUserAccountService).getCurrentSessionToken(any(AsyncCallback.class));
		usd = new UserSessionData();
		profile = new UserProfile();
		profile.setOwnerId(USER_ID);
		usd.setProfile(profile);
		Session session = new Session();
		session.setSessionToken("1111");
		session.setAcceptsTermsOfUse(true);
		usd.setSession(session);
		AsyncMockStubber.callSuccessWith(usd).when(mockUserAccountService).getCurrentUserSessionData(any(AsyncCallback.class));
		when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
		authenticationController = new AuthenticationControllerImpl(mockUserAccountService, mockClientCache, mockCookieProvider, mockGinInjector, mockSynapseJSNIUtils);
		when(mockGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockGinInjector.getHeader()).thenReturn(mockHeader);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockGinInjector.getSessionDetector()).thenReturn(mockSessionDetector);
	}
	
	@Test
	public void testReloadUserSessionData() {
		authenticationController.reloadUserSessionData(mockCallback);
		
		// attempt to get the current session
		verify(mockUserAccountService).getCurrentUserSessionData(any(AsyncCallback.class));
		assertTrue(authenticationController.isLoggedIn());
		verify(mockCallback).invoke();
		assertEquals(USER_ID, authenticationController.getCurrentUserPrincipalId());
		
		//and if we log out, then our full session has been lost and isLoggedIn always reports false
		authenticationController.logoutUser();
		
		assertNull(authenticationController.getCurrentUserPrincipalId());
	}
	
	@Test
	public void testReloadUserSessionDataNullToken() {
		// when there is a null token, getting the current user session data will fail
		AsyncMockStubber.callFailureWith(new Exception()).when(mockUserAccountService).getCurrentUserSessionData(any(AsyncCallback.class));
		
		authenticationController.reloadUserSessionData(mockCallback);
		
		assertFalse(authenticationController.isLoggedIn());
		assertNull(authenticationController.getCurrentUserPrincipalId());
		verify(mockCallback).invoke();
	}
		
	@Test
	public void testGetCurrentUserPrincipalIdNullProfile() throws Exception {
		usd.setProfile(null);
		
		authenticationController.reloadUserSessionData(mockCallback);
		
		assertFalse(authenticationController.isLoggedIn());
		assertNull(authenticationController.getCurrentUserPrincipalId());
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testLogout() {
		authenticationController.logoutUser();
		
		//sets session cookie
		verify(mockJsClient).initSession(eq(WebConstants.EXPIRE_SESSION_TOKEN));
		verify(mockClientCache).clear();
		verify(mockSessionDetector).initializeSessionTokenState();
		verify(mockHeader).refresh();
		verify(mockJsClient).logout();
		verify(mockSynapseJSNIUtils).setAnalyticsUserId("");
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
		AsyncMockStubber.callSuccessWith(loginResponse).when(mockJsClient).login(any(LoginRequest.class), any(AsyncCallback.class));
		AsyncCallback loginCallback = mock(AsyncCallback.class);
		
		//make the actual call
		authenticationController.loginUser(username, password, loginCallback);
		
		//verify input arguments (including the cached receipt)
		ArgumentCaptor<LoginRequest> loginRequestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
		verify(mockJsClient).login(loginRequestCaptor.capture(), any(AsyncCallback.class));
		LoginRequest request = loginRequestCaptor.getValue();
		assertEquals(username, request.getUsername());
		assertEquals(password, request.getPassword());
		assertEquals(oldAuthReceipt, request.getAuthenticationReceipt());
		
		//verify the new receipt is cached
		verify(mockClientCache).put(eq(username + AuthenticationControllerImpl.USER_AUTHENTICATION_RECEIPT), eq(newAuthReceipt), anyLong());
		
		verify(loginCallback).onSuccess(any(UserProfile.class));
		verify(mockSessionDetector).initializeSessionTokenState();
		verify(mockSynapseJSNIUtils).setAnalyticsUserId(USER_ID);
	}
	
	@Test
	public void testLoginUserFailure() {
		Exception ex = new Exception("invalid login");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).login(any(LoginRequest.class), any(AsyncCallback.class));
		String username = "testusername";
		String password = "pw";
		AsyncCallback loginCallback = mock(AsyncCallback.class);
		
		//make the actual call
		authenticationController.loginUser(username, password, loginCallback);
		
		verify(loginCallback).onFailure(ex);
	}

	// Note.  If login when the stack is in READ_ONLY mode, then the widgets SynapseAlert should send user to the Down page.
}
