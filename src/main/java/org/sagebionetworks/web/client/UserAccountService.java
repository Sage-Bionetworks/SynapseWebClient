package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.UserLoginBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("users")
public interface UserAccountService extends RemoteService {	

	public void sendPasswordResetEmail(String emailAddress) throws RestServiceException;
	
	public void changePassword(String sessionToken, String newPassword) throws RestServiceException;

	public LoginResponse initiateSession(LoginRequest loginRequest) throws RestServiceException;
	
	public UserSessionData getUserSessionData(String sessionToken) throws RestServiceException;
	
	public void signTermsOfUse(String sessionToken, boolean acceptsTerms) throws RestServiceException;

	public void createUserStep1(String email, String portalEndpoint) throws RestServiceException;
	public String createUserStep2(String userName, String fName, String lName, String password, String emailValidationToken) throws RestServiceException;
	
	public void terminateSession(String sessionToken) throws RestServiceException;
	
	public String getPrivateAuthServiceUrl();
	
	public String getPublicAuthServiceUrl();
	
	public String getTermsOfUse();
	
	public PublicPrincipalIds getPublicAndAuthenticatedGroupPrincipalIds();
	
	public StorageUsageSummaryList getStorageUsage();

}
