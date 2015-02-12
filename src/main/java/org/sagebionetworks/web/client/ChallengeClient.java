
package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("challenge")	
public interface ChallengeClient extends RemoteService {

	public String getEvaluations(List<String> evaluationIds) throws RestServiceException;
	
	public String getAvailableEvaluations() throws RestServiceException;
	public String getAvailableEvaluations(Set<String> targetEvaluationIds) throws RestServiceException;
	
	public ArrayList<String> getSharableEvaluations(String entityId) throws RestServiceException;
	
	public Submission createSubmission(Submission submission, String etag, String teamId, String memberStateHash) throws RestServiceException;
	
	public String getUserEvaluationPermissions(String evalId) throws RestServiceException; 
	public String getEvaluationAcl(String evalId) throws RestServiceException;
	public AccessControlList updateEvaluationAcl(AccessControlList acl) throws RestServiceException;
	
	public String getAvailableEvaluationsSubmitterAliases() throws RestServiceException;

	public Boolean hasSubmitted()	throws RestServiceException;
	
	List<Team> getSubmissionTeams(String userId, String challengeId) throws RestServiceException;
	ChallengeTeam registerChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException;
	void unregisterChallengeTeam(String challengeId, String teamId) throws RestServiceException;
	ChallengeTeam updateRegisteredChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException;
	ChallengeTeamPagedResults getChallengeTeams(String userId, String challengeId, Integer limit, Integer offset) throws RestServiceException;
	UserProfilePagedResults getChallengeParticipants(boolean affiliated, String challengeId, Integer limit, Integer offset) throws RestServiceException;
	Challenge getChallenge(String projectId) throws RestServiceException;
	ChallengePagedResults getChallenges(String userId, Integer limit, Integer offset) throws RestServiceException;
	List<Team> getRegistratableTeams(String challengeId) throws RestServiceException;
}
