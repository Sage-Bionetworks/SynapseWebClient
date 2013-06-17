package org.sagebionetworks.web.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;


/**
 * @author dburdick
 *
 */
public class CookieHelperImplTest {

	CookieHelper cookieHelper;
	CookieProvider mockCookieProvider;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void before() {
		mockCookieProvider = mock(CookieProvider.class);
		cookieHelper = new CookieHelperImpl(mockCookieProvider, adapterFactory);
	}
	
	@Test
	public void testIsLoggedIn() throws Exception {
		UserSessionData sessionData = new UserSessionData();
		sessionData.setIsSSO(false);
		sessionData.setProfile(new UserProfile());
		sessionData.setSessionToken("1234");
		
		// logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(sessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		assertTrue(cookieHelper.isLoggedIn());

		// not logged in
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn(null);
		assertFalse(cookieHelper.isLoggedIn());
		
		when(mockCookieProvider.getCookie(CookieKeys.USER_LOGIN_DATA)).thenReturn("");
		assertFalse(cookieHelper.isLoggedIn());
		
	}
	
}
