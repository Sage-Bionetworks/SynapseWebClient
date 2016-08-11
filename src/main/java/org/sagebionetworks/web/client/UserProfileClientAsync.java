package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserProfileClientAsync {

	void createVerificationSubmission(
			VerificationSubmission verificationSubmission,
			String hostPageBaseURL, AsyncCallback<VerificationSubmission> callback);

	void listVerificationSubmissions(VerificationStateEnum currentState,
			Long submitterId, Long limit, Long offset,
			AsyncCallback<VerificationPagedResults> callback);

	void updateVerificationState(long verificationId,
			VerificationState verificationState, String hostPageBaseURL, AsyncCallback<Void> callback);

	void deleteVerificationSubmission(long verificationId,
			AsyncCallback<Void> callback);

	void getMyOwnUserBundle(int mask, AsyncCallback<UserBundle> callback);

	void getUserBundle(Long principalId, int mask,
			AsyncCallback<UserBundle> callback);

	void unbindOAuthProvidersUserId(OAuthProvider provider, String alias,
			AsyncCallback<Void> callback);

}
