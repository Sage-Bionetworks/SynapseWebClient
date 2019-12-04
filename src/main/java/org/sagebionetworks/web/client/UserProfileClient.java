
package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("userprofileclient")
public interface UserProfileClient extends RemoteService {

	VerificationSubmission createVerificationSubmission(VerificationSubmission verificationSubmission, String hostPageBaseURL) throws RestServiceException;

	VerificationPagedResults listVerificationSubmissions(VerificationStateEnum currentState, Long submitterId, Long limit, Long offset) throws RestServiceException;

	void updateVerificationState(long verificationId, VerificationState verificationState, String hostPageBaseURL) throws RestServiceException;

	UserBundle getMyOwnUserBundle(int mask) throws RestServiceException;
}
