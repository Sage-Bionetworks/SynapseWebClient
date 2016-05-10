package org.sagebionetworks.web.server.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.TeamSubmissionEligibility;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ChallengeClient;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

@SuppressWarnings("serial")
public class ChallengeClientImpl extends SynapseClientBase implements
		ChallengeClient {
	
	/**
	 * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
	 * @param in
	 * @return
	 */
	public <T extends JSONEntity> PaginatedResults<T> convertPaginated(org.sagebionetworks.reflection.model.PaginatedResults<T> in){
		return  new PaginatedResults<T>(in.getResults(), in.getTotalNumberOfResults());
	}
	
	/*
	 * ChallengeClient Service Methods
	 */
	
	// before we hit this limit we will use another mechanism to find users
	private static final int EVALUATION_PAGINATION_LIMIT = Integer.MAX_VALUE;
	private static final int EVALUATION_PAGINATION_OFFSET = 0;

	private static final Long GROUPS_PAGINATION_OFFSET = 0L;
	// before we hit this limit we will use another mechanism to find groups
	private static final Long GROUPS_PAGINATION_LIMIT = 1000L;

	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.web.client.ChallengeClient#getEvaluations(java.util.List)
	 */
	@Override
	public PaginatedResults<Evaluation> getEvaluations(List<String> evaluationIds)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			List<Evaluation> evalList = new ArrayList<Evaluation>();
			for (String evalId : evaluationIds) {
				evalList.add(synapseClient.getEvaluation(evalId));
			}
			PaginatedResults<Evaluation> results = new PaginatedResults<Evaluation>();
			results.setResults(evalList);
			results.setTotalNumberOfResults(evalList.size());
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<Evaluation> getAvailableEvaluations() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient
					.getAvailableEvaluationsPaginated(
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<Evaluation> getAvailableEvaluations(Set<String> targetEvaluationIds)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			List<String> targetEvaluationIdsList = new ArrayList<String>();
			targetEvaluationIdsList.addAll(targetEvaluationIds);
			return convertPaginated(synapseClient
					.getAvailableEvaluationsPaginated(
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT,
							targetEvaluationIdsList));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	/**
	 * Return all evaluations associated to a particular entity, for which the
	 * caller can change permissions
	 */
	@Override
	public List<Evaluation> getSharableEvaluations(String entityId)
			throws RestServiceException {
		try {
			
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			if (entityId == null || entityId.trim().length() == 0) {
				throw new BadRequestException("Entity ID must be given");
			}
			// look up the available evaluations
			org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> allEvaluations = synapseClient
					.getEvaluationByContentSource(entityId,
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT);
			List<Evaluation> mySharableEvalauations = new ArrayList<Evaluation>();
			
			for (Evaluation eval : allEvaluations.getResults()) {
				// evaluation is associated to entity id. can I change
				// permissions?
				
				UserEvaluationPermissions uep = synapseClient
						.getUserEvaluationPermissions(eval.getId());
				
				if (uep.getCanChangePermissions()) {
					mySharableEvalauations.add(eval);
				}
			}
			return mySharableEvalauations;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public Submission createTeamSubmission(Submission submission, String etag, String memberStateHash, String hostPageBaseURL)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String challengeEndpoint = SynapseClientImpl.getChallengeEndpoint(hostPageBaseURL);
			String settingsEndpoint = SynapseClientImpl.getNotificationEndpoint(NotificationTokenType.Settings, hostPageBaseURL);
			return synapseClient.createTeamSubmission(submission, etag, memberStateHash, challengeEndpoint, settingsEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	public Submission createIndividualSubmission(Submission submission, String etag, String hostPageBaseURL)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String challengeEndpoint = SynapseClientImpl.getChallengeEndpoint(hostPageBaseURL);
			String settingsEndpoint = SynapseClientImpl.getNotificationEndpoint(NotificationTokenType.Settings, hostPageBaseURL);
			return synapseClient.createIndividualSubmission(submission, etag, challengeEndpoint, settingsEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public String getUserEvaluationPermissions(String evalId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			UserEvaluationPermissions permissions = synapseClient
					.getUserEvaluationPermissions(evalId);
			JSONObjectAdapter json = permissions
					.writeToJSONObject(adapterFactory.createNew());
			return json.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	public String getEvaluationAcl(String evalId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			AccessControlList acl = synapseClient.getEvaluationAcl(evalId);
			JSONObjectAdapter json = acl.writeToJSONObject(adapterFactory
					.createNew());
			return json.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	public AccessControlList updateEvaluationAcl(AccessControlList acl)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateEvaluationAcl(acl);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Boolean hasSubmitted() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// get all evaluations for which the user has joined as a
			// participant
			org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> evaluations = synapseClient
					.getAvailableEvaluationsPaginated(
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT);
			for (Evaluation evaluation : evaluations.getResults()) {
				// return true if any of these have a submission
				org.sagebionetworks.reflection.model.PaginatedResults<Submission> res = synapseClient
						.getMySubmissions(evaluation.getId(), 0, 0);
				if (res.getTotalNumberOfResults() > 0) {
					return true;
				}
			}
			return false;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public List<Team> getSubmissionTeams(String userId, String challengeId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedIds results = synapseClient.listSubmissionTeams(challengeId, userId, GROUPS_PAGINATION_LIMIT, GROUPS_PAGINATION_OFFSET);
			return getTeams(results.getResults(), false, synapseClient);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	public List<Team> getTeams(List<String> teamIds, boolean sortAlphabetically, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		//get teams in bulk
		List<Long> teamLongIds = new ArrayList<Long>();
		for (String teamId : teamIds) {
			teamLongIds.add(Long.parseLong(teamId));
		}
		List<Team> teams = synapseClient.listTeams(teamLongIds);

		if (sortAlphabetically) {
			//sort by name
			Collections.sort(teams, new Comparator<Team>() {
		        @Override
		        public int compare(Team o1, Team o2) {
		        	return o1.getName().compareToIgnoreCase(o2.getName());
		        }
			});
		}

		return teams;
	}
	
	@Override
	public ChallengeTeam registerChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createChallengeTeam(challengeTeam);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void unregisterChallengeTeam(String challengeTeamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteChallengeTeam(challengeTeamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ChallengeTeam updateRegisteredChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateChallengeTeam(challengeTeam);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public ChallengeTeamPagedResults getChallengeTeams(String currentUserId, String challengeId, Integer limit, Integer offset)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.ChallengeTeamPagedResults pagedResults = synapseClient.listChallengeTeams(challengeId, limit.longValue(), offset.longValue());
			Long totalCount = pagedResults.getTotalNumberOfResults();
			List<ChallengeTeamBundle> challengeTeamList = new ArrayList<ChallengeTeamBundle>();
			
			List<Long> teamIds = new ArrayList<Long>();
			//initialize to all isAdmin to false
			for (ChallengeTeam challengeTeam : pagedResults.getResults()) {
				ChallengeTeamBundle teamBundle = new ChallengeTeamBundle(challengeTeam, false);
				teamIds.add(Long.parseLong(challengeTeam.getTeamId()));
				challengeTeamList.add(teamBundle);
			}
			if (currentUserId != null) {
				for (int i = 0; i < teamIds.size(); i++) {
					try {
						TeamMember member = synapseClient.getTeamMember(teamIds.get(i).toString(), currentUserId);
						challengeTeamList.get(i).setIsAdmin(member.getIsAdmin());
					} catch (Exception e) {
						//do nothing on failure
					}	
				}
			}
			
			ChallengeTeamPagedResults returnResults = new ChallengeTeamPagedResults(challengeTeamList, totalCount);
			return returnResults;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public UserProfilePagedResults getChallengeParticipants(boolean affiliated, String challengeId, Integer limit, Integer offset) 
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			PaginatedIds paginatedIds = synapseClient.listChallengeParticipants(challengeId, affiliated, limit.longValue(), offset.longValue());
			//get all user profiles in a single batch
			UserProfilePagedResults userProfiles = new UserProfilePagedResults();
			List<Long> userIds = new ArrayList<Long>();
			for (String userId : paginatedIds.getResults()) {
				userIds.add(Long.parseLong(userId));
			}
			userProfiles.setTotalNumberOfResults(paginatedIds.getTotalNumberOfResults());
			userProfiles.setResults(synapseClient.listUserProfiles(userIds));
			return userProfiles;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public Challenge getChallengeForProject(String projectId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getChallengeForProject(projectId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public ChallengePagedResults getChallenges(String userId, Integer limit, Integer offset)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.ChallengePagedResults pagedResults = synapseClient.listChallengesForParticipant(userId, limit.longValue(), offset.longValue());
			List<Challenge> challenges = pagedResults.getResults();
			List<ChallengeBundle> results = new ArrayList<ChallengeBundle>(pagedResults.getResults().size());
			if (pagedResults.getResults().size() > 0) {
				//gather all project ids
				List<Reference> references = new ArrayList<Reference>();
				for (Challenge challenge : challenges) {
					Reference ref = new Reference();
					ref.setTargetId(challenge.getProjectId());
					references.add(ref);
				}
				org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> headers = synapseClient.getEntityHeaderBatch(references);
				List<EntityHeader> projectHeaders = headers.getResults();
				
				Map<String, String> projectNameLookup = new HashMap<String, String>();
				for (EntityHeader projectHeader : projectHeaders) {
					projectNameLookup.put(projectHeader.getId(), projectHeader.getName());
				}
				
				for (Challenge challenge : challenges) {
					results.add(new ChallengeBundle(challenge, projectNameLookup.get(challenge.getProjectId())));
				}
			}
			ChallengePagedResults challengeBundles = new ChallengePagedResults(results, pagedResults.getTotalNumberOfResults());
			return challengeBundles;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public List<Team> getRegistratableTeams(String userId, String challengeId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//check to see if current user is registered for the challenge.  if not, send back error.
			Challenge challenge = synapseClient.getChallenge(challengeId);
			TeamMembershipStatus status = synapseClient.getTeamMembershipStatus(challenge.getParticipantTeamId(), userId);
			if (!status.getIsMember()) {
				throw new NotFoundException("User is not registered for the Challenge.");
			}
			PaginatedIds results = synapseClient.listRegistratableTeams(challengeId, GROUPS_PAGINATION_LIMIT, GROUPS_PAGINATION_OFFSET);
			return getTeams(results.getResults(), false, synapseClient);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public Set<String> getChallengeEvaluationIds(String challengeId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Challenge challenge = synapseClient.getChallenge(challengeId);
			//get the challenge, to resolve the project id
			org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> allEvaluations = synapseClient
					.getEvaluationByContentSource(challenge.getProjectId(),
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT);
			
			Set<String> evaluationIds = new HashSet<String>();
			for (Evaluation evaluation : allEvaluations.getResults()) {
				evaluationIds.add(evaluation.getId());
			}
			return evaluationIds;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}

	@Override
	public TeamSubmissionEligibility getTeamSubmissionEligibility(String evaluationId, String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getTeamSubmissionEligibility(evaluationId, teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}
}
