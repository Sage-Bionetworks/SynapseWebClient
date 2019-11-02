package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandlerImpl.SESSION_KEY_PREFIX;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandlerImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class IsACTMemberAsyncHandlerTest {
	IsACTMemberAsyncHandlerImpl widget;
	@Mock
	UserProfileClientAsync mockUserProfileClient;
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	CallbackP<Boolean> mockCallback;
	@Mock
	UserBundle mockUserBundle;
	public static final String CURRENT_USER_ID = "33325";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new IsACTMemberAsyncHandlerImpl(mockUserProfileClient, mockSessionStorage, mockAuthController, mockJsniUtils);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
	}

	@Test
	public void testAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.isACTActionAvailable(mockCallback);
		verify(mockCallback).invoke(false);
	}

	@Test
	public void testCache() {
		when(mockSessionStorage.getItem(SESSION_KEY_PREFIX + CURRENT_USER_ID)).thenReturn(Boolean.TRUE.toString());
		widget.isACTActionAvailable(mockCallback);
		verify(mockCallback).invoke(true);
		// if hiding ACT UI, should invoke false
		widget.setACTActionVisible(false);
		widget.isACTActionAvailable(mockCallback);
		verify(mockCallback).invoke(false);

		reset(mockCallback);

		when(mockSessionStorage.getItem(SESSION_KEY_PREFIX + CURRENT_USER_ID)).thenReturn(Boolean.FALSE.toString());
		widget.isACTActionAvailable(mockCallback);
		verify(mockCallback).invoke(false);
		widget.setACTActionVisible(true);
		widget.isACTActionAvailable(mockCallback);
		verify(mockCallback, times(2)).invoke(false);
	}

	@Test
	public void testRpcSuccess() {
		Boolean isACTMember = true;
		when(mockUserBundle.getIsACTMember()).thenReturn(isACTMember);
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		widget.isACTActionAvailable(mockCallback);
		verify(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		verify(mockSessionStorage).setItem(SESSION_KEY_PREFIX + CURRENT_USER_ID, isACTMember.toString());
		verify(mockCallback).invoke(isACTMember);
	}

	@Test
	public void testRpcSuccessHideACTAction() {
		// Is in ACT, but hide ACT UI. Answer to isACTActionAvailable should be false.
		widget.setACTActionVisible(false);
		Boolean isACTMember = true;
		when(mockUserBundle.getIsACTMember()).thenReturn(isACTMember);
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		widget.isACTActionAvailable(mockCallback);
		verify(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		verify(mockSessionStorage).setItem(SESSION_KEY_PREFIX + CURRENT_USER_ID, isACTMember.toString());
		verify(mockCallback).invoke(false);
	}

	@Test
	public void testIsMember() {
		// Is in ACT, but hide ACT UI. Answer to isACTMember should be true.
		widget.setACTActionVisible(false);
		Boolean isACTMember = true;
		when(mockUserBundle.getIsACTMember()).thenReturn(isACTMember);
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		widget.isACTMember(mockCallback);
		verify(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		verify(mockSessionStorage).setItem(SESSION_KEY_PREFIX + CURRENT_USER_ID, isACTMember.toString());
		verify(mockCallback).invoke(true);
	}

	@Test
	public void testRpcFailure() {
		String message = "an error occurred";
		Exception ex = new Exception(message);
		AsyncMockStubber.callFailureWith(ex).when(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		widget.isACTActionAvailable(mockCallback);
		verify(mockUserProfileClient).getMyOwnUserBundle(anyInt(), any(AsyncCallback.class));
		verify(mockJsniUtils).consoleError(message);
		verify(mockCallback).invoke(false);
	}
}
