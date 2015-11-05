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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserProfileClientAsync {

	void createVerificationSubmission(
			VerificationSubmission verificationSubmission,
			AsyncCallback<VerificationSubmission> callback);

	void listVerificationSubmissions(VerificationStateEnum currentState,
			Long submitterId, Long limit, Long offset,
			AsyncCallback<VerificationPagedResults> callback);

	void updateVerificationState(long verificationId,
			VerificationState verificationState, AsyncCallback<Void> callback);

	void deleteVerificationSubmission(long verificationId,
			AsyncCallback<Void> callback);

	void getMyOwnUserBundle(int mask, AsyncCallback<UserBundle> callback);

	void getUserBundle(long principalId, int mask,
			AsyncCallback<UserBundle> callback);

	void getFileURL(FileHandleAssociation fileHandleAssociation,
			AsyncCallback<String> callback);

}
