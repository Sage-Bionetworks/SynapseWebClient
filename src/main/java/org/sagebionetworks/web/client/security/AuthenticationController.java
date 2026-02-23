package org.sagebionetworks.web.client.security;

import com.google.common.util.concurrent.FluentFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.jsinterop.SynapseSessionManagerJs;

public interface AuthenticationController {
  /**
   * Bind to the JS session manager singleton. Must be called once after the JS
   * bundle has loaded.
   */
  void bindToSessionManager(SynapseSessionManagerJs manager);

  /**
   * Terminates the session of the current user
   */
  public void logoutUser();

  /**
   * Is the user logged in?
   *
   * @return true if the session manager reports isAuthenticated AND a cached UserProfile exists
   */
  public boolean isLoggedIn();

  /**
   * Get the current user's principal id. May return the session manager's userId
   * even before the full profile is fetched.
   */
  public String getCurrentUserPrincipalId();

  public String getCurrentUserRealmId();

  /**
   * Get the current access token from the session manager snapshot.
   */
  public String getCurrentUserAccessToken();

  /**
   * Get the cached UserProfile object, or null if not yet fetched / not logged in.
   */
  public UserProfile getCurrentUserProfile();

  public void updateCachedProfile(UserProfile updatedProfile);

  /**
   * Ask the session manager to refresh. The subscription callback handles
   * detecting changes and updating GWT state.
   */
  void checkForUserChange();

  void clearLocalStorage();

  /**
   * Returns a future that completes after the session manager refreshes and
   * the user profile is fetched (if authenticated).
   */
  FluentFuture<Void> getCheckForUserChangeFuture();

  /**
   * Get the underlying JS session manager.
   */
  SynapseSessionManagerJs getSessionManager();
}
