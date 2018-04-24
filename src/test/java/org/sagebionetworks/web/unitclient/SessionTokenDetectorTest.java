package org.sagebionetworks.web.unitclient;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.SessionTokenDetector.*;
import static org.sagebionetworks.web.client.cookie.CookieKeys.*;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SessionTokenDetector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;

public class SessionTokenDetectorTest {
	@Mock
	CookieProvider mockCookies;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	GWTWrapper mockGwt;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	
	SessionTokenDetector detector;
	Callback timerCallback;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		detector = new SessionTokenDetector(mockCookies, mockAuthController, mockGlobalAppState, mockGwt);
		
		detector.start();
		verify(mockGwt).scheduleFixedDelay(callbackCaptor.capture(), eq(INTERVAL_MS));
		timerCallback = callbackCaptor.getValue();
	}
	
	private void setSessionCookie() {
		when(mockCookies.getCookie(USER_LOGIN_TOKEN)).thenReturn("a session token");
	}
	
	private void clearSessionCookie() {
		when(mockCookies.getCookie(USER_LOGIN_TOKEN)).thenReturn(null);
	}


	@Test
	public void testLoggedIn() {
		// user logged in, in another window
		setSessionCookie();
		
		timerCallback.invoke();
		
		verify(mockAuthController).reloadUserSessionData();
		verify(mockGlobalAppState).refreshPage();
	}
	
	@Test
	public void testLoggedOut() {
		// In this case, simulate that the app is initialized with a session token available, and user will log out in another window.
		setSessionCookie();
		detector.initializeSessionTokenState();
		// log out in the other window (removes session token cookie)
		clearSessionCookie();
		
		timerCallback.invoke();
		
		verify(mockAuthController).logoutUser();
		verify(mockGlobalAppState).refreshPage();
	}
	

}
