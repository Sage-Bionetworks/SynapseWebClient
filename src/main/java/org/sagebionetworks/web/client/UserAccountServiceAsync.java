package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.UserLoginBundle;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserAccountServiceAsync {

	void sendPasswordResetEmail(String emailAddress, AsyncCallback<Void> callback);

	void changePassword(String sessionToken, String newPassword, AsyncCallback<Void> callback);

	void initiateSession(LoginRequest loginRequest, AsyncCallback<LoginResponse> callback);
	
	void getUserSessionData(String sessionToken, AsyncCallback<UserSessionData> callback);
	
	void signTermsOfUse(String sessionToken, boolean acceptsTerms, AsyncCallback<Void> callback);

	void createUserStep1(String email, String portalEndpoint, AsyncCallback<Void> callback);
	void createUserStep2(String userName, String fName, String lName, String password, String emailValidationToken, AsyncCallback<String> callback);
	
	void terminateSession(String sessionToken, AsyncCallback<Void> callback);

	void getPrivateAuthServiceUrl(AsyncCallback<String> callback);

	void getPublicAuthServiceUrl(AsyncCallback<String> callback);
	
	void getTermsOfUse(AsyncCallback<String> callback);
	
	void getPublicAndAuthenticatedGroupPrincipalIds(AsyncCallback<PublicPrincipalIds> callback);
	
	void getStorageUsage(AsyncCallback<StorageUsageSummaryList> callback);

	void getUserLoginBundle(String sessionToken, AsyncCallback<UserLoginBundle> callback);
}
