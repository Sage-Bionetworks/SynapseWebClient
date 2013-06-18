package org.sagebionetworks.web.client.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;


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

	@Test
	public void testGetCurrentUserPrincipalId() throws Exception {
		String principalId = "4321";
		UserSessionData sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		UserProfile profile = new UserProfile();
		profile.setOwnerId(principalId);
		sessionData.setProfile(profile);
		sessionData.setSessionToken("1234");
		
		// logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(sessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		assertEquals(principalId, authenticationController.getCurrentUserPrincipalId());
		
		// not logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(null);
		assertNull(authenticationController.getCurrentUserPrincipalId());
			
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn("");
		assertNull(authenticationController.getCurrentUserPrincipalId());
		
		// empty user profile
		sessionData.setProfile(null);
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(sessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		assertNull(authenticationController.getCurrentUserPrincipalId());
		
		
	}

}
