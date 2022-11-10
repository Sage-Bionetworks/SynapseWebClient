package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

public interface UserAccountServiceAsync {
  void signTermsOfUse(String accessToken, AsyncCallback<Void> callback);

  void createUserStep1(
    NewUser newUser,
    String portalEndpoint,
    AsyncCallback<Void> callback
  );

  void createUserStep2(
    String userName,
    String fName,
    String lName,
    String password,
    EmailValidationSignedToken emailValidationSignedToken,
    AsyncCallback<String> callback
  );

  void getPublicAndAuthenticatedGroupPrincipalIds(
    AsyncCallback<PublicPrincipalIds> callback
  );

  // this serves to test the access token and return the current user profile, and avoid CORS issue (backend responds with a CORS failure if authorization token is invalid)
  void getMyProfile(AsyncCallback<UserProfile> callback);
}
