package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.LoginResponse;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("users")
public interface UserAccountService extends RemoteService {

	void sendPasswordResetEmail(String emailAddress) throws RestServiceException;

	void changePassword(String sessionToken, String newPassword) throws RestServiceException;

	LoginResponse initiateSession(LoginRequest loginRequest) throws RestServiceException;

	UserSessionData getUserSessionData(String sessionToken) throws RestServiceException;

	void signTermsOfUse(String sessionToken, boolean acceptsTerms) throws RestServiceException;

	void createUserStep1(NewUser newUser, String portalEndpoint) throws RestServiceException;

	String createUserStep2(String userName, String fName, String lName, String password, EmailValidationSignedToken emailValidationSignedToken) throws RestServiceException;

	void terminateSession(String sessionToken) throws RestServiceException;

	String getPrivateAuthServiceUrl();

	String getPublicAuthServiceUrl();

	String getTermsOfUse();

	PublicPrincipalIds getPublicAndAuthenticatedGroupPrincipalIds();

}
