package org.sagebionetworks.web.client.security;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFuture;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.EmailQuarantineReason;
import org.sagebionetworks.repo.model.principal.EmailQuarantineStatus;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.context.QueryClientProvider;
import org.sagebionetworks.web.client.jsinterop.SessionStateJsObject;
import org.sagebionetworks.web.client.jsinterop.SynapseSessionManagerJs;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;

/**
 * A thin GWT facade over {@code SynapseSessionManager} (JS singleton).
 *
 * Session state (token, userId, isAuthenticated) is owned by the JS session
 * manager. This class subscribes to state changes and handles the GWT-specific
 * concern of caching a {@link UserProfile}.
 *
 * CODE SPLITTING NOTE: this class should be kept small
 */
public class AuthenticationControllerImpl implements AuthenticationController {

  public static String FORCE_DISPLAY_ORIGINAL_COLUMN_NAMES =
    "force-display-original-column-names";

  private List<String> persistentLocalStorageKeys;
  private UserProfile currentUserProfile;
  private ClientCache localStorage;
  private SessionStorage sessionStorage;
  private PortalGinInjector ginInjector;
  private SynapseJSNIUtils jsniUtils;
  private QueryClient queryClient;

  private SynapseSessionManagerJs sessionManager;
  /** Tracks the last token we saw, so we can detect changes in the subscription callback. */
  private String lastKnownToken;

  @Inject
  public AuthenticationControllerImpl(
    ClientCache localStorage,
    SessionStorage sessionStorage,
    PortalGinInjector ginInjector,
    SynapseJSNIUtils jsniUtils,
    QueryClientProvider queryClientProvider
  ) {
    this.localStorage = localStorage;
    this.sessionStorage = sessionStorage;
    this.ginInjector = ginInjector;
    this.jsniUtils = jsniUtils;
    this.queryClient = queryClientProvider.getQueryClient();
    setPersistentLocalStorageKeys();
  }

  /**
   * Bind to the JS session manager singleton (window.SynapseSessionManager).
   * Must be called once after the JS bundle has loaded. Subscribes to state
   * changes so that GWT state stays in sync.
   */
  public void bindToSessionManager(SynapseSessionManagerJs manager) {
    this.sessionManager = manager;
    this.lastKnownToken = getTokenFromSnapshot();

    manager.subscribe(() -> onSessionStateChanged());
  }

  /** Called by the JS session manager subscription whenever state changes. */
  private void onSessionStateChanged() {
    SessionStateJsObject state = sessionManager.getSnapshot();
    String newToken = state.token;

    boolean tokenChanged = !Objects.equals(lastKnownToken, newToken);
    lastKnownToken = newToken;

    if (tokenChanged) {
      resetQueryClientCache();
    }

    if (state.isAuthenticated) {
      if (tokenChanged || currentUserProfile == null) {
        // Fetch the user profile for the (potentially new) authenticated user
        fetchUserProfile();
      }
    } else {
      if (currentUserProfile != null) {
        currentUserProfile = null;
      }
    }

    // Always keep the UI in sync
    ginInjector
      .getGlobalApplicationState()
      .synchronizeReactContextWithGlobalStore();
    ginInjector.getFooter().refresh();
    ginInjector.getHeader().refresh();
  }

  private void fetchUserProfile() {
    ginInjector
      .getSynapseJavascriptClient()
      .getMyUserProfile()
      .addCallback(
        new FutureCallback<UserProfile>() {
          @Override
          public void onSuccess(UserProfile profile) {
            currentUserProfile = profile;
          }

          @Override
          public void onFailure(Throwable t) {
            currentUserProfile = null;
            if (
              t instanceof ForbiddenException &&
              t.getMessage().toLowerCase().contains("terms of service")
            ) {
              ginInjector
                .getGlobalApplicationState()
                .getPlaceChanger()
                .goTo(new LoginPlace(LoginPlace.SHOW_TOU));
            } else {
              jsniUtils.consoleError(t);
            }
          }
        },
        directExecutor()
      );
  }

  private String getTokenFromSnapshot() {
    if (sessionManager == null) {
      return null;
    }
    return sessionManager.getSnapshot().token;
  }

  public void resetQueryClientCache() {
    queryClient.resetQueries();
  }

