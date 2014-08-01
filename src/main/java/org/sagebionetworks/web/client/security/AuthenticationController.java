package org.sagebionetworks.web.client.security;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthenticationController {
		
	/**
	 * Login the user
	 * @param username
	 * @param password
	 * @return
	 */
	public void loginUser(String username, String password, AsyncCallback<String> callback);
	
	/**
	 * revalidates the given session token
	 * @param token
	 */
	public void revalidateSession(String token, AsyncCallback<String> callback);
	
	/**
	 * Terminates the session of the current user
	 */
	public void logoutUser();
	

	/**
	 * Is the user logged in?
	 * @return
	 */
	public boolean isLoggedIn();
	
	/**
	 * Get the OwnerId/Principal id out of the UserProfile / UserSessionData in a lightweight fashion
	 * @return
	 */
	public String getCurrentUserPrincipalId();

	/**
	 * Get the current session token, if there is one
	 * @return
	 */
	public String getCurrentUserSessionToken();
	
	/**
	 * Get the UserSessionData object 
	 * @return
	 */
	public UserSessionData getCurrentUserSessionData();

	/**
	 * Redownload the user's session data 
	 */
	void reloadUserSessionData(AsyncCallback<String> callback);

	public void getTermsOfUse(AsyncCallback<String> callback);
	
	/**
	 * Signs the terms of use for a user
	 */
	public void signTermsOfUse(boolean accepted, AsyncCallback<Void> callback);
	
	public void updateCachedProfile(UserProfile updatedProfile);
}
