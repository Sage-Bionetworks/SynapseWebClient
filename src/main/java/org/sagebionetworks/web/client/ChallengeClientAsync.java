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
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChallengeClientAsync {
	void getSharableEvaluations(String entityId, AsyncCallback<List<Evaluation>> asyncCallback);

	/**
	 * Create a new Submission object. Callback returning the updated version of the Submission object
	 * 
	 * @param submissionJson
	 * @param etag
	 * @param callback
	 */
	void createIndividualSubmission(Submission submission, String etag, String hostPageBaseURL, AsyncCallback<Submission> callback) throws RestServiceException;

	void createTeamSubmission(Submission submission, String etag, String memberStateHash, String hostPageBaseURL, AsyncCallback<Submission> callback) throws RestServiceException;

	void getSubmissionTeams(String userId, String challengeId, AsyncCallback<List<Team>> submissionTeams);

	void registerChallengeTeam(ChallengeTeam challengeTeam, AsyncCallback<ChallengeTeam> callback);

	void unregisterChallengeTeam(String challengeTeamId, AsyncCallback<Void> callback);

	void updateRegisteredChallengeTeam(ChallengeTeam challengeTeam, AsyncCallback<ChallengeTeam> callback);

	void getChallengeTeams(String userId, String challengeId, Integer limit, Integer offset, AsyncCallback<ChallengeTeamPagedResults> callback);

	void getChallengeParticipants(boolean affiliated, String challengeId, Integer limit, Integer offset, AsyncCallback<UserProfilePagedResults> callback);

	void getChallengeForProject(String projectId, AsyncCallback<Challenge> callback);

	void getRegistratableTeams(String userId, String challengeId, AsyncCallback<List<Team>> callback);

	void getUserEvaluationPermissions(String evalId, AsyncCallback<String> callback);

	void getEvaluationAcl(String evalId, AsyncCallback<String> callback);

	void updateEvaluationAcl(AccessControlList acl, AsyncCallback<AccessControlList> callback);

	void getChallengeEvaluationIds(String challengeId, AsyncCallback<Set<String>> callback);

	void getTeamSubmissionEligibility(String evaluationId, String teamId, AsyncCallback<TeamSubmissionEligibility> callback);

	void updateEvaluation(Evaluation evaluation, AsyncCallback<Void> callback);

	void createEvaluation(Evaluation evaluation, AsyncCallback<Void> callback);

	void deleteEvaluation(String evaluationId, AsyncCallback<Void> callback);

	void getProjectEvaluationIds(String projectId, AsyncCallback<Set<String>> callback);

	void deleteChallenge(String challengeId, AsyncCallback<Void> callback);

	void updateChallenge(Challenge challenge, AsyncCallback<Challenge> callback);

	void createChallenge(Challenge challenge, AsyncCallback<Challenge> callback);

	void requestToCancelSubmission(String submissionId, AsyncCallback<Void> callback);
}
