package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.IdList;
import org.sagebionetworks.repo.model.ListWrapper;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClient;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
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
	
	private org.sagebionetworks.client.SynapseClient createAnonymousSynapseClient() {
		return createSynapseClient(null);
	}
	
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
	public String getAvailableEvaluations() throws RestServiceException {
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

	public Submission createSubmission(Submission submission, String etag, String teamId, String memberStateHash)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//TODO: use new submission method that uses team id and member state hash
			return synapseClient.createSubmission(submission, etag);
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

	public String getAvailableEvaluationsSubmitterAliases()
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// query for all available evaluations.
			PaginatedResults<Evaluation> availableEvaluations = synapseClient
					.getAvailableEvaluationsPaginated(ZERO_OFFSET, MAX_LIMIT);
			// gather all submissions
			List<Submission> allSubmissions = new ArrayList<Submission>();
			for (Evaluation evaluation : availableEvaluations.getResults()) {
				// query for all submissions for each evaluation
				PaginatedResults<Submission> submissions = synapseClient
						.getMySubmissions(evaluation.getId(), ZERO_OFFSET, MAX_LIMIT);
				allSubmissions.addAll(submissions.getResults());
			}

			// sort by created on
			Collections.sort(allSubmissions, new Comparator<Submission>() {
				@Override
				public int compare(Submission o1, Submission o2) {
					return o2.getCreatedOn().compareTo(o1.getCreatedOn());
				}
			});

			// run through and only keep unique submitter alias values (first in
			// the list was most recently used)
			Set<String> uniqueSubmitterAliases = new HashSet<String>();
			List<String> returnAliases = new ArrayList<String>();
			for (Submission sub : allSubmissions) {
				String submitterAlias = sub.getSubmitterAlias();
				if (!uniqueSubmitterAliases.contains(submitterAlias)) {
					uniqueSubmitterAliases.add(submitterAlias);
					returnAliases.add(submitterAlias);
				}
			}
			// if it contains null or empty string, remove
			returnAliases.remove(null);
			returnAliases.remove("");
			RestResourceList returnList = new RestResourceList();
			returnList.setList(returnAliases);
			JSONObjectAdapter returnListJson = returnList
					.writeToJSONObject(adapterFactory.createNew());
			return returnListJson.toJSONString();

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
			//TODO: use new listSubmissionTeams
			PaginatedIds results = getTestRegisteredTeams();
//			PaginatedIds results = synapseClient.listSubmissionTeams(challengeId, userId, GROUPS_PAGINATION_LIMIT, GROUPS_PAGINATION_OFFSET);
			return getTeams(results.getResults(), synapseClient);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	public List<Team> getTeams(List<String> teamIds, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		//TODO: get teams in bulk
		List<Team> teamList = new ArrayList<Team>(teamIds.size());
		for (String teamId : teamIds) {
			teamList.add(synapseClient.getTeam(teamId));
		}
		return teamList;
	}
	
	private static final String testChallengeId = "1";
	private static final String testTeam1 = "3322410";
	private static final String testTeam2 = "3319267";
	private static final String testChallengeProject = "syn2290704";

	private static Challenge getTestChallenge() {
		Challenge c = new Challenge();
		c.setId(testChallengeId);
		c.setParticipantTeamId(testTeam1);
		c.setProjectId(testChallengeProject);
		return c;
	}
	private static ChallengeTeam getTestChallengeTeam(String message, String teamId) {
		ChallengeTeam ct = new ChallengeTeam();
		ct.setChallengeId(testChallengeId);
		ct.setMessage(message);
		ct.setTeamId(teamId);
		return ct;
	}
	private static ChallengeTeamPagedResults getTestChallengeTeamPagedResults(){
		ChallengeTeamPagedResults results = new ChallengeTeamPagedResults();
		ChallengeTeamBundle bundle1 = new ChallengeTeamBundle(getTestChallengeTeam("join the first team", testTeam1), true);
		ChallengeTeamBundle bundle2 = new ChallengeTeamBundle(getTestChallengeTeam("join the second team", testTeam2), false);
		List<ChallengeTeamBundle> resultList = new ArrayList<ChallengeTeamBundle>();
		resultList.add(bundle1);
		resultList.add(bundle2);
		results.setResults(resultList);
		results.setTotalNumberOfResults(4L);
		return results;
	}
	
	private static UserProfilePagedResults getTestUserProfilePagedResults(org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException{
		UserProfilePagedResults results = new UserProfilePagedResults();
		UserProfile profile1 = synapseClient.getUserProfile("1418535");
		UserProfile profile2 = synapseClient.getUserProfile("1118328");
		List<UserProfile> resultList = new ArrayList<UserProfile>();
		resultList.add(profile1);
		resultList.add(profile2);
		results.setResults(resultList);
		results.setTotalNumberOfResults(4L);
		return results;
	}
	
	private static org.sagebionetworks.repo.model.ChallengePagedResults getTestChallengePagedResults() {
		org.sagebionetworks.repo.model.ChallengePagedResults results = new org.sagebionetworks.repo.model.ChallengePagedResults();
		List<Challenge> challangeList = new ArrayList<Challenge>();
		challangeList.add(getTestChallenge());
		results.setResults(challangeList);
		results.setTotalNumberOfResults(1L);
		return results;
	}
	private static PaginatedIds getTestRegisteredTeams(){
		PaginatedIds ids = new PaginatedIds();
		List<String> idlist = new ArrayList<String>();
		idlist.add(testTeam1);
		idlist.add(testTeam2);
		ids.setResults(idlist);
		ids.setTotalNumberOfResults(2L);
		return ids;
	}
	
	@Override
	public ChallengeTeam registerChallengeTeam(ChallengeTeam challengeTeam) throws RestServiceException{
		return getTestChallengeTeam("You should totally join this team to win the test challenge!", testTeam1);
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.createChallengeTeam(challengeTeam);
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
	}
	
	@Override
	public void unregisterChallengeTeam(String challengeId, String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteChallengeTeam(Long.parseLong(challengeId), Long.parseLong(teamId));
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
		return getTestChallengeTeamPagedResults();
		//TODO: use service
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			org.sagebionetworks.repo.model.ChallengeTeamPagedResults pagedResults = synapseClient.listChallengeTeams(Long.parseLong(challengeId), limit.longValue(), offset.longValue());
//			Long totalCount = pagedResults.getTotalNumberOfResults();
//			List<ChallengeTeamBundle> challengeTeamList = new ArrayList<ChallengeTeamBundle>();
//			IdList currentUserIdList = new IdList();
//			List<Long> currentUserIdWrapper = new ArrayList<Long>();
//			currentUserIdWrapper.add(Long.parseLong(currentUserId));
//			currentUserIdList.setList(currentUserIdWrapper);
//			for (ChallengeTeam challengeTeam : pagedResults.getResults()) {
//				ListWrapper<TeamMember> teamMemberList = synapseClient.listTeamMembers(challengeTeam.getTeamId(), currentUserIdList);
//				if (teamMemberList.getList() != null && teamMemberList.getList().size() > 0) {
//					ChallengeTeamBundle teamBundle = new ChallengeTeamBundle(challengeTeam, teamMemberList.getList().get(0).getIsAdmin());
//					challengeTeamList.add(teamBundle);
//				}
//			}
//			
//			ChallengeTeamPagedResults returnResults = new ChallengeTeamPagedResults(challengeTeamList, totalCount);
//			return returnResults;
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
	}
	
	@Override
	public UserProfilePagedResults getChallengeParticipants(boolean affiliated, String challengeId, Integer limit, Integer offset) 
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return getTestUserProfilePagedResults(synapseClient);
			//TODO: use service
//			PaginatedIds paginatedIds = synapseClient.listChallengeParticipants(Long.parseLong(challengeId), affiliated, limit.longValue(), offset.longValue());
//			//TODO: get all user profiles in a single batch call when that service is available
//			UserProfilePagedResults userProfiles = new UserProfilePagedResults();
//			List<UserProfile> userProfileResults = new ArrayList<UserProfile>();
//			for (String userId : paginatedIds.getResults()) {
//				userProfileResults.add(synapseClient.getUserProfile(userId));
//			}
//			userProfiles.setTotalNumberOfResults(paginatedIds.getTotalNumberOfResults());
//			userProfiles.setResults(userProfileResults);
//			return userProfiles;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public Challenge getChallenge(String projectId) throws RestServiceException {
		return getTestChallenge();
		//TODO: use service
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.getChallengeForProject(projectId);
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
	}
	
	@Override
	public ChallengePagedResults getChallenges(String userId, Integer limit, Integer offset)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//TODO: use service
			org.sagebionetworks.repo.model.ChallengePagedResults pagedResults = getTestChallengePagedResults();
//			org.sagebionetworks.repo.model.ChallengePagedResults pagedResults = synapseClient.listChallengesForParticipant(Long.parseLong(userId), limit.longValue(), offset.longValue());
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
			
			List<ChallengeBundle> results = new ArrayList<ChallengeBundle>(pagedResults.getResults().size());
			for (int i = 0; i < challenges.size(); i++) {
				results.add(new ChallengeBundle(challenges.get(i), projectHeaders.get(i).getName()));
			}
			ChallengePagedResults challengeBundles = new ChallengePagedResults(results, pagedResults.getTotalNumberOfResults());
			return challengeBundles;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public List<Team> getRegistratableTeams(String challengeId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//TODO: use service
			PaginatedIds results = getTestRegisteredTeams();
//			PaginatedIds results = synapseClient.listRegistratableTeams(Long.parseLong(challengeId), GROUPS_PAGINATION_LIMIT, GROUPS_PAGINATION_OFFSET); 
			return getTeams(results.getResults(), synapseClient);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

}
