package org.sagebionetworks.web.client.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * @author dburdick
 *
 */
public class AuthenticationControllerImplTest {

	AuthenticationController authenticationController;
	CookieProvider mockCookieProvider;
	UserAccountServiceAsync mockUserAccountService;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void before() {
		mockCookieProvider = mock(CookieProvider.class);
		mockUserAccountService = mock(UserAccountServiceAsync.class);
		authenticationController = new AuthenticationControllerImpl(mockCookieProvider, mockUserAccountService, adapterFactory);
	}
	
	@Test
	public void testIsLoggedIn() throws Exception {
		UserSessionData sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		sessionData.setProfile(new UserProfile());
		sessionData.setSessionToken("1234");
		
		// logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(sessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		assertTrue(authenticationController.isLoggedIn());

		// not logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(null);
		assertFalse(authenticationController.isLoggedIn());
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn("");
		assertFalse(authenticationController.isLoggedIn());
		
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	public void testGetCurrentUserPrincipalId() throws Exception {
		String principalId = "4321";
		UserSessionData sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		UserProfile profile = new UserProfile();
		profile.setOwnerId(principalId);
		sessionData.setProfile(profile);
		sessionData.setSessionToken("1234");
		
		AsyncCallback<String> callback = mock(AsyncCallback.class);
		AsyncMockStubber.callSuccessWith(sessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString()).when(mockUserAccountService).getUser(anyString(), any(AsyncCallback.class));	
		
		// not logged in
		assertNull(authenticationController.getCurrentUserPrincipalId());
		
		// logged in
		authenticationController.loginUser("token", callback);
		assertEquals(principalId, authenticationController.getCurrentUserPrincipalId());	
		
		// empty user profile
		sessionData.setProfile(null);
		AsyncMockStubber.callSuccessWith(sessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString()).when(mockUserAccountService).getUser(anyString(), any(AsyncCallback.class));	
		authenticationController.loginUser("token", callback);
		assertNull(authenticationController.getCurrentUserPrincipalId());
		
		
	}

	
}
