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
import org.sagebionetworks.markdown.SynapseMarkdownProcessor;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityIdList;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.MembershipRqstSubmission;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.Reference;
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
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.dao.WikiPageKeyHelper;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
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
import org.sagebionetworks.table.query.util.TableSqlProcessor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityConstants;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SynapseClientImpl extends RemoteServiceServlet implements
		SynapseClient, TokenProvider {
	
	public static final int MAX_LOG_ENTRY_LABEL_SIZE = 200;
	
	static private Log log = LogFactory.getLog(SynapseClientImpl.class);
	// This will be appended to the User-Agent header.
	public static final String PORTAL_USER_AGENT = "Synapse-Web-Client/"
			+ PortalVersionHolder.getVersionInfo();
	static {// kick off initialization (like pattern compilation) by referencing
			// it
		SynapseMarkdownProcessor.getInstance();
	}

	private Cache<MarkdownCacheRequest, WikiPage> wiki2Markdown = CacheBuilder
			.newBuilder().maximumSize(35).expireAfterAccess(1, TimeUnit.HOURS)
			.build(new CacheLoader<MarkdownCacheRequest, WikiPage>() {
				@Override
				public WikiPage load(MarkdownCacheRequest key) throws Exception {
					try {
						org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
						WikiPage returnPage = null;
						if (key.getVersion() == null)
							returnPage = synapseClient.getWikiPage(key
									.getWikiPageKey());
						else
							returnPage = synapseClient
									.getWikiPageForVersion(
											key.getWikiPageKey(),
											key.getVersion());

						return returnPage;
					} catch (SynapseException e) {
						throw ExceptionUtil.convertSynapseException(e);
					}
				}
			});

	private TokenProvider tokenProvider = this;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	AutoGenFactory entityFactory = new AutoGenFactory();
	private volatile HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> pageName2WikiKeyMap;
	private volatile HashSet<String> wikiBasedEntities;

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
	
	public void setMarkdownCache(Cache<MarkdownCacheRequest, WikiPage> wikiToMarkdown) {
		this.wiki2Markdown = wikiToMarkdown;
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
	 * SynapseClient Service Methods
	 */
	@Override
	public Entity getEntity(String entityId) throws RestServiceException {
		return getEntityForVersion(entityId, null);
	}
	
	public Project getProject(String projectId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return (Project) synapseClient.getEntityById(projectId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Entity getEntityForVersion(String entityId, Long versionNumber)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (versionNumber == null) {
				return synapseClient.getEntityById(entityId);
			} else {
				return synapseClient.getEntityByIdForVersion(entityId,
						versionNumber);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public EntityBundle getEntityBundle(String entityId, int partsMask)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getEntityBundle(entityId, partsMask);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public EntityBundle getEntityBundleForVersion(String entityId,
			Long versionNumber, int partsMask) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getEntityBundle(entityId, versionNumber, partsMask);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<VersionInfo> getEntityVersions(String entityId, int offset, int limit)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient
					.getEntityVersions(entityId, offset, limit));
		} catch (SynapseException e) {
			log.error(e);
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public EntityPath getEntityPath(String entityId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getEntityPath(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public SearchResults search(SearchQuery searchQuery)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.search(searchQuery);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	/*
	 * Private Methods
	 */
	
	private JSONObject query(String query) throws SynapseException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		return synapseClient.query(query);
	}

	public static String createJSONStringFromArray(
			List<? extends JSONEntity> list) throws JSONObjectAdapterException {
		JSONArrayAdapter aa = new JSONArrayAdapterImpl();
		for (int i = 0; i < list.size(); i++) {
			JSONObjectAdapter oa = new JSONObjectAdapterImpl();
			list.get(i).writeToJSONObject(oa);
			aa.put(i, oa);
		}
		return aa.toJSONString();
	}

	
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

	@Override
	public SerializableWhitelist junk(SerializableWhitelist l) {
		return null;
	}

	private static final Integer MAX_LIMIT = Integer.MAX_VALUE;
	private static final Integer ZERO_OFFSET = 0;


	private static final int USER_PAGINATION_OFFSET = 0;
	// before we hit this limit we will use another mechanism to find users
	private static final int USER_PAGINATION_LIMIT = 1000;
	
	/**
	 * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
	 * @param in
	 * @return
	 */
	public <T extends JSONEntity> PaginatedResults<T> convertPaginated(org.sagebionetworks.repo.model.PaginatedResults<T> in){
		return  new PaginatedResults<T>(in.getResults(), in.getTotalNumberOfResults());
	}

	@Override
	public PaginatedResults<EntityHeader> getEntityReferencedBy(String entityId)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return convertPaginated(synapseClient
					.getEntityReferencedBy(entityId, null));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void logDebug(String message) {
		log.debug(message);
	}

	@Override
	public void logError(String message) throws RestServiceException {
		log.error(message);
	}

	@Override
	public void logErrorToRepositoryServices(String message, String label) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			LogEntry entry = new LogEntry();
			String outputLabel = "";
			if (label != null) {
				outputLabel = label.substring(0, Math.min(label.length(), MAX_LOG_ENTRY_LABEL_SIZE));
			}
			new PortalVersionHolder();
			entry.setLabel("SWC/" + PortalVersionHolder.getVersionInfo() + "/" + outputLabel);
			String userId = "";
			UserProfile profile = synapseClient.getMyProfile();
			if (profile != null) {
				userId = "userId="+profile.getOwnerId()+" ";
			}
			entry.setMessage(userId+message);
			synapseClient.logError(entry);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void logInfo(String message) {
		log.info(message);
	}

	@Override
	public String getRepositoryServiceUrl() {
		return urlProvider.getRepositoryServiceUrl();
	}
	
	/**
	 * Update an entity.
	 */
	public Entity updateEntity(Entity toUpdate) throws RestServiceException{
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.putEntity(toUpdate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	/**
	 * Create or update an entity
	 * 
	 * @param entity
	 * @param annos
	 * @param isNew
	 * @return
	 * @throws RestServiceException
	 */
	@Override
	public String createOrUpdateEntity(Entity entity, Annotations annos,
			boolean isNew) throws RestServiceException {
		// First read the entity
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			if (isNew) {
				// This is a create
				entity = synapseClient.createEntity(entity);
			} else {
				// This is an update
				entity = synapseClient.putEntity(entity);
			}
			// Update the annotations
			if (annos != null) {
				annos.setEtag(entity.getEtag());
				annos.setId(entity.getId());
				synapseClient.updateAnnotations(entity.getId(), annos);
			}
			return entity.getId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}

	/**
	 * Parse an entity from its json.
	 * 
	 * @param json
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public Entity parseEntityFromJson(String json)
			throws JSONObjectAdapterException {
		if (json == null)
			throw new IllegalArgumentException("Entity cannot be null");
		// Create an adapter
		JSONObjectAdapter adapter = adapterFactory.createNew(json);
		// Extrat the entity type.
		if (!adapter.has(EntityConstants.ENTITY_TYPE)
				|| adapter.isNull(EntityConstants.ENTITY_TYPE)) {
			throw new IllegalArgumentException("JSON does not contain: "
					+ EntityConstants.ENTITY_TYPE);
		}
		String entityType = adapter.getString(EntityConstants.ENTITY_TYPE);
		Entity entity = (Entity) entityFactory.newInstance(entityType);
		entity.initializeFromJSONObject(adapter);
		return entity;
	}

	@Override
	public PaginatedResults<EntityHeader> getEntityTypeBatch(List<String> entityIds)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient
					.getEntityTypeBatch(entityIds);
			return new PaginatedResults<EntityHeader>(results.getResults(), results.getTotalNumberOfResults());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<EntityHeader> getEntityHeaderBatch(ReferenceList list)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient
					.getEntityHeaderBatch(list.getReferences());
			return new PaginatedResults<EntityHeader>(results.getResults(), results.getTotalNumberOfResults());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public ArrayList<EntityHeader> getEntityHeaderBatch(List<String> entityIds)
			throws RestServiceException {
		try {
			List<Reference> list = new ArrayList<Reference>();
			for (String entityId : entityIds) {
				Reference ref = new Reference();
				ref.setTargetId(entityId);
				list.add(ref);
			}
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient
					.getEntityHeaderBatch(list);
			ArrayList<EntityHeader> returnList = new ArrayList<EntityHeader>();
			returnList.addAll(results.getResults());
			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteEntityById(String entityId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.deleteEntityById(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteEntityById(String entityId, Boolean skipTrashCan)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.deleteEntityById(entityId, skipTrashCan);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteEntityVersionById(String entityId, Long versionNumber)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.deleteEntityVersionById(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<TrashedEntity> viewTrashForUser(long offset, long limit)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return convertPaginated(synapseClient
					.viewTrashForUser(offset, limit));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public void restoreFromTrash(String entityId, String newParentId)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.restoreFromTrash(entityId, newParentId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void moveToTrash(String entityId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.moveToTrash(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void purgeTrashForUser() throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.purgeTrashForUser();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void purgeTrashForUser(String entityId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.purgeTrashForUser(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void purgeMultipleTrashedEntitiesForUser(Set<String> entityIds) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			for (String entityId : entityIds) {
				synapseClient.purgeTrashForUser(entityId);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UserProfile getUserProfile() throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getMyProfile();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UserProfile getUserProfile(String userId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			UserProfile profile;
			if (userId == null) {
				profile = synapseClient.getMyProfile();
			} else {
				profile = synapseClient.getUserProfile(userId);
			}
			return profile;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Team getTeam(String teamId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getTeam(teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UserGroupHeaderResponsePage getUserGroupHeadersById(ArrayList<String> ids)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			return synapseClient.getUserGroupHeadersByIds(ids);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UserGroupHeaderResponsePage getUserGroupHeadersByPrefix(String prefix, long limit, long offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getUserGroupHeadersByPrefix(prefix, limit, offset);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (UnsupportedEncodingException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public void updateUserProfile(UserProfile profile)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.updateMyProfile(profile);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public void additionalEmailValidation(String userId, String emailAddress,
			String callbackUrl) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Long userIdLong = Long.parseLong(userId);
			synapseClient.additionalEmailValidation(userIdLong, emailAddress,
					callbackUrl);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void addEmail(String emailValidationToken)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			AddEmailInfo newEmailInfo = new AddEmailInfo();
			newEmailInfo.setEmailValidationToken(emailValidationToken);
			synapseClient.addEmail(newEmailInfo, true);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getNotificationEmail() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getNotificationEmail();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void setNotificationEmail(String email) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.setNotificationEmail(email);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public AccessControlList getBenefactorAcl(String id) throws RestServiceException {
		EntityBundle bundle = getEntityBundle(id, EntityBundle.BENEFACTOR_ACL);
		return bundle.getBenefactorAcl();
	}

	@Override
	public AccessControlList getEntityBenefactorAcl(String id) throws RestServiceException {
		return getBenefactorAcl(id);
	}

	@Override
	public AccessControlList createAcl(AccessControlList acl)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createACL(acl);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessControlList updateAcl(AccessControlList aclEW)
			throws RestServiceException {
		return updateAcl(aclEW, false);
	}

	@Override
	public AccessControlList updateAcl(AccessControlList acl, boolean recursive)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateACL(acl, recursive);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AccessControlList deleteAcl(String ownerEntityId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// first delete the ACL
			synapseClient.deleteACL(ownerEntityId);
			// now get the ACL governing this entity, which will be some
			// ancestor, the 'permissions benefactor'
			return getBenefactorAcl(ownerEntityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public boolean hasAccess(String ownerEntityId, String accessType)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.canAccess(ownerEntityId,
					ACCESS_TYPE.valueOf(accessType));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public boolean hasAccess(String ownerId, String ownerType, String accessType)
			throws RestServiceException {
		ObjectType type = ObjectType.valueOf(ownerType);
		ACCESS_TYPE access = ACCESS_TYPE.valueOf(accessType);
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.canAccess(ownerId, type, access);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

//	@Override
//	public PaginatedResults<UserProfile> getAllUsers() throws RestServiceException {
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return convertPaginated(synapseClient
//					.getUsers(USER_PAGINATION_OFFSET, USER_PAGINATION_LIMIT));
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		} 
//	}

	
	@Override
	public AccessRequirement createAccessRequirement(AccessRequirement ar)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createAccessRequirement(ar);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ACTAccessRequirement createLockAccessRequirement(String entityId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createLockAccessRequirement(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public AccessRequirementsTransport getUnmetAccessRequirements(
			String entityId, ACCESS_TYPE accessType) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(entityId);
			subjectId.setType(RestrictableObjectType.ENTITY);

			VariableContentPaginatedResults<AccessRequirement> accessRequirements = synapseClient
					.getUnmetAccessRequirements(subjectId, accessType);
			AccessRequirementsTransport transport = new AccessRequirementsTransport();
			transport.setAccessRequirements(new PaginatedResults<AccessRequirement>(
							accessRequirements.getResults(), accessRequirements
									.getTotalNumberOfResults()));
			Entity e = synapseClient.getEntityById(entityId);
			transport.setEntity(e);
			UserProfile profile = getUserProfile();
			transport.setUserProfile(profile);
			return transport;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public List<AccessRequirement> getTeamAccessRequirements(String teamId)
			throws RestServiceException {
		return getTeamAccessRequirements(teamId, false);
	}

	private List<AccessRequirement> getTeamAccessRequirements(String teamId, boolean unmetOnly)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(teamId);
			subjectId.setType(RestrictableObjectType.TEAM);
			VariableContentPaginatedResults<AccessRequirement> accessRequirements;
			if (unmetOnly)
				accessRequirements = synapseClient
						.getUnmetAccessRequirements(subjectId, ACCESS_TYPE.PARTICIPATE);
			else
				accessRequirements = synapseClient
						.getAccessRequirements(subjectId);
			return accessRequirements.getResults();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<AccessRequirement> getAllEntityUploadAccessRequirements(String entityId)
			throws RestServiceException {
		return getEntityAccessRequirements(entityId, false, ACCESS_TYPE.UPLOAD);
	}

	public PaginatedResults<AccessRequirement> getEntityAccessRequirements(String entityId,
			boolean unmetOnly, ACCESS_TYPE targetAccessType)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(entityId);
			subjectId.setType(RestrictableObjectType.ENTITY);
			VariableContentPaginatedResults<AccessRequirement> accessRequirements;
			if (unmetOnly)
				accessRequirements = synapseClient
						.getUnmetAccessRequirements(subjectId, targetAccessType);
			else
				accessRequirements = synapseClient
						.getAccessRequirements(subjectId);
			// filter to the targetAccessType
			if (targetAccessType != null) {
				List<AccessRequirement> filteredResults = AccessRequirementUtils.filterAccessRequirements(
						accessRequirements.getResults(), targetAccessType);
				accessRequirements.setResults(filteredResults);
				accessRequirements.setTotalNumberOfResults(filteredResults
						.size());
			}

			return new PaginatedResults<AccessRequirement>(accessRequirements.getResults(), accessRequirements.getTotalNumberOfResults());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public AccessApproval createAccessApproval(AccessApproval aaEW)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createAccessApproval(aaEW);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Entity updateExternalFile(String entityId,
			String externalUrl, String name) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			boolean isManuallySettingName = isManuallySettingExternalName(name);
			Entity entity = synapseClient.getEntityById(entityId);
			if (!(entity instanceof FileEntity)) {
				throw new RuntimeException("Upload failed. Entity id: "
						+ entity.getId() + " is not a File.");
			}

			ExternalFileHandle efh = new ExternalFileHandle();
			efh.setExternalURL(externalUrl);
			ExternalFileHandle clone = synapseClient
					.createExternalFileHandle(efh);
			((FileEntity) entity).setDataFileHandleId(clone.getId());
			if (isManuallySettingName)
				entity.setName(name);
			Entity updatedEntity = synapseClient.putEntity(entity);
			if (!isManuallySettingName)
				updatedEntity = updateExternalFileName(updatedEntity,
						externalUrl, synapseClient);
			return updatedEntity;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private boolean isManuallySettingExternalName(String name) {
		return name != null && name.trim().length() > 0;
	}

	@Override
	public Entity createExternalFile(String parentEntityId,
			String externalUrl, String name) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			boolean isManuallySettingName = isManuallySettingExternalName(name);
			FileEntity newEntity = new FileEntity();
			ExternalFileHandle efh = new ExternalFileHandle();
			efh.setExternalURL(externalUrl);
			if (isManuallySettingName)
				efh.setFileName(name);
			ExternalFileHandle clone = synapseClient
					.createExternalFileHandle(efh);
			newEntity.setDataFileHandleId(clone.getId());
			newEntity.setParentId(parentEntityId);
			if (isManuallySettingName)
				newEntity.setName(name);
			Entity updatedEntity = synapseClient.createEntity(newEntity);
			if (!isManuallySettingName)
				updatedEntity = updateExternalFileName(updatedEntity,
						externalUrl, synapseClient);
			return updatedEntity;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private Entity updateExternalFileName(Entity entity, String externalUrl,
			org.sagebionetworks.client.SynapseClient synapseClient) {
		String oldName = entity.getName();
		try {
			// also try to rename to something reasonable, ignore if anything
			// goes wrong
			entity.setName(DisplayUtils.getFileNameFromExternalUrl(externalUrl));
			entity = synapseClient.putEntity(entity);
		} catch (Throwable t) {
			// if anything goes wrong, send back the actual name
			entity.setName(oldName);
		}
		return entity;
	}

	@Override
	public String markdown2Html(String markdown, Boolean isPreview,
			Boolean isAlphaMode, String clientHostString)
			throws RestServiceException {
		try {
			long startTime = System.currentTimeMillis();
			String html = SynapseMarkdownProcessor.getInstance().markdown2Html(
					markdown, isPreview, clientHostString);
			long endTime = System.currentTimeMillis();
			float elapsedTime = endTime - startTime;
			logInfo("Markdown processing took " + (elapsedTime / 1000f)
					+ " seconds.  In alpha mode? " + isAlphaMode);
			return html;
		} catch (IOException e) {
			throw new RestServiceException(e.getMessage());
		}
	}

	@Override
	public Activity getActivityForEntity(String entityId)
			throws RestServiceException {
		return getActivityForEntityVersion(entityId, null);
	}

	@Override
	public Activity getActivityForEntityVersion(String entityId,
			Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getActivityForEntityVersion(
					entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Activity getActivity(String activityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getActivity(activityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<Reference> getEntitiesGeneratedBy(String activityId, Integer limit,
			Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient
					.getEntitiesGeneratedBy(activityId, limit, offset));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getJSONEntity(String repoUri) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONObject entity = synapseClient.getEntity(repoUri);
			return entity.toString();
		} catch (SynapseTableUnavailableException e) {
			handleTableUnavailableException(e);
			// TableUnavilableException is thrown in line above, should never
			// reach the next line
			return null;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	
	@Override
	public PaginatedResults<WikiHeader> getWikiHeaderTree(String ownerId, String ownerType)
			throws RestServiceException {
		return getWikiHeaderTree(ownerId,
				ObjectType.valueOf(ownerType));
	}

	private PaginatedResults<WikiHeader> getWikiHeaderTree(String ownerId,
			ObjectType ownerType) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient
					.getWikiHeaderTree(ownerId, ownerType));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private String getRootWikiId(
			org.sagebionetworks.client.SynapseClient synapseClient,
			String ownerId, ObjectType ownerType) throws RestServiceException {
		try {
			WikiPageKey key= synapseClient.getRootWikiPageKey(ownerId, ownerType);
			return key.getWikiPageId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getFileEndpoint() throws RestServiceException {
		// org.sagebionetworks.client.SynapseClient synapseClient =
		// createSynapseClient();
		// return synapseClient.getFileEndpoint();
		return StackConfiguration.getFileServiceEndpoint();
	}

	@Override
	public String getRootWikiId(String ownerObjectId, String ownerObjectType) throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			// asking for the root. find the root id first
			String rootWikiPageId = getRootWikiId(synapseClient,
					ownerObjectId,
					ObjectType.valueOf(ownerObjectType));
			return rootWikiPageId;
	}
	
	@Override
	public FileHandleResults getWikiAttachmentHandles(
			org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient,
						key.getOwnerObjectId(),
						ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			FileHandleResults results = synapseClient
					.getWikiAttachmenthHandles(properKey);
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	// V2 Wiki crud
	@Override
	public V2WikiPage createV2WikiPage(String ownerId, String ownerType,
			String wikiPageJson) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(
					adapterFactory);
			@SuppressWarnings("unchecked")
			V2WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,
					V2WikiPage.class);
			return synapseClient.createV2WikiPage(ownerId,
					ObjectType.valueOf(ownerType), page);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public V2WikiPage getV2WikiPage(org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient,
						key.getOwnerObjectId(),
						ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			return synapseClient.getV2WikiPage(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public V2WikiPage getVersionOfV2WikiPage(
			org.sagebionetworks.web.shared.WikiPageKey key, Long version)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient,
						key.getOwnerObjectId(),
						ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			return synapseClient.getVersionOfV2WikiPage(
					properKey, version);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getPlainTextWikiPage(
			org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException, IOException {
		String markdown = getMarkdown(key);
		String html = SynapseMarkdownProcessor.getInstance().markdown2Html(
				markdown, false, null);
		String plainText = Jsoup.clean(html, "", Whitelist.none(),
				new Document.OutputSettings().prettyPrint(false));
		return plainText;
	}

	@Override
	public V2WikiPage updateV2WikiPage(String ownerId, String ownerType,
			V2WikiPage page) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateV2WikiPage(ownerId,
					ObjectType.valueOf(ownerType), page);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public V2WikiPage restoreV2WikiPage(String ownerId, String ownerType,
			String wikiId, Long versionToUpdate) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.restoreV2WikiPage(ownerId,
					ObjectType.valueOf(ownerType), wikiId, versionToUpdate);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public void deleteV2WikiPage(org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			synapseClient.deleteV2WikiPage(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public PaginatedResults<V2WikiHeader> getV2WikiHeaderTree(String ownerId, String ownerType)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient
					.getV2WikiHeaderTree(ownerId, ObjectType.valueOf(ownerType)));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public V2WikiOrderHint getV2WikiOrderHint(org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
				key.getOwnerObjectId(),
				ObjectType.valueOf(key.getOwnerObjectType()),
				key.getWikiPageId());
		try {
			V2WikiOrderHint orderHint = synapseClient.getV2OrderHint(properKey);
			return orderHint;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public V2WikiOrderHint updateV2WikiOrderHint(V2WikiOrderHint toUpdate) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			V2WikiOrderHint orderHint = synapseClient.updateV2WikiOrderHint(toUpdate);
			return orderHint;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public FileHandleResults getV2WikiAttachmentHandles(
			org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient,
						key.getOwnerObjectId(),
						ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			return synapseClient
					.getV2WikiAttachmentHandles(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public FileHandleResults getVersionOfV2WikiAttachmentHandles(
			org.sagebionetworks.web.shared.WikiPageKey key, Long version)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				// asking for the root. find the root id first
				String rootWikiPage = getRootWikiId(synapseClient,
						key.getOwnerObjectId(),
						ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			return synapseClient
					.getVersionOfV2WikiAttachmentHandles(properKey, version);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public PaginatedResults<V2WikiHistorySnapshot> getV2WikiHistory(
			org.sagebionetworks.web.shared.WikiPageKey key, Long limit,
			Long offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
					key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()),
					key.getWikiPageId());
			return convertPaginated(synapseClient
					.getV2WikiHistory(properKey, limit, offset));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getMarkdown(org.sagebionetworks.web.shared.WikiPageKey key)
			throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		String wikiPageKeyId = getWikiKeyId(synapseClient, key);
		WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
				key.getOwnerObjectId(),
				ObjectType.valueOf(key.getOwnerObjectType()), wikiPageKeyId);
		try {
			return synapseClient.downloadV2WikiMarkdown(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getVersionOfMarkdown(
			org.sagebionetworks.web.shared.WikiPageKey key, Long version)
			throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
				key.getOwnerObjectId(),
				ObjectType.valueOf(key.getOwnerObjectType()),
				key.getWikiPageId());
		try {
			return synapseClient.downloadVersionOfV2WikiMarkdown(properKey,
					version);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public S3FileHandle zipAndUploadFile(String content, String fileName)
			throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		File file = zipUp(content, fileName);
		String contentType = guessContentTypeFromStream(file);
		try {
			// Upload the file and create S3 handle
			S3FileHandle handle = synapseClient.createFileHandle(file,
					contentType);
			return handle;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (IOException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private File zipUp(String content, String fileName) throws IOException {
		// Create a temporary file to write content to
		File tempFile = File.createTempFile(fileName, ".tmp");
		if (content != null) {
			FileUtils.writeByteArrayToFile(tempFile, content.getBytes());
		} else {
			// When creating a wiki for the first time, markdown content doesn't
			// exist
			// Uploaded file should be empty
			byte[] emptyByteArray = new byte[0];
			FileUtils.writeByteArrayToFile(tempFile, emptyByteArray);
		}
		return tempFile;
	}

	private static String guessContentTypeFromStream(File file)
			throws FileNotFoundException, IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			// Let java guess from the stream.
			String contentType = URLConnection.guessContentTypeFromStream(is);
			// If Java fails then set the content type to be octet-stream
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			return contentType;
		} finally {
			is.close();
		}
	}

	@Override
	public WikiPage createV2WikiPageWithV1(String ownerId, String ownerType,
			WikiPage page) throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createWikiPage(ownerId, ObjectType.valueOf(ownerType), page);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public WikiPage updateV2WikiPageWithV1(String ownerId, String ownerType,
			WikiPage page) throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateWikiPage(ownerId, ObjectType.valueOf(ownerType), page);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private String getWikiKeyId(
			org.sagebionetworks.client.SynapseClient synapseClient,
			org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		String wikiPageId = key.getWikiPageId();
		if (wikiPageId == null) {
			// asking for the root. find the root id first
			wikiPageId = getRootWikiId(synapseClient, key.getOwnerObjectId(),
					ObjectType.valueOf(key.getOwnerObjectType()));
		}
		return wikiPageId;
	}

	@Override
	public WikiPage getV2WikiPageAsV1(
			org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException, IOException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
				key.getOwnerObjectId(),
				ObjectType.valueOf(key.getOwnerObjectType()),
				getWikiKeyId(synapseClient, key));
		String etag = null;
		try {
			V2WikiPage page = synapseClient.getV2WikiPage(properKey);
			etag = page.getEtag();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}

		MarkdownCacheRequest request = new MarkdownCacheRequest(properKey,
				etag, null);
		return processMarkdownRequest(request);
	}

	@Override
	public WikiPage getVersionOfV2WikiPageAsV1(
			org.sagebionetworks.web.shared.WikiPageKey key, Long version)
			throws RestServiceException, IOException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = WikiPageKeyHelper.createWikiPageKey(
				key.getOwnerObjectId(),
				ObjectType.valueOf(key.getOwnerObjectType()),
				getWikiKeyId(synapseClient, key));
		String etag = null;
		try {
			V2WikiPage page = synapseClient.getVersionOfV2WikiPage(properKey,
					version);
			etag = page.getEtag();
			key.setWikiPageId(page.getId());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}

		MarkdownCacheRequest request = new MarkdownCacheRequest(properKey,
				etag, version);
		return processMarkdownRequest(request);
	}

	private WikiPage processMarkdownRequest(MarkdownCacheRequest request)
			throws RestServiceException {
		try {
			return wiki2Markdown.get(request);
		} catch (ExecutionException e) {
			if (e.getCause() != null
					&& e.getCause() instanceof SynapseException)
				throw ExceptionUtil
						.convertSynapseException((SynapseException) e
								.getCause());
			else
				throw new RestServiceException(e.getMessage());
		}
	}

	@Override
	public EntityHeader addFavorite(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.addFavorite(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void removeFavorite(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.removeFavorite(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public List<EntityHeader> getFavorites()
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<EntityHeader> favorites = synapseClient
					.getFavorites(MAX_LIMIT, ZERO_OFFSET);
			List<EntityHeader> headers = favorites.getResults();
			//sort by name
			Collections.sort(headers, new Comparator<EntityHeader>() {
		        @Override
		        public int compare(EntityHeader o1, EntityHeader o2) {
		        	return o1.getName().compareToIgnoreCase(o2.getName());
		        }
			});
			return headers;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public String createTeam(String teamName) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Team t = new Team();
			t.setName(teamName);
			t.setCanPublicJoin(false);
			t = synapseClient.createTeam(t);
			return t.getId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public void deleteTeam(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteTeam(teamId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public TeamMemberPagedResults getTeamMembers(String teamId, String fragment, Integer limit,
			Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<TeamMember> members = synapseClient
					.getTeamMembers(teamId, fragment, limit, offset);
			List<TeamMember> teamMembers = members.getResults();
			
			//gather user ids to ask for all user profiles in bulk
			List<Long> userIds = new ArrayList<Long>();
			for (TeamMember member : members.getResults()) {
				userIds.add(Long.parseLong(member.getMember().getOwnerId()));
			}
			List<UserProfile> profiles = synapseClient.listUserProfiles(userIds);
			List<TeamMemberBundle> teamMemberBundles = new ArrayList<TeamMemberBundle>();
			for (int i = 0; i < userIds.size(); i++) {
				teamMemberBundles.add(new TeamMemberBundle(profiles.get(i), teamMembers.get(i).getIsAdmin(), teamMembers.get(i).getTeamId()));
			}
			TeamMemberPagedResults results = new TeamMemberPagedResults();
			results.setResults(teamMemberBundles);
			results.setTotalNumberOfResults(members.getTotalNumberOfResults());
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public List<UserProfile> listUserProfiles(List<String> userIds) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			List<Long> userIdsLong = new LinkedList<Long>();
			for (String idString :userIds) {
				userIdsLong.add(Long.parseLong(idString));
			}
			return synapseClient.listUserProfiles(userIdsLong);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public List<Team> getTeamsForUser(String userId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<Team> teams = synapseClient.getTeamsForUser(
					userId, MAX_LIMIT, ZERO_OFFSET);
			List<Team> teamList = teams.getResults();
			Collections.sort(teamList, new Comparator<Team>() {
		        @Override
		        public int compare(Team o1, Team o2) {
		        	return o1.getName().compareToIgnoreCase(o2.getName());
		        }
			});
			return teamList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public PaginatedResults<Team> getTeams(String userId, Integer limit, Integer offset)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient.getTeamsForUser(
					userId, limit, offset));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public PaginatedResults<Team> getTeamsBySearch(String searchTerm, Integer limit,
			Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (searchTerm != null && searchTerm.trim().length() == 0)
				searchTerm = null;
			if (offset == null)
				offset = ZERO_OFFSET.intValue();
			return convertPaginated(synapseClient.getTeams(searchTerm,
					limit, offset));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	public TeamMembershipStatus getTeamMembershipState(String currentUserId, String teamId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient
					.getTeamMembershipStatus(teamId, currentUserId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public void requestMembership(String currentUserId, String teamId,
			String message) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient
					.getTeamMembershipStatus(teamId, currentUserId);
			// if we can join the team without creating the request (like if we
			// are a team admin, or there is an open invitation), then just do
			// that!
			if (membershipStatus.getCanJoin()) {
				synapseClient.addTeamMember(teamId, currentUserId);
			} else if (!membershipStatus.getHasOpenRequest()) {
				// otherwise, create the request
				MembershipRqstSubmission membershipRequest = new MembershipRqstSubmission();
				membershipRequest.setMessage(message);
				membershipRequest.setTeamId(teamId);
				membershipRequest.setUserId(currentUserId);

				// make new Synapse call
				synapseClient.createMembershipRequest(membershipRequest);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void inviteMember(String userGroupId, String teamId, String message)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient
					.getTeamMembershipStatus(teamId, userGroupId);
			// if we can join the team without creating the invite (like if we
			// are a team admin, or there is an open membership request), then
			// just do that!
			if (membershipStatus.getCanJoin()) {
				synapseClient.addTeamMember(teamId, userGroupId);
			} else if (!membershipStatus.getHasOpenInvitation()) {
				// check to see if there is already an open invite
				MembershipInvtnSubmission membershipInvite = new MembershipInvtnSubmission();
				membershipInvite.setMessage(message);
				membershipInvite.setTeamId(teamId);
				membershipInvite.setInviteeId(userGroupId);

				// make new Synapse call
				synapseClient.createMembershipInvitation(membershipInvite);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getCertifiedUserPassingRecord(String userId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PassingRecord passingRecord = synapseClient
					.getCertifiedUserPassingRecord(userId);
			// This method only returns the PassingRecord if the user actually
			// passed (portal does not currently care about the top failed
			// attempt).
			if (passingRecord.getPassed() == null || !passingRecord.getPassed()) {
				throw new NotFoundException(
						"The user has not passed the certification quiz.");
			}
			JSONObjectAdapter passingRecordJson = passingRecord
					.writeToJSONObject(adapterFactory.createNew());
			return passingRecordJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getCertificationQuiz() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Quiz quiz = synapseClient.getCertifiedUserTest();
			JSONObjectAdapter quizJson = quiz.writeToJSONObject(adapterFactory
					.createNew());
			return quizJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String submitCertificationQuizResponse(String quizResponseJson)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(
					adapterFactory);
			QuizResponse response = jsonEntityFactory.createEntity(
					quizResponseJson, QuizResponse.class);
			PassingRecord passingRecord = synapseClient
					.submitCertifiedUserTestResponse(response);
			JSONObjectAdapter passingRecordJson = passingRecord
					.writeToJSONObject(adapterFactory.createNew());
			return passingRecordJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	public Boolean isTeamMember(String userId, Long groupPrincipalId)
			throws RestServiceException {
		Boolean isMember = null;
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient
					.getTeamMembershipStatus(groupPrincipalId.toString(),
							userId);
			isMember = membershipStatus.getIsMember();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
		return isMember;
	}

	@Override
	public TeamBundle getTeamBundle(String userId, String teamId,
			boolean isLoggedIn) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<TeamMember> allMembers = synapseClient
					.getTeamMembers(teamId, null, 1, ZERO_OFFSET);
			long memberCount = allMembers.getTotalNumberOfResults();
			boolean isAdmin = false;
			Team team = synapseClient.getTeam(teamId);
			TeamMembershipStatus membershipStatus = null;
			// get membership state for the current user
			if (isLoggedIn) {
				membershipStatus = synapseClient
						.getTeamMembershipStatus(teamId, userId);
				if (membershipStatus.getIsMember()) {
					TeamMember teamMember = synapseClient.getTeamMember(teamId,
							userId);
					isAdmin = teamMember.getIsAdmin();
				}
			}
			return new TeamBundle(team, memberCount,
					membershipStatus, isAdmin);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public List<MembershipRequestBundle> getOpenRequests(String teamId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<MembershipRequest> requests = synapseClient
					.getOpenMembershipRequests(teamId, null, MAX_LIMIT,
							ZERO_OFFSET);
			// and ask for the team info for each invite, and fill that in the
			// bundle

			ArrayList<MembershipRequestBundle> returnList = new ArrayList<MembershipRequestBundle>();
			// now go through and create a MembershipRequestBundle for each pair

			for (MembershipRequest request : requests.getResults()) {
				UserProfile profile = synapseClient.getUserProfile(request.getUserId());
				MembershipRequestBundle b = new MembershipRequestBundle(profile, request);
				returnList.add(b);
			}
			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	private boolean isTeamAdmin(String currentUserId, String teamId, org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException {
		TeamMember member = synapseClient.getTeamMember(teamId, currentUserId);
		return member.getIsAdmin();
	}
	
	@Override
	public Long getOpenRequestCount(String currentUserId, String teamId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// must be an admin to the team open requests. To get admin status,
			// must be a member
			if (isTeamAdmin(currentUserId, teamId, synapseClient)) {
				org.sagebionetworks.repo.model.PaginatedResults<MembershipRequest> requests = synapseClient
						.getOpenMembershipRequests(teamId, null, 1, ZERO_OFFSET);
				return requests.getTotalNumberOfResults();
			} else {
				return null;
			}
				
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public ArrayList<MembershipInvitationBundle> getOpenInvitations(String userId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<MembershipInvitation> invitations = synapseClient
					.getOpenMembershipInvitations(userId, null, MAX_LIMIT,
							ZERO_OFFSET);
			// and ask for the team info for each invite, and fill that in the
			// bundle

			ArrayList<MembershipInvitationBundle> returnList = new ArrayList<MembershipInvitationBundle>();
			// now go through and create a MembershipInvitationBundle for each
			// pair

			for (MembershipInvitation invite : invitations.getResults()) {
				Team team = synapseClient.getTeam(invite.getTeamId());
				MembershipInvitationBundle b = new MembershipInvitationBundle(team, invite);
				returnList.add(b);
			}

			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public ArrayList<MembershipInvitationBundle> getOpenTeamInvitations(
			String teamId, Integer limit, Integer offset)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			org.sagebionetworks.repo.model.PaginatedResults<MembershipInvtnSubmission> invitations = synapseClient
					.getOpenMembershipInvitationSubmissions(teamId, null,
							limit, offset);
			// and ask for the team info for each invite, and fill that in the
			// bundle

			ArrayList<MembershipInvitationBundle> returnList = new ArrayList<MembershipInvitationBundle>();
			// now go through and create a MembershipInvitationBundle for each
			// pair

			for (MembershipInvtnSubmission invite : invitations.getResults()) {
				UserProfile profile = synapseClient.getUserProfile(invite
						.getInviteeId());
				MembershipInvitationBundle b = new MembershipInvitationBundle(invite,
						profile);
				returnList.add(b);
			}

			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public void deleteMembershipInvitation(String invitationId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteMembershipInvitation(invitationId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void setIsTeamAdmin(String currentUserId, String targetUserId,
			String teamId, boolean isTeamAdmin) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.setTeamMemberPermissions(teamId, targetUserId,
					isTeamAdmin);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteTeamMember(String currentUserId, String targetUserId,
			String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.removeTeamMember(teamId, targetUserId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String updateTeam(String teamJson) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(
					adapterFactory);
			Team team = jsonEntityFactory.createEntity(teamJson, Team.class);
			Team updatedTeam = synapseClient.updateTeam(team);
			JSONObjectAdapter updatedTeamJson = updatedTeam
					.writeToJSONObject(adapterFactory.createNew());
			return updatedTeamJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public EntityIdList getDescendants(String nodeId, int pageSize,
			String lastDescIdExcl) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getDescendants(nodeId,
					pageSize, lastDescIdExcl);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Doi getEntityDoi(String entityId, Long versionNumber)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getEntityDoi(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (Exception e) {
			throw ExceptionUtil
					.convertSynapseException(new SynapseNotFoundException()); 
		}
	}

	@Override
	public void createDoi(String entityId, Long versionNumber)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.createEntityDoi(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getFileEntityTemporaryUrlForVersion(String entityId,
			Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			URL url = synapseClient.getFileEntityTemporaryUrlForVersion(
					entityId, versionNumber);
			return url.toString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}

	}


	@Override
	public ChunkedFileToken getChunkedFileToken(String fileName, String contentType,
			String contentMD5, Long storageLocationId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			CreateChunkedFileTokenRequest ccftr = new CreateChunkedFileTokenRequest();
			ccftr.setFileName(fileName);
			ccftr.setContentType(contentType);
			ccftr.setContentMD5(contentMD5);
			ccftr.setStorageLocationId(storageLocationId);
			// Start the upload
			return synapseClient.createChunkedFileUploadToken(ccftr);

		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getChunkedPresignedUrl(ChunkRequest request)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createChunkedPresignedUrl(request).toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UploadDaemonStatus combineChunkedFileUpload(List<ChunkRequest> requests)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// reconstruct all part numbers, and token
			ChunkedFileToken token = null;
			List<Long> parts = new ArrayList<Long>();

			for (ChunkRequest request : requests) {
				token = request.getChunkedFileToken();
				parts.add(request.getChunkNumber());
			}

			CompleteAllChunksRequest cacr = new CompleteAllChunksRequest();
			cacr.setChunkedFileToken(token);
			cacr.setChunkNumbers(parts);

			// Start the daemon
			return synapseClient.startUploadDeamon(cacr);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public UploadDaemonStatus getUploadDaemonStatus(String daemonId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			UploadDaemonStatus status = synapseClient
					.getCompleteUploadDaemonStatus(daemonId);
			if (State.FAILED == status.getState()) {
				logError(status.getErrorMessage());
			}
			return status;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}

	/**
	 * Gets the ID of the file entity with the given name whose parent has the given ID.
	 * 
	 * @param fileName The name of the entity to find.
	 * @param parentEntityId The ID of the parent that the found entity must have.
	 * @return The ID of the file entity with the given name and parent ID.
	 * @throws NotFoundException If no file with given name and parent ID was found.
	 * @throws ConflictException If an entity with given name and parent ID was found, but that
	 * 							 entity was not a File Entity.
	 */
	@Override
	public String getFileEntityIdWithSameName(String fileName, String parentEntityId) throws RestServiceException, SynapseException {
		String queryString =  	"select * from entity where parentId == '" + parentEntityId +
								WebConstants.AND_NAME_EQUALS + fileName + WebConstants.LIMIT_ONE;
		JSONObject query = query(queryString);
		if (query == null) {
			throw new SynapseClientException("Query service call returned null");
		}
		if(!query.has("totalNumberOfResults")){
			throw new SynapseClientException("Query results did not have "+"totalNumberOfResults");
		}
		try {
			if (query.getLong("totalNumberOfResults") != 0) {
				JSONObject result = query.getJSONArray("results").getJSONObject(0);
				
				// Get types associated with found entity.
				JSONArray typeArray = result.getJSONArray("entity.concreteType");
				Set<String> types = new HashSet<String>();
				for (int i = 0; i < typeArray.length(); i++) {
					types.add(typeArray.getString(i));
				}
				
				if (types.contains(FileEntity.class.getName())) {
					// The found entity is a File Entity.
					return result.getString("entity.id");
				} else {
					// The found entity is not a File Entity.
					throw new ConflictException("An non-file entity with name " + fileName + " and parentId " + parentEntityId + " already exists.");
				}
			} else {
				throw new NotFoundException("An entity with name " + fileName + " and parentId " + parentEntityId + " was not found.");
			}
		} catch (JSONException e) {
			throw new SynapseClientException(e);
		}
	}
	
	@Override
	public String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			//create entity if we have to
			FileEntity fileEntity = null;
			FileHandle newHandle = synapseClient.getRawFileHandle(fileHandleId);
			if (entityId == null) {
				fileEntity = FileHandleServlet.getNewFileEntity(parentEntityId, fileHandleId, newHandle.getFileName(), synapseClient);
			}
			else {
				//get the file entity to update
				fileEntity = (FileEntity) synapseClient.getEntityById(entityId);
				//update data file handle id
				fileEntity.setDataFileHandleId(fileHandleId);
				fileEntity = (FileEntity)synapseClient.putEntity(fileEntity);
			}
			return fileEntity.getId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	public static void lockDown(String entityId, boolean isRestricted, org.sagebionetworks.client.SynapseClient client) throws SynapseException {
		// now lock down restricted data
		if (isRestricted) {
			// we only proceed if there aren't currently any access restrictions
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(entityId);
			subjectId.setType(RestrictableObjectType.ENTITY);

			VariableContentPaginatedResults<AccessRequirement> currentARs = client.getAccessRequirements(subjectId);
			if (currentARs.getTotalNumberOfResults()==0L) {
				client.createLockAccessRequirement(entityId);
			}
		}
	}

	@Override
	public String getSynapseVersions() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createAnonymousSynapseClient();
		try {
			SynapseVersionInfo versionInfo = synapseClient.getVersionInfo();
			new PortalVersionHolder();
			return PortalVersionHolder.getVersionInfo() + ","
					+ versionInfo.getVersion();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

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

	private String getSynapseProperty(String key) {
		return PortalPropertiesHolder.getProperty(key);
	}

	@Override
	public HashMap<String, String> getSynapseProperties(){
		return PortalPropertiesHolder.getPropertiesMap();
	}

	public static class PortalPropertiesHolder {
		private static Properties props;
		private static HashMap<String, String> propsMap;

		static {
			InputStream s = SynapseClientImpl.class
					.getResourceAsStream("/portal.properties");
			props = new Properties();
			try {
				props.load(s);
			} catch (IOException e) {
				throw new RuntimeException("portal.properties file not found",
						e);
			}
		}

		public static String getProperty(String key) {
			return props.getProperty(key);
		}
		
		public static HashMap<String, String> getPropertiesMap() {
			if (propsMap == null) {
				propsMap = new HashMap<String, String>();
				for (Entry<Object, Object> entry : props.entrySet()) {
					propsMap.put(entry.getKey().toString(), entry.getValue().toString());
	}
			}
			return propsMap;
		}
	}

	@Override
	public String getAPIKey() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.retrieveApiKey();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String createColumnModel(String columnModelJson)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			ColumnModel column = new ColumnModel(
					adapterFactory.createNew(columnModelJson));
			ColumnModel createdColumn = synapseClient.createColumnModel(column);
			return createdColumn.writeToJSONObject(adapterFactory.createNew())
					.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public List<String> getColumnModelsForTableEntity(String tableEntityId)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			List<ColumnModel> columns = synapseClient
					.getColumnModelsForTableEntity(tableEntityId);
			List<String> stringList = new ArrayList<String>();
			for (ColumnModel col : columns) {
				stringList.add(col
						.writeToJSONObject(adapterFactory.createNew())
						.toJSONString());
			}
			return stringList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String sendMessage(Set<String> recipients, String subject,
			String messageBody) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			MessageToUser message = new MessageToUser();
			message.setRecipients(recipients);
			message.setSubject(subject);
			MessageToUser sentMessage = synapseClient.sendStringMessage(
					message, messageBody);
			JSONObjectAdapter sentMessageJson = sentMessage
					.writeToJSONObject(adapterFactory.createNew());
			return sentMessageJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public Boolean isAliasAvailable(String alias, String aliasType)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createAnonymousSynapseClient();
		try {
			AliasType type = AliasType.valueOf(aliasType);
			AliasCheckRequest request = new AliasCheckRequest();
			request.setAlias(alias);
			request.setType(type);
			AliasCheckResponse response = synapseClient
					.checkAliasAvailable(request);
			return response.getAvailable();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	private static long sequence = 0;


	private void handleTableUnavailableException(
			SynapseTableUnavailableException e) throws TableUnavilableException {
		try {
			throw new TableUnavilableException(e.getStatus()
					.writeToJSONObject(adapterFactory.createNew())
					.toJSONString());
		} catch (JSONObjectAdapterException e1) {
			throw new TableUnavilableException(e.getMessage());
		}
	}

	@Override
	public HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> getHelpPages()
			throws RestServiceException {
		initHelpPagesMap();
		return pageName2WikiKeyMap;
	}

	private void initHelpPagesMap() {
		if (pageName2WikiKeyMap == null) {
			HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> tempMap = new HashMap<String, org.sagebionetworks.web.shared.WikiPageKey>();
			tempMap.put(
					WebConstants.GETTING_STARTED,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.GETTING_STARTED_GUIDE_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.GETTING_STARTED_GUIDE_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.CREATE_PROJECT,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.CREATE_PROJECT_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.CREATE_PROJECT_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.R_CLIENT,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.R_CLIENT_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.R_CLIENT_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.PYTHON_CLIENT,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.PYTHON_CLIENT_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.PYTHON_CLIENT_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.COMMAND_LINE_CLIENT,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.PYTHON_CLIENT_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.PYTHON_CLIENT_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.FORMATTING_GUIDE,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.FORMATTING_GUIDE_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.FORMATTING_GUIDE_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.CHALLENGE_PARTICIPATION_INFO,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.CHALLENGE_PARTICIPATION_INFO_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.CHALLENGE_PARTICIPATION_INFO_WIKI_ID_PROPERTY)));
			tempMap.put(
					WebConstants.GOVERNANCE,
					new org.sagebionetworks.web.shared.WikiPageKey(
							getSynapseProperty(WebConstants.GOVERNANCE_ENTITY_ID_PROPERTY),
							ObjectType.ENTITY.toString(),
							getSynapseProperty(WebConstants.GOVERNANCE_WIKI_ID_PROPERTY)));
			
			//Workshop
			addHelpPageMapping(tempMap, WebConstants.COLLABORATORIUM, WebConstants.COLLABORATORIUM_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_I, WebConstants.STAGE_I_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_II, WebConstants.STAGE_II_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_III, WebConstants.STAGE_III_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_IV, WebConstants.STAGE_IV_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_V, WebConstants.STAGE_V_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_VI, WebConstants.STAGE_VI_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_VII, WebConstants.STAGE_VII_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_VIII, WebConstants.STAGE_VIII_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_IX, WebConstants.STAGE_IX_ENTITY_ID_PROPERTY, null);
			addHelpPageMapping(tempMap, WebConstants.STAGE_X, WebConstants.STAGE_X_ENTITY_ID_PROPERTY, null);
			
			pageName2WikiKeyMap = tempMap;
		}
	}
	
	private void addHelpPageMapping(HashMap<String, org.sagebionetworks.web.shared.WikiPageKey> mapping, String token, String entityIdPropertyKey, String wikiIdPropertyKey) {
		String wikiIdProperty = wikiIdPropertyKey != null ? getSynapseProperty(wikiIdPropertyKey) : "";
		mapping.put(
				token,
				new org.sagebionetworks.web.shared.WikiPageKey(
						getSynapseProperty(entityIdPropertyKey),
						ObjectType.ENTITY.toString(),
						wikiIdProperty));
	}

	public Set<String> getWikiBasedEntities() throws RestServiceException {
		initWikiEntities();
		return wikiBasedEntities;
	}

	private void initWikiEntities() {
		if (wikiBasedEntities == null) {
			HashSet<String> tempSet = new HashSet<String>();
			tempSet.add(getSynapseProperty(WebConstants.GETTING_STARTED_GUIDE_ENTITY_ID_PROPERTY));
			tempSet.add(getSynapseProperty(WebConstants.CREATE_PROJECT_ENTITY_ID_PROPERTY));
			tempSet.add(getSynapseProperty(WebConstants.R_CLIENT_ENTITY_ID_PROPERTY));
			tempSet.add(getSynapseProperty(WebConstants.PYTHON_CLIENT_ENTITY_ID_PROPERTY));
			tempSet.add(getSynapseProperty(WebConstants.FORMATTING_GUIDE_ENTITY_ID_PROPERTY));
			// because wikiBasedEntities is volatile, current state will be
			// reflected in all threads
			wikiBasedEntities = tempSet;
		}
	}

	@Override
	public String deleteApiKey() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.invalidateApiKey();
			return getAPIKey();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String deleteRowsFromTable(String toDelete)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RowSelection toDeleteSet = new RowSelection(
					adapterFactory.createNew(toDelete));
			RowReferenceSet responseSet = synapseClient
					.deleteRowsFromTable(toDeleteSet);
			return responseSet.writeToJSONObject(adapterFactory.createNew())
					.toJSONString();
		} catch (SynapseTableUnavailableException e) {
			try {
				throw new TableUnavilableException(e.getStatus()
						.writeToJSONObject(adapterFactory.createNew())
						.toJSONString());
			} catch (JSONObjectAdapterException e1) {
				throw new TableUnavilableException(e.getMessage());
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public void setTableSchema(TableEntity table, List<ColumnModel> models)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// Create any models that do not have an ID
			List<String> newSchema = new LinkedList<String>();
			for (ColumnModel m : models) {
				if (m.getId() == null) {
					ColumnModel clone = synapseClient.createColumnModel(m);
					m.setId(clone.getId());
				}
				newSchema.add(m.getId());
			}
			// Get the table
			table.setColumnIds(newSchema);
			table = synapseClient.putEntity(table);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	
	@Override
	public void validateTableQuery(String sql) throws RestServiceException {
		try {
			TableQueryParser.parserQuery(sql);
		} catch (ParseException e) {
			throw new TableQueryParseException(e.getMessage());
		}
	}
	
	@Override
	public String toggleSortOnTableQuery(String sql, String header)	throws RestServiceException {
		try {
			return TableSqlProcessor.toggleSort(sql, header);
		} catch (ParseException e) {
			throw new TableQueryParseException(e.getMessage());
		}
	}
	
	@Override
	public List<SortItem> getSortFromTableQuery(String sql)
			throws RestServiceException {
		try {
			return TableSqlProcessor.getSortingInfo(sql);
		} catch (ParseException e) {
			throw new TableQueryParseException(e.getMessage());
		}
	}

	
	@Override
	public String startAsynchJob(AsynchType type, AsynchronousRequestBody body)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.startAsynchJob(AsynchJobType.valueOf(type.name()), body);
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public AsynchronousResponseBody getAsynchJobResults(AsynchType type, String jobId, AsynchronousRequestBody body)
			throws RestServiceException, ResultNotReadyException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.getAsyncResult(AsynchJobType.valueOf(type.name()), jobId, body);
		} catch (SynapseResultNotReadyException e){
			// This occurs when the job is not ready.
			// Re-throw the ResultNotReadyException with the status JSON.
			throw new ResultNotReadyException(e.getJobStatus());
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public EntityQueryResults executeEntityQuery(EntityQuery query) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.entityQuery(query);
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public Entity createEntity(Entity entity) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.createEntity(entity);
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public FileHandle getFileHandle(String fileHandleId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.getRawFileHandle(fileHandleId);
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	
	@Override
	public String createFileHandleURL(String fileHandleId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			URL url = synapseClient.getFileHandleTemporaryUrl(fileHandleId);
			return url.toString();
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (IOException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}

	@Override
	public List<ColumnModel> createTableColumns(List<ColumnModel> models) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.createColumnModels(models);
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public List<UploadDestination> getUploadDestinations(String parentEntityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			return synapseClient.getUploadDestinations(parentEntityId);
		}catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	public ProjectPagedResults getMyProjects(ProjectListType projectListType, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			List<ProjectHeader> headers = (List<ProjectHeader>)synapseClient.getMyProjects(projectListType, sortBy, sortDir, limit, offset).getResults();
			List<String> lastModifiedBy = new LinkedList<String>();
			for (ProjectHeader header: headers) {
				if (header.getModifiedBy() != null)
					lastModifiedBy.add(header.getModifiedBy().toString());
			}			
			return new ProjectPagedResults(headers, headers.size(), listUserProfiles(lastModifiedBy));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	
	public ProjectPagedResults getProjectsForTeam(String teamId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Long teamIdLong = Long.parseLong(teamId);
			List<ProjectHeader> headers = (List<ProjectHeader>)synapseClient.getProjectsForTeam(teamIdLong, sortBy, sortDir, limit, offset).getResults();
			List<String> lastModifiedBy = new LinkedList<String>();
			for (ProjectHeader header: headers) {
				if (header.getModifiedBy() != null)
					lastModifiedBy.add(header.getModifiedBy().toString());
			}
			return new ProjectPagedResults(headers, headers.size(), listUserProfiles(lastModifiedBy));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	
	
	public ProjectPagedResults getUserProjects(String userId, int limit, int offset, ProjectListSortColumn sortBy, SortDirection sortDir) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Long userIdLong = Long.parseLong(userId);
			List<ProjectHeader> headers = (List<ProjectHeader>)synapseClient.getProjectsFromUser(userIdLong, sortBy, sortDir, limit, offset).getResults();
			List<String> lastModifiedBy = new LinkedList<String>();
			for (ProjectHeader header: headers) {
				if (header.getModifiedBy() != null)
					lastModifiedBy.add(header.getModifiedBy().toString());
			}
			return new ProjectPagedResults(headers, headers.size(), listUserProfiles(lastModifiedBy));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	public static int safeLongToInt(long l) {
       if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
           throw new IllegalArgumentException
               ("Cannot safely cast "+l+" to int without changing the value.");
       }
       return (int) l;
   }

	public String getHost(String urlString) throws RestServiceException {
		if (urlString == null || urlString.length() == 0) {
			throw new IllegalArgumentException("url is required");
		}
		//URL does not recognize sftp:// protocol.  replace with http (we're after the host in this method)
		if (urlString.toLowerCase().startsWith(WebConstants.SFTP_PREFIX)) {
			urlString = "http://" + urlString.substring(WebConstants.SFTP_PREFIX.length());
		}
		try {
			URL url = new URL(urlString);
			return url.getHost();
		} catch (MalformedURLException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public TableFileHandleResults getTableFileHandle(RowReferenceSet set) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getFileHandlesFromTable(set);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	public void updateAnnotations(String entityId, Annotations annotations) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.updateAnnotations(entityId, annotations);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public void deleteOpenMembershipRequests(String currentUserId, String teamId)
			throws RestServiceException {
		// This method does nothing?
		
	}

}
