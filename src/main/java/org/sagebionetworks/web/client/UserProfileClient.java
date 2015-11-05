
package org.sagebionetworks.web.client;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.TeamSubmissionEligibility;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
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

	String getFileURL(FileHandleAssociation fileHandleAssociation)
			throws RestServiceException;

	
}