  public void checkForQuarantinedEmail() {
    ginInjector
      .getSynapseJavascriptClient()
      .getNotificationEmail(
        new AsyncCallback<NotificationEmail>() {
          @Override
          public void onSuccess(NotificationEmail notificationEmailStatus) {
            EmailQuarantineStatus status =
              notificationEmailStatus.getQuarantineStatus();
            if (isQuarantined(status)) {
              ginInjector
                .getQuarantinedEmailModal()
                .show(status.getReasonDetails());
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            jsniUtils.consoleError(caught);
          }
        }
      );
  }

  public static boolean isQuarantined(EmailQuarantineStatus status) {
    return (
      status != null &&
      EmailQuarantineReason.PERMANENT_BOUNCE.equals(status.getReason())
    );
  }

  public void clearLocalStorage() {
    Map<String, String> storedKeyValues = new HashMap<String, String>();
    for (String key : persistentLocalStorageKeys) {
      if (localStorage.contains(key)) {
        storedKeyValues.put(key, localStorage.get(key));
      }
    }
    localStorage.clear();
    for (String key : storedKeyValues.keySet()) {
      localStorage.put(
        key,
        storedKeyValues.get(key),
        DateTimeUtilsImpl.getYearFromNow().getTime()
      );
    }
  }

  @Override
  public void logoutUser() {
    // Clear local/session storage
    clearLocalStorage();
    Place lastPlace = ginInjector.getGlobalApplicationState().getLastPlace();
    sessionStorage.clear();
    ginInjector.getGlobalApplicationState().setLastPlace(lastPlace);

    currentUserProfile = null;

    // Delegate to the JS session manager to clear the session (signs out, initializes anonymous session)
    sessionManager
      .clearSession()
      .then(v -> {
        ginInjector
          .getGlobalApplicationState()
          .synchronizeReactContextWithGlobalStore();
        resetQueryClientCache();
        ginInjector.getFooter().refresh();
        ginInjector.getHeader().refresh();
        ginInjector.getGlobalApplicationState().refreshPage();
        return null;
      });
  }

  @Override
  public void updateCachedProfile(UserProfile updatedProfile) {
    currentUserProfile = updatedProfile;
  }

  @Override
  public boolean isLoggedIn() {
    SessionStateJsObject state = sessionManager.getSnapshot();
    return state.isAuthenticated;
  }

  @Override
  public String getCurrentUserPrincipalId() {
    // Prefer the session manager's userId (available immediately, no profile needed)
    SessionStateJsObject state = sessionManager.getSnapshot();
    if (state.userId != null) {
      return state.userId;
    }
    if (currentUserProfile != null) {
      return currentUserProfile.getOwnerId();
    }
    return null;
  }

  @Override
  public String getCurrentUserRealmId() {
    // Prefer the session manager's userId (available immediately, no profile needed)
    SessionStateJsObject state = sessionManager.getSnapshot();
    if (state.realmId != null) {
      return state.realmId;
    }
    return null;
  }

  @Override
  public UserProfile getCurrentUserProfile() {
    return currentUserProfile;
  }

  @Override
  public String getCurrentUserAccessToken() {
    return getTokenFromSnapshot();
  }

  @Override
  public void checkForUserChange() {
    // Delegate to the JS session manager. The subscription callback
    // (onSessionStateChanged) handles detecting changes and updating GWT state.
    sessionManager.refreshSession();
  }

  @Override
  public FluentFuture<Void> getCheckForUserChangeFuture() {
    return getFuture(cb -> {
      sessionManager
        .refreshSession()
        .then(v -> {
          // After refresh, fetch the profile if authenticated
          SessionStateJsObject state = sessionManager.getSnapshot();
          lastKnownToken = state.token;
          if (state.isAuthenticated) {
            ginInjector
              .getSynapseJavascriptClient()
              .getMyUserProfile()
              .addCallback(
                new FutureCallback<UserProfile>() {
                  @Override
                  public void onSuccess(UserProfile profile) {
                    currentUserProfile = profile;
                    checkForQuarantinedEmail();
                    cb.onSuccess(null);
                  }

                  @Override
                  public void onFailure(Throwable t) {
                    currentUserProfile = null;
                    if (
                      t instanceof ForbiddenException &&
                      t.getMessage().toLowerCase().contains("terms of service")
                    ) {
                      ginInjector
                        .getGlobalApplicationState()
                        .getPlaceChanger()
                        .goTo(new LoginPlace(LoginPlace.SHOW_TOU));
                    }
                    cb.onSuccess(null);
                  }
                },
                directExecutor()
              );
          } else {
            currentUserProfile = null;
            cb.onSuccess(null);
          }
          return null;
        });
    });
  }

  public SynapseSessionManagerJs getSessionManager() {
    return sessionManager;
  }

  private void setPersistentLocalStorageKeys() {
    String[] swcPersistentLocalStorageKeys = new String[] {
      FORCE_DISPLAY_ORIGINAL_COLUMN_NAMES,
    };
    String[] srcPersistentLocalStorageKeys =
      jsniUtils.getSrcPersistentLocalStorageKeys();

    this.persistentLocalStorageKeys = new ArrayList<String>();

    for (int i = 0; i < srcPersistentLocalStorageKeys.length; i++) {
      this.persistentLocalStorageKeys.add(srcPersistentLocalStorageKeys[i]);
    }

    for (int i = 0; i < swcPersistentLocalStorageKeys.length; i++) {
      this.persistentLocalStorageKeys.add(swcPersistentLocalStorageKeys[i]);
    }
  }
}
