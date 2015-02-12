package org.sagebionetworks.web.server.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.AsynchJobType;
import org.sagebionetworks.client.exceptions.SynapseClientException;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.client.exceptions.SynapseResultNotReadyException;
import org.sagebionetworks.client.exceptions.SynapseTableUnavailableException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.markdown.SynapseMarkdownProcessor;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityIdList;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.IdList;
import org.sagebionetworks.repo.model.ListWrapper;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.MembershipRqstSubmission;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.dao.WikiPageKeyHelper;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.CompleteAllChunksRequest;
import org.sagebionetworks.repo.model.file.CreateChunkedFileTokenRequest;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.repo.model.principal.AddEmailInfo;
import org.sagebionetworks.repo.model.principal.AliasCheckRequest;
import org.sagebionetworks.repo.model.principal.AliasCheckResponse;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.RowSelection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableFileHandleResults;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.table.query.ParseException;
import org.sagebionetworks.table.query.TableQueryParser;
import org.sagebionetworks.web.client.ChallengeClient;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.widget.table.v2.TableModelUtils;
import org.sagebionetworks.web.server.table.TableSqlProcessor;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.ChallengeBundle;
import org.sagebionetworks.web.shared.ChallengePagedResults;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityConstants;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;
import org.sagebionetworks.web.shared.exceptions.TableQueryParseException;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
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
			InputStream s = SynapseClientImpl.class
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
			return synapseClient.createSubmission(submission, etag, teamId, memberStateHash);
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
	public List<Team> getSubmissionTeams(String userId, String challengeId) {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedIds results = synapseClient.listSubmissionTeams(challengeId, userId, GROUPS_PAGINATION_LIMIT, GROUPS_PAGINATION_OFFSET);
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

	@Override
	public ChallengeTeam registerChallengeTeam(ChallengeTeam challengeTeam) {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createChallengeTeam(challengeTeam);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void unregisterChallengeTeam(String challengeId, String teamId) {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteChallengeTeam(challengeId, teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ChallengeTeam updateRegisteredChallengeTeam(ChallengeTeam challengeTeam) {
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
			IdList currentUserIdList = new IdList();
			List<Long> currentUserIdWrapper = new ArrayList<Long>();
			currentUserIdWrapper.add(Long.parseLong(currentUserId));
			currentUserIdList.setList(currentUserIdWrapper);
			for (ChallengeTeam challengeTeam : pagedResults.getResults()) {
				ListWrapper<TeamMember> teamMemberList = synapseClient.listTeamMembers(challengeTeam.getTeamId(), currentUserIdList);
				if (teamMemberList.getList() != null && teamMemberList.getList().size() > 0) {
					ChallengeTeamBundle teamBundle = new ChallengeTeamBundle(challengeTeam, teamMemberList.getList().get(0).getIsAdmin());
					challengeTeamList.add(teamBundle);
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
			//TODO: get all user profiles in a single batch call when that service is available
			UserProfilePagedResults userProfiles = new UserProfilePagedResults();
			List<UserProfile> userProfileResults = new ArrayList<UserProfile>();
			for (String userId : paginatedIds.getResults()) {
				userProfileResults.add(synapseClient.getUserProfile(userId));
			}
			userProfiles.setTotalNumberOfResults(paginatedIds.getTotalNumberOfResults());
			userProfiles.setResults(userProfileResults);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public Challenge getChallenge(String projectId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getChallenge(projectId);
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
			
			List<ChallengeBundle> results = new ArrayList<ChallengeBundle>(pagedResults.getResults().size());
			for (int i = 0; i < challenges.size(); i++) {
				results.add(new ChallengeBundle(challenges.get(i), projectHeaders.get(i).getName()));
			}
			ChallengePagedResults challengeBundles = new ChallengePagedResults(results, pagedResults.getTotalNumberOfResults());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public List<Team> getRegistratableTeams(String challengeId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedIds results = synapseClient.listRegistratableTeams(challengeId, GROUPS_PAGINATION_LIMIT, GROUPS_PAGINATION_OFFSET); 
			return getTeams(results.getResults(), synapseClient);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

}
