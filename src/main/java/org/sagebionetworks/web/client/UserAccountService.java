package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;

@RemoteServiceRelativePath("users")
public interface UserAccountService extends XsrfProtectedService {	

	public void sendPasswordResetEmail(String emailAddress) throws RestServiceException;
	
	public void changePassword(String sessionToken, String newPassword) throws RestServiceException;

	public LoginResponse initiateSession(LoginRequest loginRequest) throws RestServiceException;
	
	public UserSessionData getUserSessionData(String sessionToken) throws RestServiceException;
	
	public void signTermsOfUse(String sessionToken, boolean acceptsTerms) throws RestServiceException;

	public void createUserStep1(String email, String portalEndpoint) throws RestServiceException;

	public String createUserStep2(String userName, String fName, String lName, String password, EmailValidationSignedToken emailValidationSignedToken) throws RestServiceException;
	
	public void terminateSession(String sessionToken) throws RestServiceException;
	
	public String getPrivateAuthServiceUrl();
	
	public String getPublicAuthServiceUrl();
	
	public String getTermsOfUse();
	
	public PublicPrincipalIds getPublicAndAuthenticatedGroupPrincipalIds();

}
