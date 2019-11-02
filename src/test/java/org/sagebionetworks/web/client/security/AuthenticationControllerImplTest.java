package org.sagebionetworks.web.client.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.security.AuthenticationControllerImpl.USER_AUTHENTICATION_RECEIPT;
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
import org.sagebionetworks.repo.model.principal.EmailQuarantineReason;
import org.sagebionetworks.repo.model.principal.EmailQuarantineStatus;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SessionDetector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.QuarantinedEmailModal;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AuthenticationControllerImplTest {

	public static final String SESSION_TOKEN = "1111";
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
	@Mock
	AsyncCallback<UserProfile> mockUserProfileCallback;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	@Mock
	NotificationEmail mockNotificationEmail;
	@Mock
	QuarantinedEmailModal mockQuarantinedEmailModal;
	@Mock
	EmailQuarantineStatus mockEmailQuarantineStatus;
	UserProfile profile;
	UserSessionData usd;
	public static final String USER_ID = "98208";
	public static final String USER_AUTHENTICATION_RECEIPT_VALUE = "abc-def-ghi";

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		// by default, return a valid user session data if asked
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).initSession(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(SESSION_TOKEN).when(mockUserAccountService).getCurrentSessionToken(any(AsyncCallback.class));
		usd = new UserSessionData();
		profile = new UserProfile();
		profile.setOwnerId(USER_ID);
		usd.setProfile(profile);
		Session session = new Session();
		session.setSessionToken(SESSION_TOKEN);
		session.setAcceptsTermsOfUse(true);
		usd.setSession(session);
		AsyncMockStubber.callSuccessWith(usd).when(mockUserAccountService).getCurrentUserSessionData(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockNotificationEmail).when(mockJsClient).getNotificationEmail(any(AsyncCallback.class));
		when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
		authenticationController = new AuthenticationControllerImpl(mockUserAccountService, mockClientCache, mockCookieProvider, mockGinInjector, mockSynapseJSNIUtils);
		when(mockGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockGinInjector.getHeader()).thenReturn(mockHeader);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockGinInjector.getSessionDetector()).thenReturn(mockSessionDetector);
		when(mockGinInjector.getQuarantinedEmailModal()).thenReturn(mockQuarantinedEmailModal);
		when(mockNotificationEmail.getQuarantineStatus()).thenReturn(mockEmailQuarantineStatus);
	}

	@Test
	public void testLogout() {
		when(mockClientCache.get(USER_AUTHENTICATION_RECEIPT)).thenReturn(USER_AUTHENTICATION_RECEIPT_VALUE);

		authenticationController.logoutUser();

		// sets session cookie
		verify(mockJsClient).initSession(eq(WebConstants.EXPIRE_SESSION_TOKEN));
		verify(mockClientCache).clear();
		// verify that authentication receipt is restored
		verify(mockClientCache).put(eq(USER_AUTHENTICATION_RECEIPT), eq(USER_AUTHENTICATION_RECEIPT_VALUE), anyLong());
		verify(mockSessionDetector).initializeSessionTokenState();
		verify(mockGlobalApplicationState).refreshPage();
		verify(mockJsClient).logout();
		verify(mockSynapseJSNIUtils).setAnalyticsUserId("");
	}

	@Test
	public void testStoreLoginReceipt() {
		String receipt = "31416";
		authenticationController.storeAuthenticationReceipt(receipt);
		verify(mockClientCache).put(eq(USER_AUTHENTICATION_RECEIPT), eq(receipt), anyLong());
	}

	@Test
	public void testGetLoginRequest() {
		String username = "testusername";
		String password = "pw";

		LoginRequest request = authenticationController.getLoginRequest(username, password);
		assertNull(request.getAuthenticationReceipt());

		String cachedReceipt = "12345";
		when(mockClientCache.get(USER_AUTHENTICATION_RECEIPT)).thenReturn(cachedReceipt);
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
		when(mockClientCache.get(USER_AUTHENTICATION_RECEIPT)).thenReturn(oldAuthReceipt);
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setAcceptsTermsOfUse(true);
		loginResponse.setAuthenticationReceipt(newAuthReceipt);
		loginResponse.setSessionToken(newSessionToken);
		AsyncMockStubber.callSuccessWith(loginResponse).when(mockJsClient).login(any(LoginRequest.class), any(AsyncCallback.class));
		AsyncCallback loginCallback = mock(AsyncCallback.class);

		// make the actual call
		authenticationController.loginUser(username, password, loginCallback);

		// verify input arguments (including the cached receipt)
		ArgumentCaptor<LoginRequest> loginRequestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
		verify(mockJsClient).login(loginRequestCaptor.capture(), any(AsyncCallback.class));
		LoginRequest request = loginRequestCaptor.getValue();
		assertEquals(username, request.getUsername());
		assertEquals(password, request.getPassword());
		assertEquals(oldAuthReceipt, request.getAuthenticationReceipt());

		// verify the new receipt is cached
		verify(mockClientCache).put(eq(USER_AUTHENTICATION_RECEIPT), eq(newAuthReceipt), anyLong());

		verify(loginCallback).onSuccess(any(UserProfile.class));
		verify(mockSessionDetector).initializeSessionTokenState();
		verify(mockSynapseJSNIUtils).setAnalyticsUserId(USER_ID);
	}

	@Test
	public void testLoginUserNotAcceptedTermsOfUse() {
		usd.getSession().setAcceptsTermsOfUse(false);
		usd.setProfile(null); // profile is not returned in this case

		authenticationController.initializeFromExistingSessionCookie(mockUserProfileCallback);

		verify(mockUserAccountService).getCurrentUserSessionData(any(AsyncCallback.class));
		assertEquals(usd.getSession().getSessionToken(), authenticationController.getCurrentUserSessionToken());
		assertNull(authenticationController.getCurrentUserProfile());
		verify(mockSessionDetector).initializeSessionTokenState();
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place place = placeCaptor.getValue();
		assertTrue(place instanceof LoginPlace);
		assertEquals(LoginPlace.SHOW_TOU, ((LoginPlace) place).toToken());
	}

	@Test
	public void testLoginUserFailure() {
		Exception ex = new Exception("invalid login");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).login(any(LoginRequest.class), any(AsyncCallback.class));
		String username = "testusername";
		String password = "pw";
		AsyncCallback loginCallback = mock(AsyncCallback.class);

		// make the actual call
		authenticationController.loginUser(username, password, loginCallback);

		verify(loginCallback).onFailure(ex);
	}

	@Test
	public void testNoUserChange() {
		// if we invoke checkForUserChange(), if the user does not change we should update the session
		// cookie expiration (via the initSession call).
		authenticationController.initializeFromExistingSessionCookie(mockUserProfileCallback);
		verify(mockJsClient, never()).initSession(anyString(), any(AsyncCallback.class));

		authenticationController.checkForUserChange();

		verify(mockUserAccountService).getCurrentSessionToken(any(AsyncCallback.class));
		verify(mockJsClient).initSession(eq(SESSION_TOKEN), any(AsyncCallback.class));
	}

	// Note. If login when the stack is in READ_ONLY mode, then the widgets SynapseAlert should send
	// user to the Down page.

	@Test
	public void testCheckForQuarantinedEmailNullStatus() {
		when(mockNotificationEmail.getQuarantineStatus()).thenReturn(null);

		authenticationController.checkForQuarantinedEmail();

		verify(mockQuarantinedEmailModal, never()).show(anyString());
	}

	@Test
	public void testCheckForQuarantinedEmailTransientBounceStatus() {
		when(mockEmailQuarantineStatus.getReason()).thenReturn(EmailQuarantineReason.TRANSIENT_BOUNCE);

		authenticationController.checkForQuarantinedEmail();

		verify(mockQuarantinedEmailModal, never()).show(anyString());
	}

	@Test
	public void testCheckForQuarantinedEmailPermanentBounceStatus() {
		String detailedReason = "server does not recognize this email address";
		when(mockEmailQuarantineStatus.getReason()).thenReturn(EmailQuarantineReason.PERMANENT_BOUNCE);
		when(mockEmailQuarantineStatus.getReasonDetails()).thenReturn(detailedReason);

		authenticationController.checkForQuarantinedEmail();

		verify(mockQuarantinedEmailModal).show(detailedReason);
	}
}
