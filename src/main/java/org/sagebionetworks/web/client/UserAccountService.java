package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.security.AuthenticationException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserRegistration;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("users")
public interface UserAccountService extends RemoteService {	

	public void sendPasswordResetEmail(String emailAddress) throws RestServiceException;
	
	void sendSetApiPasswordEmail() throws RestServiceException;
	
	public void setPassword(String newPassword);

	public String initiateSession(String username, String password, boolean explicitlyAcceptsTermsOfUse) throws RestServiceException;
	
	public String getUser(String sessionToken) throws AuthenticationException, RestServiceException;

	public void createUser(UserRegistration userInfo) throws RestServiceException;
			
	public void updateUser(String firstName, String lastName, String displayName) throws RestServiceException;
	
	public void terminateSession(String sessionToken) throws RestServiceException;
	
	public String getPrivateAuthServiceUrl();
	
	public String getPublicAuthServiceUrl();
	
	public String getFastPassSupportUrl() throws RestServiceException;
	
	public String getSynapseWebUrl();

	public String getTermsOfUse();
	
	public void setRegistrationUserPassword(String registrationToken, String newPassword);
}
