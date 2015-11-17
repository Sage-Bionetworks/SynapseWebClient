
package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("userprofileclient")	
public interface UserProfileClient extends RemoteService {

	VerificationSubmission createVerificationSubmission(
			VerificationSubmission verificationSubmission)
			throws RestServiceException;

	VerificationPagedResults listVerificationSubmissions(
			VerificationStateEnum currentState, Long submitterId, Long limit,
			Long offset) throws RestServiceException;

	void updateVerificationState(long verificationId,
			VerificationState verificationState) throws RestServiceException;

	void deleteVerificationSubmission(long verificationId)
			throws RestServiceException;

	UserBundle getMyOwnUserBundle(int mask) throws RestServiceException;

	UserBundle getUserBundle(Long principalId, int mask)
			throws RestServiceException;

	void unbindOAuthProvidersUserId(OAuthProvider provider, String alias)
			throws RestServiceException;

}
