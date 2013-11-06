package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserRegistration;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserAccountServiceAsync {

	void sendPasswordResetEmail(String emailAddress, AsyncCallback<Void> callback);
	
	void sendSetApiPasswordEmail(AsyncCallback<Void> callback);

	void setPassword(String newPassword, AsyncCallback<Void> callback);

	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	void initiateSession(String username, String password, boolean explicitlyAcceptsTermsOfUse, AsyncCallback<String> callback);

	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	void getUser(String sessionToken, AsyncCallback<String> callback);	

	void createUser(UserRegistration userInfo, AsyncCallback<Void> callback);
	
	void terminateSession(String sessionToken, AsyncCallback<Void> callback);

	void getPrivateAuthServiceUrl(AsyncCallback<String> callback);

	void getPublicAuthServiceUrl(AsyncCallback<String> callback);
	
	void getPublicAndAuthenticatedGroupPrincipalIds(AsyncCallback<PublicPrincipalIds> callback);
	
	/**
	 * This needs to be replaced with a Synapse Java Client call
	 */
	@Deprecated
	void getTermsOfUse(AsyncCallback<String> callback);

	void setRegistrationUserPassword(String registrationToken, String newPassword, AsyncCallback<Void> callback);
	
	void changeEmailAddress(String changeEmailToken, String newPassword, AsyncCallback<Void> callback);
	
	/**
	 * This needs to be replaced with a Synapse Java Client call
	 * return the StorageUsageSummaryList json for the current user
	 */
	void getStorageUsage(AsyncCallback<String> callback);
}
