package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserAccountServiceAsync {

	void signTermsOfUse(String sessionToken, boolean acceptsTerms, AsyncCallback<Void> callback);

	void createUserStep1(NewUser newUser, String portalEndpoint, AsyncCallback<Void> callback);

	void createUserStep2(String userName, String fName, String lName, String password, EmailValidationSignedToken emailValidationSignedToken, AsyncCallback<String> callback);

	void getPublicAndAuthenticatedGroupPrincipalIds(AsyncCallback<PublicPrincipalIds> callback);

	/**
	 * If the session token is set for this site
	 * 
	 * @param callback
	 */
	void getCurrentSessionToken(AsyncCallback<String> callback);

	void getCurrentUserSessionData(AsyncCallback<UserSessionData> callback);
}
