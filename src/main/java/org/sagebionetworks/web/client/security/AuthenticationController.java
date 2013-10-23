package org.sagebionetworks.web.client.security;

import org.sagebionetworks.repo.model.UserSessionData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthenticationController {
		
	/**
	 * Login the user
	 */
	public void loginUser(String username, String password, boolean explicitlyAcceptsTermsOfUse, AsyncCallback<String> callback);
	
	/**
	 * Logs in the user represented by the token
	 */
	public void loginUser(String token, AsyncCallback<String> callback);
	
	/**
	 * Sets the current user 
	 */
	public void loginUserSSO(String token, AsyncCallback<String> callback);
	
	/**
	 * Terminates the session of the current user
	 */
	public void logoutUser();
	
	/**
	 * Uses the session token to accept the terms of use
	 */
	public void acceptTermsOfUse(String token, AsyncCallback<String> callback,  boolean isSSO);
	

	/**
	 * Is the user logged in?
	 */
	public boolean isLoggedIn();
	
	/**
	 * Get the OwnerId/Principal id out of the UserProfile / UserSessionData in a lightweight fashion
	 */
	public String getCurrentUserPrincipalId();

	/**
	 * Get the current session token, if there is one
	 */
	public String getCurrentUserSessionToken();
	
	/**
	 * Get the current SSO status
	 */
	public boolean getCurrentUserIsSSO();		
	
	/**
	 * Get the UserSessionData object 
	 */
	public UserSessionData getCurrentUserSessionData();

	
	
	/**
	 * Redownload the user's session data 
	 */
	void reloadUserSessionData();

	public void getTermsOfUse(AsyncCallback<String> callback);
	
}
