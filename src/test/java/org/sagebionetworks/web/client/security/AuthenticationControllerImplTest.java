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
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
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
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.context.QueryClientProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.QuarantinedEmailModal;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class AuthenticationControllerImplTest {

  public static final String ACCESS_TOKEN = "1111";
  AuthenticationControllerImpl authenticationController;

  @Mock
  CookieProvider mockCookieProvider;

  @Mock
  UserAccountServiceAsync mockUserAccountService;

  @Mock
  ClientCache mockClientCache;

  @Mock
  SessionStorage mockSessionStorage;

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

  @Mock
  Place mockPlace;

  @Mock
  QueryClientProvider mockQueryClientProvider;

  @Mock
  QueryClient mockQueryClient;

  UserProfile profile;
  public static final String USER_ID = "98208";
  public static final String USER_AUTHENTICATION_RECEIPT_VALUE = "abc-def-ghi";

  @Before
  public void before() throws JSONObjectAdapterException {
    MockitoAnnotations.initMocks(this);
    // by default, return a valid user session data if asked
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockJsClient)
      .initSession(anyString(), any(AsyncCallback.class));
    profile = new UserProfile();
    profile.setOwnerId(USER_ID);
    when(mockJsClient.getAccessToken()).thenReturn(getDoneFuture(ACCESS_TOKEN));
    AsyncMockStubber
      .callSuccessWith(profile)
      .when(mockUserAccountService)
      .getMyProfile(any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockNotificationEmail)
      .when(mockJsClient)
      .getNotificationEmail(any(AsyncCallback.class));
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    when(mockQueryClientProvider.getQueryClient()).thenReturn(mockQueryClient);
    authenticationController =
      new AuthenticationControllerImpl(
        mockUserAccountService,
        mockClientCache,
        mockSessionStorage,
        mockCookieProvider,
        mockGinInjector,
        mockSynapseJSNIUtils,
        mockQueryClientProvider
      );
    when(mockGinInjector.getGlobalApplicationState())
      .thenReturn(mockGlobalApplicationState);
    when(mockGinInjector.getHeader()).thenReturn(mockHeader);
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    when(mockGinInjector.getSessionDetector()).thenReturn(mockSessionDetector);
    when(mockGinInjector.getQuarantinedEmailModal())
      .thenReturn(mockQuarantinedEmailModal);
    when(mockNotificationEmail.getQuarantineStatus())
      .thenReturn(mockEmailQuarantineStatus);
  }

  @Test
  public void testLogout() {
    when(mockGlobalApplicationState.getLastPlace()).thenReturn(mockPlace);
    when(mockClientCache.get(USER_AUTHENTICATION_RECEIPT))
      .thenReturn(USER_AUTHENTICATION_RECEIPT_VALUE);

    authenticationController.logoutUser();

    // sets session cookie
    verify(mockJsClient)
      .initSession(
        eq(WebConstants.EXPIRE_SESSION_TOKEN),
        any(AsyncCallback.class)
      );
    verify(mockClientCache).clear();
    verify(mockSessionStorage).clear();
    // verify that authentication receipt is restored
    verify(mockClientCache)
      .put(
        eq(USER_AUTHENTICATION_RECEIPT),
        eq(USER_AUTHENTICATION_RECEIPT_VALUE),
        anyLong()
      );
    // verify last place is restored
    verify(mockGlobalApplicationState).setLastPlace(mockPlace);
    verify(mockSessionDetector).initializeAccessTokenState();
    verify(mockGlobalApplicationState).refreshPage();
    verify(mockSynapseJSNIUtils).setAnalyticsUserId("");
    verify(mockQueryClient).clear();
  }

  @Test
  public void testStoreLoginReceipt() {
    String receipt = "31416";
    authenticationController.storeAuthenticationReceipt(receipt);
    verify(mockClientCache)
      .put(eq(USER_AUTHENTICATION_RECEIPT), eq(receipt), anyLong());
  }

  @Test
  public void testGetLoginRequest() {
    String username = "testusername";
    String password = "pw";

    LoginRequest request = authenticationController.getLoginRequest(
      username,
      password
    );
    assertNull(request.getAuthenticationReceipt());

    String cachedReceipt = "12345";
    when(mockClientCache.get(USER_AUTHENTICATION_RECEIPT))
      .thenReturn(cachedReceipt);
    request = authenticationController.getLoginRequest(username, password);
    assertEquals(cachedReceipt, request.getAuthenticationReceipt());
  }

  @Test
  public void testLoginUser() {
    String username = "testusername";
    String password = "pw";
    String oldAuthReceipt = "1234";
    String newAccessToken = "abcdzxcvbn";
    String newAuthReceipt = "5678";
    when(mockClientCache.get(USER_AUTHENTICATION_RECEIPT))
      .thenReturn(oldAuthReceipt);
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setAcceptsTermsOfUse(true);
    loginResponse.setAuthenticationReceipt(newAuthReceipt);
    loginResponse.setAccessToken(newAccessToken);
    AsyncMockStubber
      .callSuccessWith(loginResponse)
      .when(mockJsClient)
      .login(any(LoginRequest.class), any(AsyncCallback.class));
    AsyncCallback loginCallback = mock(AsyncCallback.class);

    // make the actual call
    authenticationController.loginUser(username, password, loginCallback);

    // verify input arguments (including the cached receipt)
    ArgumentCaptor<LoginRequest> loginRequestCaptor = ArgumentCaptor.forClass(
      LoginRequest.class
    );
    verify(mockJsClient)
      .login(loginRequestCaptor.capture(), any(AsyncCallback.class));
    LoginRequest request = loginRequestCaptor.getValue();
    assertEquals(username, request.getUsername());
    assertEquals(password, request.getPassword());
    assertEquals(oldAuthReceipt, request.getAuthenticationReceipt());

    // verify the new receipt is cached
    verify(mockClientCache)
      .put(eq(USER_AUTHENTICATION_RECEIPT), eq(newAuthReceipt), anyLong());

    verify(loginCallback).onSuccess(any(UserProfile.class));
    verify(mockSessionDetector).initializeAccessTokenState();
    verify(mockSynapseJSNIUtils).setAnalyticsUserId(USER_ID);
    verify(mockQueryClient).clear();
  }

  @Test
  public void testLoginUserNotAcceptedTermsOfUse() {
    // access token is returned without error (it's set and valid), but getMyProfile() fails with a special ForbiddenException
    AsyncMockStubber
      .callFailureWith(
        new ForbiddenException("Terms of use have not been signed.")
      )
      .when(mockUserAccountService)
      .getMyProfile(any(AsyncCallback.class));

    authenticationController.initializeFromExistingAccessTokenCookie(
      mockUserProfileCallback
    );

    assertNull(authenticationController.getCurrentUserProfile());
    verify(mockSessionDetector).initializeAccessTokenState();
    verify(mockQueryClient).clear();
    verify(mockPlaceChanger).goTo(placeCaptor.capture());
    Place place = placeCaptor.getValue();
    assertTrue(place instanceof LoginPlace);
    assertEquals(LoginPlace.SHOW_TOU, ((LoginPlace) place).toToken());
  }

  @Test
  public void testLoginUserFailure() {
    Exception ex = new Exception("invalid login");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockJsClient)
      .login(any(LoginRequest.class), any(AsyncCallback.class));
    String username = "testusername";
    String password = "pw";
    AsyncCallback loginCallback = mock(AsyncCallback.class);

    // make the actual call
    authenticationController.loginUser(username, password, loginCallback);

    verify(loginCallback).onFailure(ex);
  }

  // Note: We do not update the access token cookie expiration (since the access token will expire)

  @Test
  public void testCheckForUserChangeWithoutNetwork() {
    // if we invoke checkForUserChange(), if the user does not change we should update the session
    authenticationController.initializeFromExistingAccessTokenCookie(
      mockUserProfileCallback
    );
    verify(mockJsClient, never())
      .initSession(anyString(), any(AsyncCallback.class));
    verify(mockQueryClient).clear();
    Exception scEx = new StatusCodeException(0, "0 ");
    when(mockJsClient.getAccessToken()).thenReturn(getFailedFuture(scEx));

    authenticationController.checkForUserChange();

    verify(mockSynapseJSNIUtils).consoleError(scEx);

    Exception unknownEx = new UnknownErrorException(
      "Unexpected transient error"
    );
    when(mockJsClient.getAccessToken()).thenReturn(getFailedFuture(unknownEx));

    authenticationController.checkForUserChange();
    verify(mockSynapseJSNIUtils).consoleError(unknownEx);
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
    when(mockEmailQuarantineStatus.getReason())
      .thenReturn(EmailQuarantineReason.TRANSIENT_BOUNCE);

    authenticationController.checkForQuarantinedEmail();

    verify(mockQuarantinedEmailModal, never()).show(anyString());
  }

  @Test
  public void testCheckForQuarantinedEmailPermanentBounceStatus() {
    String detailedReason = "server does not recognize this email address";
    when(mockEmailQuarantineStatus.getReason())
      .thenReturn(EmailQuarantineReason.PERMANENT_BOUNCE);
    when(mockEmailQuarantineStatus.getReasonDetails())
      .thenReturn(detailedReason);

    authenticationController.checkForQuarantinedEmail();

    verify(mockQuarantinedEmailModal).show(detailedReason);
  }
}
