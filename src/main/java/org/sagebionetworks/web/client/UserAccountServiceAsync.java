package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserAccountServiceAsync {

	void signTermsOfUse(String accessToken, AsyncCallback<Void> callback);

	void createUserStep1(NewUser newUser, String portalEndpoint, AsyncCallback<Void> callback);

	void createUserStep2(String userName, String fName, String lName, String password, EmailValidationSignedToken emailValidationSignedToken, AsyncCallback<String> callback);

	void getPublicAndAuthenticatedGroupPrincipalIds(AsyncCallback<PublicPrincipalIds> callback);
}
