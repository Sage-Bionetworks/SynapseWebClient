package org.sagebionetworks.web.server.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseForbiddenException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
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
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.MembershipRqstSubmission;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.doi.Doi;
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
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
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
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.server.SynapseMarkdownProcessor;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityConstants;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.MembershipRequestBundle;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SynapseClientImpl extends RemoteServiceServlet implements
		SynapseClient, TokenProvider {
	static private Log log = LogFactory.getLog(SynapseClientImpl.class);
	// This will be appened to the User-Agent header.
	private static final String PORTAL_USER_AGENT = "Synapse-Web-Client/"+PortalVersionHolder.getVersionInfo();
	static {//kick off initialization (like pattern compilation) by referencing it
			SynapseMarkdownProcessor.getInstance();
		}
	
	private TokenProvider tokenProvider = this;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	AutoGenFactory entityFactory = new AutoGenFactory();
	
	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;

	/**
	 * Essentially the constructor. Setup org.sagebionetworks.client.SynapseClient client.
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
	 * This allows tests provide mock org.sagebionetworks.client.SynapseClient ojbects
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
	 * SynapseClient Service Methods
	 */
	@Override
	public EntityWrapper getEntity(String entityId) throws RestServiceException {
		return getEntityForVersion(entityId, null);
	}

	@Override
	public EntityWrapper getEntityForVersion(String entityId, Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Entity entity;
			if(versionNumber == null) {				
				entity = synapseClient.getEntityById(entityId);
			} else {
				entity = synapseClient.getEntityByIdForVersion(entityId, versionNumber);
			}
			JSONObjectAdapter entityJson = entity
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(entityJson.toJSONString(), entity.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
		
	@Override
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask)
			throws RestServiceException {
		try {			
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();			
			EntityBundle eb;
			//TODO:remove the try catch when PLFM-1752 is fixed
			try {
				eb = synapseClient.getEntityBundle(entityId, partsMask);
				} catch(SynapseNotFoundException e) {
				//if we're trying to get the filehandles, then give another try without the filehandles
				if ((EntityBundleTransport.FILE_HANDLES & partsMask)!=0) {
					int newPartsMask = (~EntityBundleTransport.FILE_HANDLES) & partsMask;
					eb = synapseClient.getEntityBundle(entityId, newPartsMask);
				}
				else throw e;
			}
			return convertBundleToTransport(entityId, eb, partsMask);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public EntityBundleTransport getEntityBundleForVersion(String entityId,
			Long versionNumber, int partsMask) throws RestServiceException {
		try {			
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			EntityBundle eb;
			//TODO:remove the try catch when PLFM-1752 is fixed
			try {
				eb = synapseClient.getEntityBundle(entityId, versionNumber, partsMask);
			} catch(SynapseNotFoundException e) {
				//if we're trying to get the filehandles, then give another try without the filehandles
				if ((EntityBundleTransport.FILE_HANDLES & partsMask)!=0) {
					int newPartsMask = (~EntityBundleTransport.FILE_HANDLES) & partsMask;
					eb = synapseClient.getEntityBundle(entityId, versionNumber, newPartsMask);
				}
				else throw e;
			}
			
			return convertBundleToTransport(entityId, eb, partsMask);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String getEntityVersions(String entityId, int offset, int limit)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<VersionInfo> versions = synapseClient.getEntityVersions(entityId, offset, limit);
			JSONObjectAdapter entityJson = versions.writeToJSONObject(adapterFactory.createNew());
			return entityJson.toJSONString();
		} catch (SynapseException e) {
			log.error(e);
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			// Since we are not throwing errors, log them
			log.error(e);
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public EntityWrapper getEntityPath(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			EntityPath entityPath = synapseClient.getEntityPath(entityId);
			JSONObjectAdapter entityPathJson = entityPath
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(entityPathJson.toJSONString(), entityPath.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public EntityWrapper search(String searchQueryJson) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
			SearchResults searchResults = synapseClient.search(new SearchQuery(
					adapter.createNew(searchQueryJson)));
			searchResults.writeToJSONObject(adapter);
			return new EntityWrapper(adapter.toJSONString(), SearchResults.class.getName());
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

	// Convert repo-side EntityBundle to serializable EntityBundleTransport
	private EntityBundleTransport convertBundleToTransport(String entityId, 
			EntityBundle eb, int partsMask) throws RestServiceException {
		EntityBundleTransport ebt = new EntityBundleTransport();
		try {
			if ((EntityBundleTransport.ENTITY & partsMask) > 0) {
				Entity e = eb.getEntity();
				ebt.setEntityJson(EntityFactory.createJSONStringForEntity(e));
			}
			if ((EntityBundleTransport.ANNOTATIONS & partsMask) > 0) {
				Annotations a = eb.getAnnotations();
				ebt.setAnnotationsJson(EntityFactory.createJSONStringForEntity(a));
			}
			if ((EntityBundleTransport.PERMISSIONS & partsMask) > 0) {
				UserEntityPermissions uep = eb.getPermissions();
				ebt.setPermissionsJson(EntityFactory.createJSONStringForEntity(uep));
			}
			if ((EntityBundleTransport.ENTITY_PATH & partsMask) > 0) {
				EntityPath path = eb.getPath();
				ebt.setEntityPathJson(EntityFactory.createJSONStringForEntity(path));
			}
			if ((EntityBundleTransport.ENTITY_REFERENCEDBY & partsMask) > 0) {
				List<EntityHeader> rbList = eb.getReferencedBy();
				PaginatedResults<EntityHeader> rb = new PaginatedResults<EntityHeader>();
				rb.setResults(rbList);
				ebt.setEntityReferencedByJson(EntityFactory.createJSONStringForEntity(rb));
			}
			if ((EntityBundleTransport.HAS_CHILDREN & partsMask) > 0) {
				Boolean hasChildren = eb.getHasChildren();
				ebt.setHashChildren(hasChildren);
			}
			if ((EntityBundleTransport.ACL & partsMask) > 0) {
				AccessControlList acl = eb.getAccessControlList();
				if (acl == null) {
					// ACL is inherited; fetch benefactor ACL.
					try {
						acl = getAcl(entityId);
					} catch (SynapseException e) {
						e.printStackTrace();
					}
				}
				ebt.setAclJson(EntityFactory.createJSONStringForEntity(acl));
			}
			if ((EntityBundleTransport.ACCESS_REQUIREMENTS & partsMask)!=0) {
				ebt.setAccessRequirementsJson(createJSONStringFromArray(eb.getAccessRequirements()));
			}
			if ((EntityBundleTransport.UNMET_ACCESS_REQUIREMENTS & partsMask)!=0) {
				ebt.setUnmetAccessRequirementsJson(createJSONStringFromArray(eb.getUnmetAccessRequirements()));
			}
			if ((EntityBundleTransport.FILE_HANDLES & partsMask)!=0 && eb.getFileHandles() != null)
				ebt.setFileHandlesJson(createJSONStringFromArray(eb.getFileHandles()));
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());			
		}
		return ebt;
	}
	
	public static String createJSONStringFromArray(List<? extends JSONEntity> list) throws JSONObjectAdapterException {
		JSONArrayAdapter aa = new JSONArrayAdapterImpl();
		for (int i=0; i<list.size(); i++) {
			JSONObjectAdapter oa = new JSONObjectAdapterImpl();
			list.get(i).writeToJSONObject(oa);
			aa.put(i, oa);
		}
		return aa.toJSONString();
	}
	

	/**
	 * The org.sagebionetworks.client.SynapseClient client is stateful so we must create a new one for each
	 * request
	 */	
	private org.sagebionetworks.client.SynapseClient createSynapseClient() {
		// Create a new syanpse			
		org.sagebionetworks.client.SynapseClient synapseClient = synapseProvider.createNewClient();		
		synapseClient.setSessionToken(tokenProvider.getSessionToken());
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		synapseClient.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
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
	
	// before we hit this limit we will use another mechanism to find users
	private static final int EVALUATION_PAGINATION_LIMIT = Integer.MAX_VALUE;
	private static final int EVALUATION_PAGINATION_OFFSET = 0;
	
	private static final int USER_PAGINATION_OFFSET = 0;
	// before we hit this limit we will use another mechanism to find users
	private static final int USER_PAGINATION_LIMIT = 1000; 
	private static final int GROUPS_PAGINATION_OFFSET = 0;
	// before we hit this limit we will use another mechanism to find groups
	private static final int GROUPS_PAGINATION_LIMIT = 1000;

	@Override
	public String getEntityReferencedBy(String entityId)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			PaginatedResults<EntityHeader> results = synapseClient
					.getEntityReferencedBy(entityId, null);
			return EntityFactory.createJSONStringForEntity(results);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public void logDebug(String message) {
		log.debug(message);
	}

	@Override
	public void logError(String message) {
		log.error(message);
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
	 * Update entity
	 */
	@Override
	public EntityWrapper updateEntity(String entityJson) throws RestServiceException {
		try {
			// update
			Entity entity = parseEntityFromJson(entityJson);
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			entity = synapseClient.putEntity(entity);
			
			EntityWrapper wrapper = new EntityWrapper();
			wrapper.setEntityClassName(entity.getClass().getName());
			wrapper.setEntityJson(entity.writeToJSONObject(adapterFactory.createNew()).toJSONString());
			return wrapper;			
		} catch (JSONObjectAdapterException e) {
			throw new BadRequestException(e.getMessage());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}	
	
	@Override
	public String createOrUpdateEntity(String entityJson, String annoJson,
			boolean isNew) throws RestServiceException {
		// First read the entity
		try {
			Entity entity = parseEntityFromJson(entityJson);
			Annotations annos = null;
			if (annoJson != null) {
				annos = EntityFactory.createEntityFromJSONString(annoJson,
						Annotations.class);
			}
			return createOrUpdateEntity(entity, annos, isNew);
		} catch (JSONObjectAdapterException e) {
			throw new BadRequestException(e.getMessage());
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
	public String getEntityTypeBatch(List<String> entityIds)
			throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient
					.getEntityTypeBatch(entityIds);
			return EntityFactory.createJSONStringForEntity(results);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}

	}
	

	@Override
	public String getEntityHeaderBatch(String referenceList)
			throws RestServiceException {
		try {
			ReferenceList list = new ReferenceList(new JSONObjectAdapterImpl(referenceList));
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient.getEntityHeaderBatch(list.getReferences());
			return EntityFactory.createJSONStringForEntity(results);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public List<String> getEntityHeaderBatch(List<String> entityIds)
			throws RestServiceException {
		try {
			List<Reference> list = new ArrayList<Reference>();
			for (String entityId : entityIds) {
				Reference ref = new Reference();
				ref.setTargetId(entityId);
				list.add(ref);
			}
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient.getEntityHeaderBatch(list);
			List<String> returnList = new ArrayList<String>();
			for (EntityHeader header : results.getResults()) {
				returnList.add(EntityFactory.createJSONStringForEntity(header));
			}
			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
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
	public void deleteEntityVersionById(String entityId, Long versionNumber) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			synapseClient.deleteEntityVersionById(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public String getUserProfile() throws RestServiceException {
		try	{
			//cached, in a cookie?
			UserProfile profile = UserDataProvider.getThreadLocalUserProfile(this.getThreadLocalRequest());
			if (profile == null){
				org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
				profile = synapseClient.getMyProfile();
			}
			return EntityFactory.createJSONStringForEntity(profile);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	
	@Override
	public String getUserProfile(String userId) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			UserProfile profile;
			if (userId == null) {
				profile = synapseClient.getMyProfile();
			} else {
				profile = synapseClient.getUserProfile(userId);
			}
			return EntityFactory.createJSONStringForEntity(profile);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper getUserGroupHeadersById(List<String> ids) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			UserGroupHeaderResponsePage response = synapseClient.getUserGroupHeadersByIds(ids);
			JSONObjectAdapter responseJSON = response
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(responseJSON.toJSONString(), responseJSON.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e); 
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public void updateUserProfile(String userProfileJson) throws RestServiceException {
		try {
			org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
			JSONObject userProfileJSONObject = new JSONObject(userProfileJson);
			UserProfile profile = EntityFactory.createEntityFromJSONObject(userProfileJSONObject, UserProfile.class);
			AttachmentData pic = profile.getPic();
			if (pic != null && pic.getTokenId() == null && pic.getUrl() != null){
				//special case, client provided just enough information to pull the pic from an external location (so try to store in s3 before updating the profile).
				log.info("Downloading picture from url: " + pic.getUrl());
				URL url = new URL(pic.getUrl());
				pic.setUrl(null);
				File temp = null;
				URLConnection conn = null;
				try
			    {
			    	conn = url.openConnection();
				    conn.setDoInput(true);
				    conn.setDoOutput(false);
				    temp = ServiceUtils.writeToTempFile(conn.getInputStream(), UserProfileAttachmentServlet.MAX_ATTACHMENT_SIZE_IN_BYTES);
				 	// Now upload the file
			    	String contentType = conn.getContentType();
			    	String fileName = temp.getName();
			    	if (contentType != null && contentType.equalsIgnoreCase("image/jpeg") && !fileName.toLowerCase().endsWith(".jpg"))
			    		fileName = profile.getOwnerId() + ".jpg";
					pic = synapseClient.uploadUserProfileAttachmentToSynapse(profile.getOwnerId(), temp, fileName);
				} catch (Throwable t){
					//couldn't pull the picture from the external server.  log and move on with the update
					t.printStackTrace();
				} finally{
					// Unconditionally delete the tmp file and close the input stream
					if (temp != null)
						temp.delete();
					try {
						conn.getInputStream().close();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			    profile.setPic(pic);
			}
			synapseClient.updateMyProfile(profile);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (IOException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	public AccessControlList getAcl(String id) throws SynapseException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		EntityHeader benefactor = synapseClient.getEntityBenefactor(id);
		String benefactorId = benefactor.getId();
		return synapseClient.getACL(benefactorId);
	}
	
	@Override
	public EntityWrapper getNodeAcl(String id) throws RestServiceException {
		try {
			AccessControlList acl = getAcl(id);
			JSONObjectAdapter aclJson = acl
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper createAcl(EntityWrapper aclEW) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			AccessControlList acl = jsonEntityFactory.createEntity(aclEW.getEntityJson(), AccessControlList.class);
			acl = synapseClient.createACL(acl);
			JSONObjectAdapter aclJson = acl
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper updateAcl(EntityWrapper aclEW) throws RestServiceException {
		return updateAcl(aclEW, false);
	}
	
	@Override
	public EntityWrapper updateAcl(EntityWrapper aclEW, boolean recursive) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			AccessControlList acl = jsonEntityFactory.createEntity(aclEW.getEntityJson(), AccessControlList.class);
			acl = synapseClient.updateACL(acl, recursive);
			JSONObjectAdapter aclJson = acl
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper deleteAcl(String ownerEntityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// first delete the ACL
			synapseClient.deleteACL(ownerEntityId);
			// now get the ACL governing this entity, which will be some ancestor, the 'permissions benefactor'
			AccessControlList acl = getAcl(ownerEntityId);
			JSONObjectAdapter aclJson = acl
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aclJson.toJSONString(), aclJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public boolean hasAccess(String ownerEntityId, String accessType) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.canAccess(ownerEntityId, ACCESS_TYPE.valueOf(accessType));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	
	@Override
	public boolean hasAccess(String ownerId, String ownerType, String accessType) throws RestServiceException {
		ObjectType type = ObjectType.valueOf(ownerType);
		ACCESS_TYPE access = ACCESS_TYPE.valueOf(accessType);
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.canAccess(ownerId, type, access);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public EntityWrapper getAllUsers() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<UserProfile> userProfiles = synapseClient.getUsers(USER_PAGINATION_OFFSET, USER_PAGINATION_LIMIT);
			JSONObjectAdapter upJson = userProfiles.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(upJson.toJSONString(), upJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper getAllGroups() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<UserGroup> userGroups = synapseClient.getGroups(GROUPS_PAGINATION_OFFSET, GROUPS_PAGINATION_LIMIT);
			JSONObjectAdapter ugJson = userGroups.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(ugJson.toJSONString(), ugJson.getClass().getName());		
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}

	@Override
	public String createUserProfileAttachmentPresignedUrl(String id,
			String tokenOrPreviewId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PresignedUrl url = synapseClient.createUserProfileAttachmentPresignedUrl(id, tokenOrPreviewId);
			return EntityFactory.createJSONStringForEntity(url);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper createAccessRequirement(EntityWrapper arEW) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			@SuppressWarnings("unchecked")
			AccessRequirement ar = jsonEntityFactory.createEntity(arEW.getEntityJson(), 
					(Class<AccessRequirement>)Class.forName(arEW.getEntityClassName()));
			AccessRequirement result = synapseClient.createAccessRequirement(ar);
			JSONObjectAdapter arJson = result.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(arJson.toJSONString(), arJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper createLockAccessRequirement(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			AccessRequirement result = synapseClient.createLockAccessRequirement(entityId);
			JSONObjectAdapter arJson = result.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(arJson.toJSONString(), arJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	
	@Override
	public AccessRequirementsTransport getUnmetAccessRequirements(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(entityId);
			subjectId.setType(RestrictableObjectType.ENTITY);

			VariableContentPaginatedResults<AccessRequirement> accessRequirements = 
				synapseClient.getUnmetAccessRequirements(subjectId);
			JSONObjectAdapter arJson = accessRequirements.writeToJSONObject(adapterFactory.createNew());
			AccessRequirementsTransport transport = new AccessRequirementsTransport();
			transport.setAccessRequirementsString(arJson.toJSONString());	
			Entity e = synapseClient.getEntityById(entityId);
			transport.setEntityString(EntityFactory.createJSONStringForEntity(e));
			transport.setEntityClassAsString(e.getClass().getName());
			transport.setUserProfileString(getUserProfile());
			return transport;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public String getUnmetEvaluationAccessRequirements(String evalId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(evalId);
			subjectId.setType(RestrictableObjectType.EVALUATION);

			VariableContentPaginatedResults<AccessRequirement> accessRequirements = 
				synapseClient.getUnmetAccessRequirements(subjectId);
			JSONObjectAdapter arJson = accessRequirements.writeToJSONObject(adapterFactory.createNew());
			return arJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public String getUnmetTeamAccessRequirements(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictableObjectDescriptor subjectId = new RestrictableObjectDescriptor();
			subjectId.setId(teamId);
			subjectId.setType(RestrictableObjectType.TEAM);
			
			VariableContentPaginatedResults<AccessRequirement> accessRequirements = 
				synapseClient.getUnmetAccessRequirements(subjectId);
			JSONObjectAdapter arJson = accessRequirements.writeToJSONObject(adapterFactory.createNew());
			return arJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}

	
	@Override
	public EntityWrapper createAccessApproval(EntityWrapper aaEW) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			    			@SuppressWarnings("unchecked")
							AccessApproval aa = jsonEntityFactory.createEntity(aaEW.getEntityJson(), 
					(Class<AccessApproval>)Class.forName(aaEW.getEntityClassName()));
			AccessApproval result = synapseClient.createAccessApproval(aa);
			JSONObjectAdapter aaJson = result.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aaJson.toJSONString(), aaJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (ClassNotFoundException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	@Override
	@Deprecated
	public EntityWrapper updateExternalLocationable(String entityId, String externalUrl, String name) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Entity locationable = synapseClient.getEntityById(entityId);
			if(!(locationable instanceof Locationable)) {
				throw new RuntimeException("Upload failed. Entity id: " + locationable.getId() + " is not Locationable.");
			}
			if (isManuallySettingExternalName(name)) {
				locationable.setName(name);
			}
			Locationable result = synapseClient.updateExternalLocationableToSynapse((Locationable)locationable, externalUrl);
			JSONObjectAdapter aaJson = result.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aaJson.toJSONString(), aaJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	@Override
	public EntityWrapper updateExternalFile(String entityId, String externalUrl, String name) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			boolean isManuallySettingName = isManuallySettingExternalName(name);
			Entity entity = synapseClient.getEntityById(entityId);
			if(!(entity instanceof FileEntity)) {
				throw new RuntimeException("Upload failed. Entity id: " + entity.getId() + " is not a File.");
			}
			
			ExternalFileHandle efh = new ExternalFileHandle();
			efh.setExternalURL(externalUrl);
			ExternalFileHandle clone = synapseClient.createExternalFileHandle(efh);
			((FileEntity)entity).setDataFileHandleId(clone.getId());
			if (isManuallySettingName)
				entity.setName(name);
			Entity updatedEntity = synapseClient.putEntity(entity);
			if (!isManuallySettingName)
				updatedEntity = updateExternalFileName(updatedEntity, externalUrl, synapseClient);
			JSONObjectAdapter aaJson = updatedEntity.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aaJson.toJSONString(), aaJson.getClass().getName());
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
	public EntityWrapper createExternalFile(String parentEntityId, String externalUrl, String name) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			boolean isManuallySettingName = isManuallySettingExternalName(name);
			FileEntity newEntity = new FileEntity();
			ExternalFileHandle efh = new ExternalFileHandle();
			efh.setExternalURL(externalUrl);
			ExternalFileHandle clone = synapseClient.createExternalFileHandle(efh);
			newEntity.setDataFileHandleId(clone.getId());
			newEntity.setParentId(parentEntityId);
			if (isManuallySettingName)
				newEntity.setName(name);
			Entity updatedEntity = synapseClient.createEntity(newEntity);
			if (!isManuallySettingName)
				updatedEntity = updateExternalFileName(updatedEntity, externalUrl, synapseClient);
			JSONObjectAdapter aaJson = updatedEntity.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(aaJson.toJSONString(), aaJson.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}
	
	private Entity updateExternalFileName(Entity entity, String externalUrl, org.sagebionetworks.client.SynapseClient synapseClient) {
		String oldName = entity.getName();
		try{
			//also try to rename to something reasonable, ignore if anything goes wrong
			entity.setName(DisplayUtils.getFileNameFromExternalUrl(externalUrl));
			entity = synapseClient.putEntity(entity);
		} catch(Throwable t) {
			//if anything goes wrong, send back the actual name
			entity.setName(oldName);
		}
		return entity;
	}

	@Override
	public String markdown2Html(String markdown, Boolean isPreview, Boolean isAlphaMode, String clientHostString) throws RestServiceException{
		try {
			long startTime = System.currentTimeMillis();
			String html = SynapseMarkdownProcessor.getInstance().markdown2Html(markdown, isPreview, clientHostString);
			long endTime = System.currentTimeMillis();
			float elapsedTime = endTime-startTime;
			
			logInfo("Markdown processing took " + (elapsedTime/1000f) + " seconds.  In alpha mode? " + isAlphaMode);
			
			return html;
		} catch (IOException e) {
			throw new RestServiceException(e.getMessage());
		}
	}

	@Override
	public String getActivityForEntity(String entityId)
			throws RestServiceException {
		return getActivityForEntityVersion(entityId, null);
	}
	
	@Override
	public String getActivityForEntityVersion(String entityId,
			Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Activity activity = synapseClient.getActivityForEntityVersion(entityId, versionNumber);
			return EntityFactory.createJSONStringForEntity(activity);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getActivity(String activityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Activity activity = synapseClient.getActivity(activityId);
			return EntityFactory.createJSONStringForEntity(activity);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String getEntitiesGeneratedBy(String activityId, Integer limit,
			Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<Reference> refs = synapseClient.getEntitiesGeneratedBy(activityId, limit, offset);
			return EntityFactory.createJSONStringForEntity(refs);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public EntityWrapper removeAttachmentFromEntity(String entityId,
			String attachmentName) throws RestServiceException {
		EntityWrapper updatedEntityWrapper = null;
		try {
				org.sagebionetworks.client.SynapseClient client = createSynapseClient();
				Entity e = client.getEntityById(entityId);
				if (e.getAttachments() != null) {
					for (AttachmentData data : e.getAttachments()) {
						if (data.getName().equals(attachmentName)) {
							e.getAttachments().remove(data);
							break;
						}
					}
				}
				// Save the changes.
				Entity updatedEntity = client.putEntity(e);
				updatedEntityWrapper = new EntityWrapper(EntityFactory.createJSONStringForEntity(updatedEntity), updatedEntity.getClass().getName());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
		
		return updatedEntityWrapper;
	}
	
	@Override
	public String getJSONEntity(String repoUri) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONObject entity = synapseClient.getEntity(repoUri);
			return entity.toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String createWikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			@SuppressWarnings("unchecked")
			WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,WikiPage.class);
			WikiPage returnPage = synapseClient.createWikiPage(ownerId, ObjectType.valueOf(ownerType), page);
			return EntityFactory.createJSONStringForEntity(returnPage);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public void deleteWikiPage(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			synapseClient.deleteWikiPage(properKey);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String getWikiHeaderTree(String ownerId, String ownerType) throws RestServiceException {
		try {
			PaginatedResults<WikiHeader> results = getWikiHeaderTree(ownerId, ObjectType.valueOf(ownerType));
			return EntityFactory.createJSONStringForEntity(results);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	private PaginatedResults<WikiHeader> getWikiHeaderTree(String ownerId, ObjectType ownerType)  throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<WikiHeader> results = synapseClient.getWikiHeaderTree(ownerId, ownerType);
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	private String getRootWikiId(org.sagebionetworks.client.SynapseClient synapseClient, String ownerId, ObjectType ownerType) throws RestServiceException{
		try{
			WikiPage rootPage = synapseClient.getRootWikiPage(ownerId, ownerType);
			if (rootPage != null)
				return rootPage.getId();
			else return null;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String getWikiPage(org.sagebionetworks.web.shared.WikiPageKey key) throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				//asking for the root.  find the root id first
				String rootWikiPage = getRootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			WikiPage returnPage = synapseClient.getWikiPage(properKey);
			return EntityFactory.createJSONStringForEntity(returnPage);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String updateWikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			@SuppressWarnings("unchecked")
			WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,WikiPage.class);
			WikiPage returnPage = synapseClient.updateWikiPage(ownerId, ObjectType.valueOf(ownerType), page);
			return EntityFactory.createJSONStringForEntity(returnPage);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public String getFileEndpoint() throws RestServiceException{
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		return synapseClient.getFileEndpoint();
		return StackConfiguration.getFileServiceEndpoint();
	}
	@Override
	public String getWikiAttachmentHandles(org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (key.getWikiPageId() == null) {
				//asking for the root.  find the root id first
				String rootWikiPage = getRootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
				key.setWikiPageId(rootWikiPage);
			}
			WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
			FileHandleResults results = synapseClient.getWikiAttachmenthHandles(properKey);
			return EntityFactory.createJSONStringForEntity(results);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	 // V2 Wiki crud
    @Override
    public String createV2WikiPage(String ownerId, String ownerType,
                    String wikiPageJson) throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
                    @SuppressWarnings("unchecked")
                    V2WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,V2WikiPage.class);
                    V2WikiPage returnPage = synapseClient.createV2WikiPage(ownerId, ObjectType.valueOf(ownerType), page);
                    return EntityFactory.createJSONStringForEntity(returnPage);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }

    private String getV2RootWikiId(org.sagebionetworks.client.SynapseClient synapseClient, String ownerId, ObjectType ownerType) throws RestServiceException{
            try{
                    V2WikiPage rootPage = synapseClient.getV2RootWikiPage(ownerId, ownerType);
                    if (rootPage != null)
                            return rootPage.getId();
                    else return null;
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }
    
    @Override
    public String getV2WikiPage(org.sagebionetworks.web.shared.WikiPageKey key)
                    throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    if (key.getWikiPageId() == null) {
                            //asking for the root.  find the root id first
                            String rootWikiPage = getV2RootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
                            key.setWikiPageId(rootWikiPage);
                    }
                    WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
                    V2WikiPage returnPage = synapseClient.getV2WikiPage(properKey);
                    return EntityFactory.createJSONStringForEntity(returnPage);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }
    
    @Override
    public String getVersionOfV2WikiPage(org.sagebionetworks.web.shared.WikiPageKey key, Long version) 
    	throws RestServiceException {
    	org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
    	try {
            if (key.getWikiPageId() == null) {
                    //asking for the root.  find the root id first
                    String rootWikiPage = getV2RootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
                    key.setWikiPageId(rootWikiPage);
            }
            WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
            V2WikiPage returnPage = synapseClient.getVersionOfV2WikiPage(properKey, version);
            return EntityFactory.createJSONStringForEntity(returnPage);
	    } catch (SynapseException e) {
	            throw ExceptionUtil.convertSynapseException(e);
	    } catch (JSONObjectAdapterException e) {
	            throw new UnknownErrorException(e.getMessage());
	    }
    }

    @Override
    public String updateV2WikiPage(String ownerId, String ownerType,
                    String wikiPageJson) throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
                    @SuppressWarnings("unchecked")
                    V2WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,V2WikiPage.class);
                    V2WikiPage returnPage = synapseClient.updateV2WikiPage(ownerId, ObjectType.valueOf(ownerType), page);
                    return EntityFactory.createJSONStringForEntity(returnPage);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }

    @Override
    public String restoreV2WikiPage(String ownerId, String ownerType,
                    String wikiPageJson, Long versionToUpdate)
                    throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
                    @SuppressWarnings("unchecked")
                    V2WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,V2WikiPage.class);
                    V2WikiPage returnPage = synapseClient.restoreV2WikiPage(ownerId, ObjectType.valueOf(ownerType), page, versionToUpdate);
                    return EntityFactory.createJSONStringForEntity(returnPage);
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
                    WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
                    synapseClient.deleteV2WikiPage(properKey);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            }
    }

    @Override
    public String getV2WikiHeaderTree(String ownerId, String ownerType)
                    throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    PaginatedResults<V2WikiHeader> results = synapseClient.getV2WikiHeaderTree(ownerId, ObjectType.valueOf(ownerType));
                    return EntityFactory.createJSONStringForEntity(results);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }
    
    @Override
    public String getV2WikiAttachmentHandles(
                    org.sagebionetworks.web.shared.WikiPageKey key)
                    throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    if (key.getWikiPageId() == null) {
                            //asking for the root.  find the root id first
                            String rootWikiPage = getV2RootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
                            key.setWikiPageId(rootWikiPage);
                    }
                    WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
                    FileHandleResults results = synapseClient.getV2WikiAttachmentHandles(properKey);
                    return EntityFactory.createJSONStringForEntity(results);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }

    @Override
    public String getVersionOfV2WikiAttachmentHandles(org.sagebionetworks.web.shared.WikiPageKey key, Long version) 
    	throws RestServiceException {
    	org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
    	try {
            if (key.getWikiPageId() == null) {
                    //asking for the root.  find the root id first
                    String rootWikiPage = getV2RootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
                    key.setWikiPageId(rootWikiPage);
            }
            WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
            FileHandleResults results = synapseClient.getVersionOfV2WikiAttachmentHandles(properKey, version);
            return EntityFactory.createJSONStringForEntity(results);
	    } catch (SynapseException e) {
	            throw ExceptionUtil.convertSynapseException(e);
	    } catch (JSONObjectAdapterException e) {
	            throw new UnknownErrorException(e.getMessage());
	    }
    }
    
    @Override
    public String getV2WikiHistory(
                    org.sagebionetworks.web.shared.WikiPageKey key, Long limit,
                    Long offset) throws RestServiceException {
            org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
            try {
                    WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
                    PaginatedResults<V2WikiHistorySnapshot> results = synapseClient.getV2WikiHistory(properKey, limit, offset);
                    return EntityFactory.createJSONStringForEntity(results);
            } catch (SynapseException e) {
                    throw ExceptionUtil.convertSynapseException(e);
            } catch (JSONObjectAdapterException e) {
                    throw new UnknownErrorException(e.getMessage());
            }
    }
    
    @Override
	public String getMarkdown(org.sagebionetworks.web.shared.WikiPageKey key) throws IOException, RestServiceException, SynapseException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
		File markdownFile = synapseClient.downloadV2WikiMarkdown(properKey);
		return FileUtils.readFileToString(markdownFile, "UTF-8");
	}

	@Override
	public String getVersionOfMarkdown(org.sagebionetworks.web.shared.WikiPageKey key, Long version) throws IOException, RestServiceException, SynapseException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
		File markdownFile = synapseClient.downloadVersionOfV2WikiMarkdown(properKey, version);
		return FileUtils.readFileToString(markdownFile, "UTF-8");
	}
	
	@Override
	public String zipAndUploadFile(String content, String fileName) throws IOException, RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		File file = zipUp(content, fileName);
		String contentType = guessContentTypeFromStream(file);
		try {
			// Upload the file and create S3 handle
			S3FileHandle handle = synapseClient.createFileHandle(file, contentType);
			try {
				return EntityFactory.createJSONStringForEntity(handle);
			} catch (JSONObjectAdapterException e) {
				throw new UnknownErrorException(e.getMessage());
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (IOException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	private File zipUp(String content, String fileName) throws IOException {
		// Create a temporary file to write content to
		File tempFile = File.createTempFile(fileName, ".tmp");
		if(content != null) {
			FileUtils.writeByteArrayToFile(tempFile, content.getBytes());
		} else {
			// When creating a wiki for the first time, markdown content doesn't exist
			// Uploaded file should be empty
			byte[] emptyByteArray = new byte[0];
			FileUtils.writeByteArrayToFile(tempFile, emptyByteArray);
		}
		return tempFile;
	}

	private static String guessContentTypeFromStream(File file)	throws FileNotFoundException, IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try{
			// Let java guess from the stream.
			String contentType = URLConnection.guessContentTypeFromStream(is);
			// If Java fails then set the content type to be octet-stream
			if(contentType == null){
				contentType = "application/octet-stream";
			}
			return contentType;
		}finally{
			is.close();
		}
	}

	@Override
	public String createV2WikiPageWithV1(String ownerId, String ownerType,
             String wikiPageJson) throws IOException, RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
        try {
	        JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	        @SuppressWarnings("unchecked")
	        WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,WikiPage.class);
	        WikiPage returnPage = synapseClient.createV2WikiPageWithV1(ownerId, ObjectType.valueOf(ownerType), page);
	        return EntityFactory.createJSONStringForEntity(returnPage);
        } catch (SynapseException e) {
            throw ExceptionUtil.convertSynapseException(e);
        } catch (JSONObjectAdapterException e) {
            throw new UnknownErrorException(e.getMessage());
        }
	}
	
	@Override
    public String updateV2WikiPageWithV1(String ownerId, String ownerType,
            String wikiPageJson) throws IOException, RestServiceException {
    	org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
	    try {
	        JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	        @SuppressWarnings("unchecked")
	        WikiPage page = jsonEntityFactory.createEntity(wikiPageJson,WikiPage.class);
	        WikiPage returnPage = synapseClient.updateV2WikiPageWithV1(ownerId, ObjectType.valueOf(ownerType), page);
	        return EntityFactory.createJSONStringForEntity(returnPage);
	    } catch (SynapseException e) {
	        throw ExceptionUtil.convertSynapseException(e);
	    } catch (JSONObjectAdapterException e) {
	        throw new UnknownErrorException(e.getMessage());
	    }
    }

	@Override
    public String getV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key)
                    throws RestServiceException, IOException {
        org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
        try {
            if (key.getWikiPageId() == null) {
                //asking for the root.  find the root id first
                String rootWikiPage = getV2RootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
                key.setWikiPageId(rootWikiPage);
            }
            WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
            WikiPage returnPage = synapseClient.getV2WikiPageAsV1(properKey);
            return EntityFactory.createJSONStringForEntity(returnPage);
        } catch (SynapseException e) {
            throw ExceptionUtil.convertSynapseException(e);
        } catch (JSONObjectAdapterException e) {
            throw new UnknownErrorException(e.getMessage());
        }
    }

	@Override
    public String getVersionOfV2WikiPageAsV1(org.sagebionetworks.web.shared.WikiPageKey key, Long version) 
    	throws RestServiceException, IOException {
    	org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
    	try {
            if (key.getWikiPageId() == null) {
	            //asking for the root.  find the root id first
	            String rootWikiPage = getV2RootWikiId(synapseClient, key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()));
	            key.setWikiPageId(rootWikiPage);
            }
            WikiPageKey properKey = new WikiPageKey(key.getOwnerObjectId(), ObjectType.valueOf(key.getOwnerObjectType()), key.getWikiPageId());
            WikiPage returnPage = synapseClient.getVersionOfV2WikiPageAsV1(properKey, version);
            return EntityFactory.createJSONStringForEntity(returnPage);
	    } catch (SynapseException e) {
	        throw ExceptionUtil.convertSynapseException(e);
	    } catch (JSONObjectAdapterException e) {
	        throw new UnknownErrorException(e.getMessage());
	    }
    }
	
	@Override
	public String addFavorite(String entityId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			EntityHeader favorite = synapseClient.addFavorite(entityId);
			return EntityFactory.createJSONStringForEntity(favorite);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
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
	public String getFavorites(Integer limit, Integer offset)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<EntityHeader> favorites = synapseClient.getFavorites(limit, offset);
			return EntityFactory.createJSONStringForEntity(favorites);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
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
	public String getTeamMembers(String teamId, String fragment, Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<TeamMember> members = synapseClient.getTeamMembers(teamId, fragment, limit, offset);
			return EntityFactory.createJSONStringForEntity(members);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public ArrayList<String> getTeamsForUser(String userId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<Team> teams = synapseClient.getTeamsForUser(userId, MAX_LIMIT, ZERO_OFFSET);
			List<Team> teamList = teams.getResults();
			ArrayList<String> teamListStrings = new ArrayList<String>();
			for (Team t : teamList) {
				teamListStrings.add(EntityFactory.createJSONStringForEntity(t));
			}
			return teamListStrings;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String getTeams(String userId, Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<Team> teams = synapseClient.getTeamsForUser(userId, limit, offset);

			return EntityFactory.createJSONStringForEntity(teams);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String getTeamsBySearch(String searchTerm, Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			if (searchTerm != null && searchTerm.trim().length() ==0)
				searchTerm = null;
			if (offset == null)
				offset = ZERO_OFFSET.intValue();
			PaginatedResults<Team> teams = synapseClient.getTeams(searchTerm, limit, offset);
			return EntityFactory.createJSONStringForEntity(teams);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public void deleteOpenMembershipRequests(String currentUserId, String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
//			get all open membership requests for this user
			PaginatedResults<MembershipRequest> requests = synapseClient.getOpenMembershipRequests(teamId, currentUserId, MAX_LIMIT, ZERO_OFFSET);
			//and delete each one
//			for (MembershipRequest request : requests.getResults()) {
//				synapseClient.deleteMembershipRequest(request.getId());
//			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	

	public String getTeamMembershipState(String currentUserId, String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(teamId, currentUserId);
			return EntityFactory.createJSONStringForEntity(membershipStatus);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public void requestMembership(String currentUserId, String teamId, String message) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(teamId, currentUserId);
			//if we can join the team without creating the request (like if we are a team admin, or there is an open invitation), then just do that!
			if (membershipStatus.getCanJoin()) {
				synapseClient.addTeamMember(teamId, currentUserId);
			} else if (!membershipStatus.getHasOpenRequest()){
				//otherwise, create the request
				MembershipRqstSubmission membershipRequest = new MembershipRqstSubmission();
				membershipRequest.setMessage(message);
				membershipRequest.setTeamId(teamId);
				membershipRequest.setUserId(currentUserId);

				//make new Synapse call
				synapseClient.createMembershipRequest(membershipRequest);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void inviteMember(String userGroupId, String teamId, String message) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(teamId, userGroupId);
			//if we can join the team without creating the invite (like if we are a team admin, or there is an open membership request), then just do that!
			if (membershipStatus.getCanJoin()) {
				synapseClient.addTeamMember(teamId, userGroupId);
			} else if (!membershipStatus.getHasOpenInvitation()){
				//check to see if there is already an open invite
				MembershipInvtnSubmission membershipInvite = new MembershipInvtnSubmission();
				membershipInvite.setMessage(message);
				membershipInvite.setTeamId(teamId);
				membershipInvite.setInviteeId(userGroupId);
				
				//make new Synapse call
				synapseClient.createMembershipInvitation(membershipInvite);
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	
	@Override
	public TeamBundle getTeamBundle(String userId, String teamId, boolean isLoggedIn) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<TeamMember> allMembers = synapseClient.getTeamMembers(teamId, null, 1, ZERO_OFFSET);
			long memberCount = allMembers.getTotalNumberOfResults();
			boolean isAdmin = false;
			Team team = synapseClient.getTeam(teamId);
			String membershipStatusJsonString = null;
			//get membership state for the current user
			if (isLoggedIn){
				TeamMembershipStatus membershipStatus = synapseClient.getTeamMembershipStatus(teamId,userId);
				JSONObjectAdapter membershipStatusJson = membershipStatus.writeToJSONObject(adapterFactory.createNew());
				membershipStatusJsonString = membershipStatusJson.toJSONString();
				if (membershipStatus.getIsMember()) {
					TeamMember teamMember = synapseClient.getTeamMember(teamId, userId);
					isAdmin = teamMember.getIsAdmin();
				}
			}
			
			JSONObjectAdapter teamJson = team.writeToJSONObject(adapterFactory.createNew());
			return new TeamBundle(teamJson.toJSONString(), memberCount, membershipStatusJsonString, isAdmin);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public List<MembershipRequestBundle> getOpenRequests(String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<MembershipRequest> requests = synapseClient.getOpenMembershipRequests(teamId, null, MAX_LIMIT, ZERO_OFFSET);
			//and ask for the team info for each invite, and fill that in the bundle
			
			List<MembershipRequestBundle> returnList = new ArrayList<MembershipRequestBundle>();
			//now go through and create a MembershipRequestBundle for each pair
			
			for (MembershipRequest request : requests.getResults()) {
				UserProfile profile = synapseClient.getUserProfile(request.getUserId());
				
				JSONObjectAdapter profileJson = profile.writeToJSONObject(adapterFactory.createNew());
				JSONObjectAdapter requestJson = request.writeToJSONObject(adapterFactory.createNew());
				MembershipRequestBundle b = new MembershipRequestBundle(profileJson.toJSONString(), requestJson.toJSONString());
				returnList.add(b);
			}
			
			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public Long getOpenRequestCount(String currentUserId, String teamId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//must be an admin to the team open requests.  To get admin status, must be a member
			try {
				PaginatedResults<MembershipRequest> requests = synapseClient.getOpenMembershipRequests(teamId, null, 1, ZERO_OFFSET);
				return requests.getTotalNumberOfResults();
			} catch (SynapseForbiddenException forbiddenEx) {
				return null;
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public List<MembershipInvitationBundle> getOpenInvitations(String userId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<MembershipInvitation> invitations = synapseClient.getOpenMembershipInvitations(userId,null, MAX_LIMIT, ZERO_OFFSET);
			//and ask for the team info for each invite, and fill that in the bundle
			
			List<MembershipInvitationBundle> returnList = new ArrayList<MembershipInvitationBundle>();
			//now go through and create a MembershipInvitationBundle for each pair
			
			for (MembershipInvitation invite : invitations.getResults()) {
				Team team = synapseClient.getTeam(invite.getTeamId());
				JSONObjectAdapter teamJson = team.writeToJSONObject(adapterFactory.createNew());
				JSONObjectAdapter inviteJson = invite.writeToJSONObject(adapterFactory.createNew());
				MembershipInvitationBundle b = new MembershipInvitationBundle(teamJson.toJSONString(), inviteJson.toJSONString());
				returnList.add(b);
			}
			
			return returnList;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public void setIsTeamAdmin(String currentUserId, String targetUserId, String teamId, boolean isTeamAdmin) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.setTeamMemberPermissions(teamId, targetUserId, isTeamAdmin);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void deleteTeamMember(String currentUserId, String targetUserId, String teamId) throws RestServiceException {
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
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			Team team = jsonEntityFactory.createEntity(teamJson, Team.class);
			Team updatedTeam = synapseClient.updateTeam(team);
			JSONObjectAdapter updatedTeamJson = updatedTeam.writeToJSONObject(adapterFactory.createNew());
			return updatedTeamJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	
	@Override
	public ArrayList<String> getFavoritesList(Integer limit, Integer offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<EntityHeader> favorites = synapseClient.getFavorites(limit, offset);
			ArrayList<String> results = new ArrayList<String>();
			for(EntityHeader eh : favorites.getResults()) {
				results.add(eh.writeToJSONObject(adapterFactory.createNew()).toJSONString());
			}
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	
	@Override
	public String getDescendants(String nodeId, int pageSize, String lastDescIdExcl) throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			EntityIdList entityIdList = synapseClient.getDescendants(nodeId, pageSize, lastDescIdExcl);
			return EntityFactory.createJSONStringForEntity(entityIdList);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	@Override
	public String getEntityDoi(String entityId, Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			Doi	doi = synapseClient.getEntityDoi(entityId, versionNumber);
			return EntityFactory.createJSONStringForEntity(doi);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		} catch (Exception e) {
			throw ExceptionUtil.convertSynapseException(new SynapseNotFoundException());	//backend will be changed to throw a SynapseNotFoundException when no records are found
		}
	}

	@Override
	public void createDoi(String entityId, Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.createEntityDoi(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String getChunkedFileToken(String fileName, String contentType, String contentMD5) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			CreateChunkedFileTokenRequest ccftr = new CreateChunkedFileTokenRequest();
			ccftr.setFileName(fileName);
			ccftr.setContentType(contentType);
			ccftr.setContentMD5(contentMD5);
			// Start the upload
			ChunkedFileToken token = synapseClient.createChunkedFileUploadToken(ccftr);
			
			JSONObjectAdapter requestJson = token.writeToJSONObject(adapterFactory.createNew());
			return requestJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String getChunkedPresignedUrl(String requestJson) throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			ChunkRequest request = jsonEntityFactory.createEntity(requestJson, ChunkRequest.class);
			return synapseClient.createChunkedPresignedUrl(request).toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String combineChunkedFileUpload(List<String> requests) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//re-create each request
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			//reconstruct all part numbers, and token
			ChunkedFileToken token=null;
			List<Long> parts = new ArrayList<Long>();
			
			for (String requestJson : requests) {
				ChunkRequest request = jsonEntityFactory.createEntity(requestJson, ChunkRequest.class);
				token = request.getChunkedFileToken();
				parts.add(request.getChunkNumber());
			}
			
			CompleteAllChunksRequest cacr = new CompleteAllChunksRequest();
			cacr.setChunkedFileToken(token);
			cacr.setChunkNumbers(parts);

			// Start the daemon
			UploadDaemonStatus status = synapseClient.startUploadDeamon(cacr);
			JSONObjectAdapter requestJson = status.writeToJSONObject(adapterFactory.createNew());
			return requestJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public String getUploadDaemonStatus(String daemonId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			UploadDaemonStatus status = synapseClient.getCompleteUploadDaemonStatus(daemonId);
			if (State.FAILED == status.getState()) {
				logError(status.getErrorMessage());
			}
			JSONObjectAdapter requestJson = status.writeToJSONObject(adapterFactory.createNew());
			return requestJson.toJSONString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}

	}
	
	@Override
	public String setFileEntityFileHandle(String fileHandleId, String entityId, String parentEntityId, boolean isRestricted) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try{
			FileHandle newHandle = synapseClient.getRawFileHandle(fileHandleId);
			//create entity if we have to
			FileEntity fileEntity = null;
			
			if (entityId == null) {
				//create the file entity
				fileEntity = FileHandleServlet.getNewFileEntity(parentEntityId, fileHandleId, synapseClient);
			}
			else {
				//get the file entity to update
				fileEntity = (FileEntity) synapseClient.getEntityById(entityId);
				//update data file handle id
				fileEntity.setDataFileHandleId(fileHandleId);
				fileEntity = synapseClient.putEntity(fileEntity);
			}
			//fix name and lock down
			FileHandleServlet.fixNameAndLockDown(fileEntity, newHandle, isRestricted, synapseClient);
			return fileEntity.getId();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String getEvaluations(List<String> evaluationIds) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			List<Evaluation> evalList = new ArrayList<Evaluation>();
			for (String evalId : evaluationIds) {
				evalList.add(synapseClient.getEvaluation(evalId));
			}
			PaginatedResults<Evaluation> results = new PaginatedResults<Evaluation>();
			results.setResults(evalList);
			results.setTotalNumberOfResults(evalList.size());
			JSONObjectAdapter evaluationsJson = results.writeToJSONObject(adapterFactory.createNew());
			return evaluationsJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	
	@Override
	public String getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			URL url = synapseClient.getFileEntityTemporaryUrlForVersion(entityId, versionNumber);
			return url.toString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}

	}
	
	@Override
	public String getAvailableEvaluations() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			PaginatedResults<Evaluation> results = synapseClient.getAvailableEvaluationsPaginated(EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
			JSONObjectAdapter evaluationsJson = results.writeToJSONObject(adapterFactory.createNew());
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
			PaginatedResults<Evaluation> returnResults = new PaginatedResults<Evaluation>();
			List<Evaluation> returnList = new ArrayList<Evaluation>();
			if (targetEvaluationIds.size() > 0) {
				Set<String> targetEvalIdsCopy = new HashSet<String>();
				targetEvalIdsCopy.addAll(targetEvaluationIds);
				PaginatedResults<Evaluation> results = synapseClient.getAvailableEvaluationsPaginated(EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
				//filter down to the target evaluation ids
				for (Evaluation evaluation : results.getResults()) {
					if (targetEvalIdsCopy.contains(evaluation.getId())) {
						targetEvalIdsCopy.remove(evaluation.getId());
						returnList.add(evaluation);
					}
				}
			}
			returnResults.setResults(returnList);
			returnResults.setTotalNumberOfResults(returnList.size());
			//filter results down to the targetEvaluationIds
			JSONObjectAdapter evaluationsJson = returnResults.writeToJSONObject(adapterFactory.createNew());
			return evaluationsJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	/**
	 * Return all evaluations associated to a particular entity, for which the caller can change permissions
	 */
	@Override
	public ArrayList<String> getSharableEvaluations(String entityId) throws RestServiceException {
		if (entityId == null || entityId.trim().length()==0 ) {
			throw new BadRequestException("Entity ID must be given");
		}
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//look up the available evaluations
			PaginatedResults<Evaluation> allEvaluations = synapseClient.getEvaluationByContentSource(entityId, EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
			
			ArrayList<String> mySharableEvalauations = new ArrayList<String>();
			for (Evaluation eval : allEvaluations.getResults()) {
				//evaluation is associated to entity id.  can I change permissions?
				UserEvaluationPermissions uep = synapseClient.getUserEvaluationPermissions(eval.getId());
				if (uep.getCanChangePermissions()) {
					mySharableEvalauations.add(eval.writeToJSONObject(adapterFactory.createNew()).toJSONString());
				}
			}
			return mySharableEvalauations;
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String createSubmission(String submissionJson, String etag) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			Submission sub = jsonEntityFactory.createEntity(submissionJson, Submission.class);
			Submission updatedSubmission = synapseClient.createSubmission(sub, etag);
			JSONObjectAdapter updatedSubmissionJson = updatedSubmission.writeToJSONObject(adapterFactory.createNew());
			return updatedSubmissionJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String getUserEvaluationPermissions(String evalId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			UserEvaluationPermissions permissions = synapseClient.getUserEvaluationPermissions(evalId);
			JSONObjectAdapter json = permissions.writeToJSONObject(adapterFactory.createNew());
			return json.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String getEvaluationAcl(String evalId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			AccessControlList acl = synapseClient.getEvaluationAcl(evalId);
			JSONObjectAdapter json = acl.writeToJSONObject(adapterFactory.createNew());
			return json.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	public String updateEvaluationAcl(String aclJson) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
			AccessControlList acl = jsonEntityFactory.createEntity(aclJson, AccessControlList.class);
			AccessControlList updatedacl = synapseClient.updateEvaluationAcl(acl);
			JSONObjectAdapter json = updatedacl.writeToJSONObject(adapterFactory.createNew());
			return json.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String getAvailableEvaluationsSubmitterAliases() throws RestServiceException{
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//query for all available evaluations.
			PaginatedResults<Evaluation> availableEvaluations = synapseClient.getAvailableEvaluationsPaginated(0, MAX_LIMIT);
			//gather all submissions
			List<Submission> allSubmissions = new ArrayList<Submission>();
			for (Evaluation evaluation : availableEvaluations.getResults()) {
				//query for all submissions for each evaluation
				PaginatedResults<Submission> submissions = synapseClient.getMySubmissions(evaluation.getId(), 0, MAX_LIMIT);
				allSubmissions.addAll(submissions.getResults());
			}
			
			//sort by created on
			Collections.sort(allSubmissions, new Comparator<Submission>() {
				@Override
				public int compare(Submission o1, Submission o2) {
					return o2.getCreatedOn().compareTo(o1.getCreatedOn());
				}
			});
			
			//run through and only keep unique submitter alias values (first in the list was most recently used)
			Set<String> uniqueSubmitterAliases = new HashSet<String>();
			List<String> returnAliases = new ArrayList<String>();
			for (Submission sub : allSubmissions) {
				String submitterAlias = sub.getSubmitterAlias();
				if (!uniqueSubmitterAliases.contains(submitterAlias)) {
					uniqueSubmitterAliases.add(submitterAlias);
					returnAliases.add(submitterAlias);
				}
			}
			//if it contains null or empty string, remove
			returnAliases.remove(null);
			returnAliases.remove("");
			RestResourceList returnList = new RestResourceList();
			returnList.setList(returnAliases);
			JSONObjectAdapter returnListJson = returnList.writeToJSONObject(adapterFactory.createNew());
			return returnListJson.toJSONString();
			
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	@Override
	public Boolean hasSubmitted() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			//get all evaluations for which the user has joined as a participant
			PaginatedResults<Evaluation> evaluations = synapseClient.getAvailableEvaluationsPaginated(EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
			for (Evaluation evaluation : evaluations.getResults()) {
				//return true if any of these have a submission
				PaginatedResults<Submission> res = synapseClient.getMySubmissions(evaluation.getId(), 0, 0);
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
	public String getSynapseVersions() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {			
			SynapseVersionInfo versionInfo = synapseClient.getVersionInfo();
			new PortalVersionHolder();			
			return PortalVersionHolder.getVersionInfo() +","+ versionInfo.getVersion();			
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private static class PortalVersionHolder {
		private static String versionInfo = "";
		
		static {
			InputStream s = SynapseClientImpl.class.getResourceAsStream("/version-info.properties");
			Properties prop = new Properties();
			try {
				prop.load(s);
			} catch (IOException e) {
				throw new RuntimeException("version-info.properties file not found", e);
			}
			versionInfo = prop.getProperty("org.sagebionetworks.portal.version");
		}
		
		private static String getVersionInfo() {
			return versionInfo;
		}
				
	}
	
	@Override
	public String getSynapseProperty(String key) {
		return PortalPropertiesHolder.getProperty(key);
	}

	private static class PortalPropertiesHolder {
		private static Properties props;
		
		static {
			InputStream s = SynapseClientImpl.class.getResourceAsStream("/portal.properties");
			props = new Properties();
			try {
				props.load(s);
			} catch (IOException e) {
				throw new RuntimeException("portal.properties file not found", e);
			}
		}
		
		private static String getProperty(String key) {
			return props.getProperty(key);
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
}
