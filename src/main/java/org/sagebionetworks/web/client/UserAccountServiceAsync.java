package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.users.UserRegistration;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserAccountServiceAsync {

	void sendPasswordResetEmail(String emailAddress, AsyncCallback<Void> callback);

	void changePassword(String sessionToken, String newPassword, AsyncCallback<Void> callback);

	void initiateSession(String username, String password, AsyncCallback<String> callback);
	
	void getUserSessionData(String sessionToken, AsyncCallback<String> callback);
	
	void signTermsOfUse(String sessionToken, boolean acceptsTerms, AsyncCallback<Void> callback);

	void createUser(UserRegistration userInfo, AsyncCallback<Void> callback);
	
	void terminateSession(String sessionToken, AsyncCallback<Void> callback);

	void getPrivateAuthServiceUrl(AsyncCallback<String> callback);

	void getPublicAuthServiceUrl(AsyncCallback<String> callback);
	
	void getTermsOfUse(AsyncCallback<String> callback);
	
	void getStorageUsage(AsyncCallback<String> callback);
}
