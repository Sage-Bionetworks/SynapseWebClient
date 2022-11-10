package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RemoteServiceRelativePath("users")
public interface UserAccountService extends RemoteService {
  void signTermsOfUse(String accessToken) throws RestServiceException;

  void createUserStep1(NewUser newUser, String portalEndpoint)
    throws RestServiceException;

  String createUserStep2(
    String userName,
    String fName,
    String lName,
    String password,
    EmailValidationSignedToken emailValidationSignedToken
  ) throws RestServiceException;

  PublicPrincipalIds getPublicAndAuthenticatedGroupPrincipalIds();
  UserProfile getMyProfile() throws RestServiceException;
}
