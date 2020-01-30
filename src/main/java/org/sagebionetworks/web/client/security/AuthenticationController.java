package org.sagebionetworks.web.client.security;

import org.sagebionetworks.repo.model.UserProfile;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthenticationController {

	/**
	 * Login the user
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public void loginUser(String username, String password, AsyncCallback<UserProfile> callback);

	/**
	 * sets a new session token
	 * 
	 * @param token
	 */
	void setNewSessionToken(String token, final AsyncCallback<UserProfile> callback);

	/**
	 * attempts to load from an existing session cookie
	 * 
	 * @param callback
	 */
	void initializeFromExistingSessionCookie(final AsyncCallback<UserProfile> callback);

	/**
	 * Terminates the session of the current user
	 */
	public void logoutUser();


	/**
	 * Is the user logged in?
	 * 
	 * @return
	 */
	public boolean isLoggedIn();

	/**
	 * Get the OwnerId/Principal id out of the UserProfile / UserSessionData in a lightweight fashion
	 * 
	 * @return
	 */
	public String getCurrentUserPrincipalId();

	/**
	 * Get the current session token, if there is one
	 * 
	 * @return
	 */
	public String getCurrentUserSessionToken();

	/**
	 * Get the UserProfile object
	 * 
	 * @return
	 */
	public UserProfile getCurrentUserProfile();

	/**
	 * Signs the terms of use for a user
	 */
	public void signTermsOfUse(boolean accepted, AsyncCallback<Void> callback);

	public void updateCachedProfile(UserProfile updatedProfile);

	void checkForUserChange();

	// refreshes the session token (iff logged in)
	void refreshSessionToken();
}
