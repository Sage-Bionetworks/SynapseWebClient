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
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChallengeClientAsync {

	void getEvaluations(List<String> evaluationIds,
			AsyncCallback<PaginatedResults<Evaluation>> callback);
	void getAvailableEvaluations(AsyncCallback<PaginatedResults<Evaluation>> callback) throws RestServiceException;
	void getAvailableEvaluations(Set<String> targetEvaluationIds, AsyncCallback<PaginatedResults<Evaluation>> callback) throws RestServiceException;
	void getSharableEvaluations(String entityId, AsyncCallback<List<Evaluation>> asyncCallback);
	
	/**
	 * Create a new Submission object.  Callback returning the updated version of the Submission object
	 * @param submissionJson
	 * @param etag
	 * @param callback
	 */
	void createIndividualSubmission(Submission submission, String etag, AsyncCallback<Submission> callback) throws RestServiceException;
	void createTeamSubmission(Submission submission, String etag, String memberStateHash, AsyncCallback<Submission> callback) throws RestServiceException;
	void getSubmissionTeams(String userId, String challengeId, AsyncCallback<List<Team>> submissionTeams);
	
	void registerChallengeTeam(ChallengeTeam challengeTeam, AsyncCallback<ChallengeTeam> callback);
	void unregisterChallengeTeam(String challengeTeamId, AsyncCallback<Void> callback);
	void updateRegisteredChallengeTeam(ChallengeTeam challengeTeam, AsyncCallback<ChallengeTeam> callback);
	void getChallengeTeams(String userId, String challengeId, Integer limit, Integer offset, AsyncCallback<ChallengeTeamPagedResults> callback);
	void getChallengeParticipants(boolean affiliated, String challengeId, Integer limit, Integer offset, AsyncCallback<UserProfilePagedResults> callback);
	void getChallengeForProject(String projectId, AsyncCallback<Challenge> callback);
	void getChallenges(String userId, Integer limit, Integer offset, AsyncCallback<ChallengePagedResults> callback);
	void getRegistratableTeams(String userId, String challengeId, AsyncCallback<List<Team>> callback);
	
	void getUserEvaluationPermissions(String evalId, AsyncCallback<String> callback); 
	void getEvaluationAcl(String evalId, AsyncCallback<String> callback);
	void updateEvaluationAcl(AccessControlList acl, AsyncCallback<AccessControlList> callback);
	
	/**
	 * Return true if the current user has created at least one submission in the given evaluations
	 * @param evaluationIds
	 * @param callback
	 * @throws RestServiceException
	 */
	void hasSubmitted(AsyncCallback<Boolean> callback)	throws RestServiceException;
	
	void getChallengeEvaluationIds(String challengeId, AsyncCallback<Set<String>> callback);
	void getTeamSubmissionEligibility(String evaluationId, String teamId, AsyncCallback<TeamSubmissionEligibility> callback);
}
