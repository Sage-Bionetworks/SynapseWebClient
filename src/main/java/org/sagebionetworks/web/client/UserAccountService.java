package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserRegistration;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("users")
public interface UserAccountService extends RemoteService {	

	public void sendPasswordResetEmail(String emailAddress) throws RestServiceException;
	
	public void changePassword(String sessionToken, String newPassword);

	public String initiateSession(String username, String password) throws RestServiceException;
	
	public String getUserSessionData(String sessionToken) throws RestServiceException;
	
	public void signTermsOfUse(String sessionToken, boolean acceptsTerms) throws RestServiceException;

	public void createUser(UserRegistration userInfo) throws RestServiceException;
	
	public void terminateSession(String sessionToken) throws RestServiceException;
	
	public String getPrivateAuthServiceUrl();
	
	public String getPublicAuthServiceUrl();
	
	public String getTermsOfUse();
	
	public String getStorageUsage();
}
