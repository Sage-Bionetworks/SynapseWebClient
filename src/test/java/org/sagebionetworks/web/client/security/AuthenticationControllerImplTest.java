package org.sagebionetworks.web.client.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.promise.IThenable;
import elemental2.promise.Promise;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.EmailQuarantineReason;
import org.sagebionetworks.repo.model.principal.EmailQuarantineStatus;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.context.QueryClientProvider;
import org.sagebionetworks.web.client.jsinterop.SessionStateJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseSessionManagerJs;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;
import org.sagebionetworks.web.client.widget.QuarantinedEmailModal;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationControllerImplTest {

  public static final String ACCESS_TOKEN = "1111";
  public static final String USER_ID = "98208";
  AuthenticationControllerImpl authenticationController;

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
  Footer mockFooter;

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

  @Mock
  SynapseSessionManagerJs mockSessionManager;

  @Mock
  Promise<Void> mockClearSessionPromise;

  @Captor
  ArgumentCaptor<
    IThenable.ThenOnFulfilledCallbackFn<Void, ?>
  > clearSessionThenCaptor;

  @Captor
  ArgumentCaptor<Place> placeCaptor;

  UserProfile profile;

  public static final String ORIENTATION_BANNER_STORAGE_VALUE = "true";
  public static final String ORIENTATION_BANNER_DISMISSED =
    "orientation_banner_dismissed";
  public static final String ORIENTATION_BANNER_NOT_DISMISSED =
    "orientation_banner_not_dismissed";

  @Before
  public void before() {
    profile = new UserProfile();
    profile.setOwnerId(USER_ID);
    when(mockQueryClientProvider.getQueryClient()).thenReturn(mockQueryClient);
    when(mockSynapseJSNIUtils.getSrcPersistentLocalStorageKeys())
      .thenReturn(
        new String[] {
          ORIENTATION_BANNER_DISMISSED,
          ORIENTATION_BANNER_NOT_DISMISSED,
        }
      );
    authenticationController =
      new AuthenticationControllerImpl(
        mockClientCache,
        mockSessionStorage,
        mockGinInjector,
        mockSynapseJSNIUtils,
        mockQueryClientProvider
      );

    // Set up session manager mock with an authenticated state by default
    SessionStateJsObject authenticatedState = createSessionState(
      ACCESS_TOKEN,
      USER_ID,
      true,
      true
    );
    when(mockSessionManager.getSnapshot()).thenReturn(authenticatedState);
    authenticationController.bindToSessionManager(mockSessionManager);
  }

  private static SessionStateJsObject createSessionState(
    String token,
    String userId,
    boolean isAuthenticated,
    boolean hasInitializedSession
  ) {
    SessionStateJsObject state = new SessionStateJsObject();
    state.token = token;
    state.userId = userId;
    state.isAuthenticated = isAuthenticated;
    state.hasInitializedSession = hasInitializedSession;
    return state;
  }

  @Test
  public void testIsLoggedInWhenAuthenticatedWithProfile() {
    authenticationController.updateCachedProfile(profile);
    assertTrue(authenticationController.isLoggedIn());
  }

  @Test
  public void testIsLoggedInWhenNotAuthenticated() {
    SessionStateJsObject anonState = createSessionState(
      null,
      null,
      false,
      true
    );
    when(mockSessionManager.getSnapshot()).thenReturn(anonState);
    assertFalse(authenticationController.isLoggedIn());
  }

  @Test
  public void testGetCurrentUserPrincipalIdFromSessionManager() {
    // userId comes from the session manager snapshot
    assertEquals(USER_ID, authenticationController.getCurrentUserPrincipalId());
  }

  @Test
  public void testGetCurrentUserPrincipalIdFallsBackToProfile() {
    SessionStateJsObject noUserIdState = createSessionState(
      ACCESS_TOKEN,
      null,
      true,
      true
    );
    when(mockSessionManager.getSnapshot()).thenReturn(noUserIdState);
    authenticationController.updateCachedProfile(profile);
    assertEquals(USER_ID, authenticationController.getCurrentUserPrincipalId());
  }

  @Test
  public void testGetCurrentUserPrincipalIdReturnsNullWhenAnonymous() {
    SessionStateJsObject anonState = createSessionState(
      null,
      null,
      false,
      true
    );
    when(mockSessionManager.getSnapshot()).thenReturn(anonState);
    assertNull(authenticationController.getCurrentUserPrincipalId());
  }

  @Test
  public void testGetCurrentUserAccessTokenReadsFromSnapshot() {
    assertEquals(
      ACCESS_TOKEN,
      authenticationController.getCurrentUserAccessToken()
    );
  }

  @Test
  public void testGetCurrentUserAccessTokenReturnsNullWhenAnonymous() {
    SessionStateJsObject anonState = createSessionState(
      null,
      null,
      false,
      true
    );
    when(mockSessionManager.getSnapshot()).thenReturn(anonState);
    assertNull(authenticationController.getCurrentUserAccessToken());
  }

  @Test
  public void testLogout() {
    when(mockGinInjector.getGlobalApplicationState())
      .thenReturn(mockGlobalApplicationState);
    when(mockGinInjector.getHeader()).thenReturn(mockHeader);
    when(mockGinInjector.getFooter()).thenReturn(mockFooter);
    when(mockGlobalApplicationState.getLastPlace()).thenReturn(mockPlace);
    when(mockClientCache.contains(ORIENTATION_BANNER_DISMISSED))
      .thenReturn(true);
    when(mockClientCache.get(ORIENTATION_BANNER_DISMISSED))
      .thenReturn(ORIENTATION_BANNER_STORAGE_VALUE);
    when(mockSessionManager.clearSession()).thenReturn(mockClearSessionPromise);

    authenticationController.updateCachedProfile(profile);
    authenticationController.logoutUser();

    // Verify the then callback is registered and invoke it to simulate resolution
    verify(mockClearSessionPromise).then(clearSessionThenCaptor.capture());
    clearSessionThenCaptor.getValue().onInvoke(null);

    // Clears local storage (preserving persistent keys)
    verify(mockClientCache).clear();
    verify(mockSessionStorage).clear();
    // verify that dismissed orientation banner is restored
    verify(mockClientCache)
      .put(
        eq(ORIENTATION_BANNER_DISMISSED),
        eq(ORIENTATION_BANNER_STORAGE_VALUE),
        anyLong()
      );
    // verify that non-dismissed orientation banner is not restored
    verify(mockClientCache, never())
      .put(
        eq(ORIENTATION_BANNER_NOT_DISMISSED),
        eq(ORIENTATION_BANNER_STORAGE_VALUE),
        anyLong()
      );
    // verify last place is restored
    verify(mockGlobalApplicationState).setLastPlace(mockPlace);
    // Delegates to JS session manager
    verify(mockSessionManager).clearSession();
    // Profile should be cleared
    assertNull(authenticationController.getCurrentUserProfile());
    // Verify post-clearSession actions from the .then() callback
    verify(mockGlobalApplicationState).synchronizeReactContextWithGlobalStore();
    verify(mockQueryClient).resetQueries();
    verify(mockFooter).refresh();
    verify(mockHeader).refresh();
    verify(mockGlobalApplicationState).refreshPage();
  }

  @Test
  public void testCheckForUserChangeDelegatesToSessionManager() {
    authenticationController.checkForUserChange();
    verify(mockSessionManager).refreshSession();
  }

  @Test
  public void testCheckForQuarantinedEmailNullStatus() {
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    AsyncMockStubber
      .callSuccessWith(mockNotificationEmail)
      .when(mockJsClient)
      .getNotificationEmail(any(AsyncCallback.class));
    when(mockNotificationEmail.getQuarantineStatus()).thenReturn(null);

    authenticationController.checkForQuarantinedEmail();

    verify(mockQuarantinedEmailModal, never()).show(anyString());
  }

  @Test
  public void testCheckForQuarantinedEmailTransientBounceStatus() {
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    AsyncMockStubber
      .callSuccessWith(mockNotificationEmail)
      .when(mockJsClient)
      .getNotificationEmail(any(AsyncCallback.class));
    when(mockNotificationEmail.getQuarantineStatus())
      .thenReturn(mockEmailQuarantineStatus);
    when(mockEmailQuarantineStatus.getReason())
      .thenReturn(EmailQuarantineReason.TRANSIENT_BOUNCE);

    authenticationController.checkForQuarantinedEmail();

    verify(mockQuarantinedEmailModal, never()).show(anyString());
  }

  @Test
  public void testCheckForQuarantinedEmailPermanentBounceStatus() {
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    AsyncMockStubber
      .callSuccessWith(mockNotificationEmail)
      .when(mockJsClient)
      .getNotificationEmail(any(AsyncCallback.class));
    when(mockNotificationEmail.getQuarantineStatus())
      .thenReturn(mockEmailQuarantineStatus);
    when(mockGinInjector.getQuarantinedEmailModal())
      .thenReturn(mockQuarantinedEmailModal);
    String detailedReason = "server does not recognize this email address";
    when(mockEmailQuarantineStatus.getReason())
      .thenReturn(EmailQuarantineReason.PERMANENT_BOUNCE);
    when(mockEmailQuarantineStatus.getReasonDetails())
      .thenReturn(detailedReason);

    authenticationController.checkForQuarantinedEmail();

    verify(mockQuarantinedEmailModal).show(detailedReason);
  }

  @Test
  public void testGetSessionManager() {
    assertEquals(
      mockSessionManager,
      authenticationController.getSessionManager()
    );
  }
}
