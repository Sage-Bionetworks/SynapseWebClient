
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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("challengeclient")
public interface ChallengeClient extends RemoteService {
	List<Evaluation> getSharableEvaluations(String entityId) throws RestServiceException;

	public Submission createIndividualSubmission(Submission submission, String etag, String hostPageBaseURL) throws RestServiceException;

	public Submission createTeamSubmission(Submission submission, String etag, String memberStateHash, String hostPageBaseURL) throws RestServiceException;

	public String getUserEvaluationPermissions(String evalId) throws RestServiceException;

	public String getEvaluationAcl(String evalId) throws RestServiceException;

	public AccessControlList updateEvaluationAcl(AccessControlList acl) throws RestServiceException;

	List<Team> getSubmissionTeams(String userId, String challengeId) throws RestServiceException;

	ChallengeTeam registerChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException;

	void unregisterChallengeTeam(String challengeTeamId) throws RestServiceException;

	ChallengeTeam updateRegisteredChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException;

	ChallengeTeamPagedResults getChallengeTeams(String userId, String challengeId, Integer limit, Integer offset) throws RestServiceException;

	UserProfilePagedResults getChallengeParticipants(boolean affiliated, String challengeId, Integer limit, Integer offset) throws RestServiceException;

	Challenge getChallengeForProject(String projectId) throws RestServiceException;

	List<Team> getRegistratableTeams(String userId, String challengeId) throws RestServiceException;

	Set<String> getChallengeEvaluationIds(String challengeId) throws RestServiceException;

	TeamSubmissionEligibility getTeamSubmissionEligibility(String evaluationId, String teamId) throws RestServiceException;

	void updateEvaluation(Evaluation evaluation) throws RestServiceException;

	void createEvaluation(Evaluation evaluation) throws RestServiceException;

	void deleteEvaluation(String evaluationId) throws RestServiceException;

	Set<String> getProjectEvaluationIds(String projectId) throws RestServiceException;

	void deleteChallenge(String challengeId) throws RestServiceException;

	Challenge updateChallenge(Challenge challenge) throws RestServiceException;

	Challenge createChallenge(Challenge challenge) throws RestServiceException;

	void requestToCancelSubmission(String submissionId) throws RestServiceException;
}
