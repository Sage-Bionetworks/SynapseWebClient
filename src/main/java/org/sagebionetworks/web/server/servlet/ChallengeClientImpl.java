package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.TeamSubmissionEligibility;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ChallengeClientImpl extends RemoteServiceServlet implements
		ChallengeClient, TokenProvider {
	
	public static final int MAX_LOG_ENTRY_LABEL_SIZE = 200;
	
	// This will be appended to the User-Agent header.
	private static final String PORTAL_USER_AGENT = "Synapse-Web-Client/"
			+ PortalVersionHolder.getVersionInfo();

	private static class PortalVersionHolder {
		private static String versionInfo = "";

		static {
			InputStream s = ChallengeClientImpl.class
					.getResourceAsStream("/version-info.properties");
			Properties prop = new Properties();
			try {
				prop.load(s);
			} catch (IOException e) {
				throw new RuntimeException(
						"version-info.properties file not found", e);
			}
			versionInfo = prop
					.getProperty("org.sagebionetworks.portal.version");
		}

		private static String getVersionInfo() {
			return versionInfo;
		}

	}
	private TokenProvider tokenProvider = this;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	AutoGenFactory entityFactory = new AutoGenFactory();
	
	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;

	/**
	 * Essentially the constructor. Setup
	 * org.sagebionetworks.client.SynapseClient client.
	 * 
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
	}

	/**
	 * Injected with Gin
	 */
	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * This allows tests provide mock org.sagebionetworks.client.SynapseClient
	 * ojbects
	 * 
	 * @param provider
	 */
	public void setSynapseProvider(SynapseProvider provider) {
		this.synapseProvider = provider;
	}

	/**
	 * This allows integration tests to override the token provider.
	 * 
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	/**
	 * Validate that the service is ready to go. If any of the injected data is
	 * missing then it cannot run. Public for tests.
	 */
	public void validateService() {
		if (synapseProvider == null)
			throw new IllegalStateException("The SynapseProvider was not set");
		if (tokenProvider == null) {
			throw new IllegalStateException("The token provider was not set");
		}
	}

	@Override
	public String getSessionToken() {
		// By default, we get the token from the request cookies.
		return UserDataProvider.getThreadLocalUserToken(this
				.getThreadLocalRequest());
	}

	/*
	 * ChallengeClient Service Methods
	 */
	private org.sagebionetworks.client.SynapseClient createSynapseClient() {
		return createSynapseClient(tokenProvider.getSessionToken());
	}
	/**
	 * The org.sagebionetworks.client.SynapseClient client is stateful so we
	 * must create a new one for each request
	 */
	private org.sagebionetworks.client.SynapseClient createSynapseClient(String sessionToken) {
		// Create a new syanpse
		org.sagebionetworks.client.SynapseClient synapseClient = synapseProvider
				.createNewClient();
		synapseClient.setSessionToken(sessionToken);
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		synapseClient.setFileEndpoint(StackConfiguration
				.getFileServiceEndpoint());
		// Append the portal's version information to the user agent.
		synapseClient.appendUserAgent(PORTAL_USER_AGENT);
		return synapseClient;
	}


	private static final Integer MAX_LIMIT = Integer.MAX_VALUE;
	private static final Integer ZERO_OFFSET = 0;

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
	public String getEvaluations(List<String> evaluationIds)
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
			JSONObjectAdapter evaluationsJson = results
					.writeToJSONObject(adapterFactory.createNew());
			return evaluationsJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public PaginatedResults<Evaluation> getAvailableEvaluations() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<Evaluation> results = synapseClient
					.getAvailableEvaluationsPaginated(
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT);
			JSONObjectAdapter evaluationsJson = results
					.writeToJSONObject(adapterFactory.createNew());
			return evaluationsJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getAvailableEvaluations(Set<String> targetEvaluationIds)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			List<String> targetEvaluationIdsList = new ArrayList<String>();
			targetEvaluationIdsList.addAll(targetEvaluationIds);
			PaginatedResults<Evaluation> results = synapseClient
					.getAvailableEvaluationsPaginated(
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT,
							targetEvaluationIdsList);
			JSONObjectAdapter evaluationsJson = results
					.writeToJSONObject(adapterFactory.createNew());
			return evaluationsJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	/**
	 * Return all evaluations associated to a particular entity, for which the
	 * caller can change permissions
	 */
	@Override
	public ArrayList<String> getSharableEvaluations(String entityId)
			throws RestServiceException {
		if (entityId == null || entityId.trim().length() == 0) {
			throw new BadRequestException("Entity ID must be given");
		}
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// look up the available evaluations
			PaginatedResults<Evaluation> allEvaluations = synapseClient
					.getEvaluationByContentSource(entityId,
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT);

			ArrayList<String> mySharableEvalauations = new ArrayList<String>();
			for (Evaluation eval : allEvaluations.getResults()) {
				// evaluation is associated to entity id. can I change
				// permissions?
				UserEvaluationPermissions uep = synapseClient
						.getUserEvaluationPermissions(eval.getId());
				if (uep.getCanChangePermissions()) {
					mySharableEvalauations.add(eval.writeToJSONObject(
							adapterFactory.createNew()).toJSONString());
				}
			}
			return mySharableEvalauations;
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	public Submission createTeamSubmission(Submission submission, String etag, String memberStateHash)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createTeamSubmission(submission, etag, memberStateHash);
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	public Submission createIndividualSubmission(Submission submission, String etag)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createIndividualSubmission(submission, etag);
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
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
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	public AccessControlList updateEvaluationAcl(AccessControlList acl)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateEvaluationAcl(acl);
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public Boolean hasSubmitted() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// get all evaluations for which the user has joined as a
			// participant
			PaginatedResults<Evaluation> evaluations = synapseClient
					.getAvailableEvaluationsPaginated(
							EVALUATION_PAGINATION_OFFSET,
							EVALUATION_PAGINATION_LIMIT);
			for (Evaluation evaluation : evaluations.getResults()) {
				// return true if any of these have a submission
				PaginatedResults<Submission> res = synapseClient
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
			
			//gather all project ids
			List<Reference> references = new ArrayList<Reference>();
			for (Challenge challenge : challenges) {
				Reference ref = new Reference();
				ref.setTargetId(challenge.getProjectId());
				references.add(ref);
			}
			BatchResults<EntityHeader> headers = synapseClient.getEntityHeaderBatch(references);
			List<EntityHeader> projectHeaders = headers.getResults();
			
			Map<String, String> projectNameLookup = new HashMap<String, String>();
			for (EntityHeader projectHeader : projectHeaders) {
				projectNameLookup.put(projectHeader.getId(), projectHeader.getName());
			}
			
			List<ChallengeBundle> results = new ArrayList<ChallengeBundle>(pagedResults.getResults().size());
			for (Challenge challenge : challenges) {
				results.add(new ChallengeBundle(challenge, projectNameLookup.get(challenge.getProjectId())));
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
			PaginatedResults<Evaluation> allEvaluations = synapseClient
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
