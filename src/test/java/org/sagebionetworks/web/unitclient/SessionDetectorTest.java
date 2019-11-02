package org.sagebionetworks.web.unitclient;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SessionDetector;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;

@RunWith(MockitoJUnitRunner.class)
public class SessionDetectorTest {

	SessionDetector sessionDetector;

	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	ClientCache mockClientCache;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	@Before
	public void setUp() throws Exception {
		sessionDetector =
				new SessionDetector(mockAuthController, mockGlobalAppState, mockGWT, mockClientCache);
	}

	@Test
	public void testSessionChanges() {
		sessionDetector.start();

		verify(mockGWT).scheduleFixedDelay(callbackCaptor.capture(), eq(SessionDetector.INTERVAL_MS));
		// SWC-4947
		verify(mockAuthController).checkForUserChange();

		Callback cb = callbackCaptor.getValue();

		// Was no session, still is no session.
		cb.invoke();
		verify(mockAuthController, times(1)).checkForUserChange();

		// Was no session, is now logged in. Should check for user change.
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("1");
		cb.invoke();
		verify(mockAuthController, times(2)).checkForUserChange();

		// Was a session, now is no session. Should check for user change.
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(null);
		when(mockClientCache.get(SessionDetector.SESSION_MARKER)).thenReturn("1");
		cb.invoke();
		verify(mockAuthController, times(3)).checkForUserChange();

		// Was a session, now has same session. No need to check for user change.
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("1");
		when(mockClientCache.get(SessionDetector.SESSION_MARKER)).thenReturn("1");
		cb.invoke();
		verify(mockAuthController, times(3)).checkForUserChange();
	}

}
