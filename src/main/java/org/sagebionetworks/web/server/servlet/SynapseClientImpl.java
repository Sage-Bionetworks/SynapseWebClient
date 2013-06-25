package org.sagebionetworks.web.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.evaluation.model.Participant;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationState;
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
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
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
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
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
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityConstants;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

import eu.henkelmann.actuarius.ActuariusTransformer;

@SuppressWarnings("serial")
public class SynapseClientImpl extends RemoteServiceServlet implements
		SynapseClient, TokenProvider {

	static private Log log = LogFactory.getLog(SynapseClientImpl.class);
	static {//kick off initialization (like pattern compilation) by referencing it
			SynapseMarkdownProcessor.getInstance();
		}
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SynapseClientImpl.class
			.getName());
	private TokenProvider tokenProvider = this;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	AutoGenFactory entityFactory = new AutoGenFactory();
	ActuariusTransformer markdownProcessor = new ActuariusTransformer();
	
	/**
	 * Injected with Gin
	 */
	private ServiceUrlProvider urlProvider;

	/**
	 * Essentially the constructor. Setup synapse client.
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
	 * This allows tests provide mock Synapse ojbects
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
		Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();			
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
			Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
	 * The synapse client is stateful so we must create a new one for each
	 * request
	 */	
	private Synapse createSynapseClient() {
		// Create a new syanpse			
		Synapse synapseClient = synapseProvider.createNewClient();		
		synapseClient.setSessionToken(tokenProvider.getSessionToken());
		synapseClient.setRepositoryEndpoint(urlProvider
				.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		synapseClient.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		return synapseClient;
	}

	@Override
	public SerializableWhitelist junk(SerializableWhitelist l) {
		return null;
	}

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
			Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();
			BatchResults<EntityHeader> results = synapseClient.getEntityHeaderBatch(list.getReferences());
			return EntityFactory.createJSONStringForEntity(results);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}


	@Override
	public void deleteEntityById(String entityId) throws RestServiceException {
		try {
			Synapse synapseClient = createSynapseClient();
			synapseClient.deleteEntityById(entityId);			
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public void deleteEntityVersionById(String entityId, Long versionNumber) throws RestServiceException {
		try {
			Synapse synapseClient = createSynapseClient();
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
				Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();
			String targetUserId = userId == null ? "" : "/"+userId;
			JSONObject userProfile = synapseClient.getSynapseEntity(urlProvider.getRepositoryServiceUrl(), "/userProfile"+targetUserId);
			return userProfile.toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	
	@Override
	public EntityWrapper getUserGroupHeadersById(List<String> ids) throws RestServiceException {
		try {
			Synapse synapseClient = createSynapseClient();
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
			Synapse synapseClient = createSynapseClient();
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
				userProfileJSONObject = EntityFactory.createJSONObjectForEntity(profile);
			}
			synapseClient.putJSONObject("/userProfile", userProfileJSONObject, null);				
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
		try {
			return synapseClient.canAccess(ownerId, type, access);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public EntityWrapper getAllUsers() throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
	public EntityWrapper createAccessApproval(EntityWrapper aaEW) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
	
	private Entity updateExternalFileName(Entity entity, String externalUrl, Synapse synapseClient) {
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
	public String markdown2Html(String markdown, Boolean isPreview, Boolean isAlphaMode) throws RestServiceException{
		try {
			long startTime = System.currentTimeMillis();
			String html = null;
			if (isAlphaMode)
				html = SynapseMarkdownProcessor.getInstance().markdown2Html(markdown, isPreview);
			else
				html = ServerMarkdownUtils.markdown2Html(markdown, isPreview, markdownProcessor);
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
				Synapse client = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
		try {
			JSONObject entity = synapseClient.getEntity(repoUri);
			return entity.toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String createWikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
		try {
			PaginatedResults<WikiHeader> results = synapseClient.getWikiHeaderTree(ownerId, ownerType);
			return results;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	private String getRootWikiId(Synapse synapseClient, String ownerId, ObjectType ownerType) throws RestServiceException{
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
//		Synapse synapseClient = createSynapseClient();
//		return synapseClient.getFileEndpoint();
		return StackConfiguration.getFileServiceEndpoint();
	}
	@Override
	public String getWikiAttachmentHandles(org.sagebionetworks.web.shared.WikiPageKey key)
			throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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

	@Override
	public String addFavorite(String entityId) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
		try {
			synapseClient.removeFavorite(entityId);			
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public String getFavorites(Integer limit, Integer offset)
			throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			PaginatedResults<EntityHeader> favorites = synapseClient.getFavorites(limit, offset);
			return EntityFactory.createJSONStringForEntity(favorites);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public ArrayList<String> getFavoritesList(Integer limit, Integer offset) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
		try {
			synapseClient.createEntityDoi(entityId, versionNumber);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public UserEvaluationState getUserEvaluationState(String evaluationId) throws RestServiceException{
		Synapse synapseClient = createSynapseClient();
		try {
			return synapseClient.getUserEvaluationState(evaluationId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void createParticipants(String[] evaluationIds)
			throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			for (int i = 0; i < evaluationIds.length; i++) {
				Participant participant = synapseClient.createParticipant(evaluationIds[i]);	
			}
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public String getChunkedFileToken(String fileName, String contentType) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			CreateChunkedFileTokenRequest ccftr = new CreateChunkedFileTokenRequest();
			ccftr.setFileName(fileName);
			ccftr.setContentType(contentType);
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
		Synapse synapseClient = createSynapseClient();
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
	public String completeUpload(String fileHandleId, String entityId, String parentEntityId, boolean isRestricted) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
	public String getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			URL url = synapseClient.getFileEntityTemporaryUrlForVersion(entityId, versionNumber);
			return url.toString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}

	}
	
	@Override
	public String getAvailableEvaluations() throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			PaginatedResults<Evaluation> results = synapseClient.getAvailableEvaluationsPaginated(EvaluationStatus.OPEN, EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
			JSONObjectAdapter evaluationsJson = results.writeToJSONObject(adapterFactory.createNew());
			return evaluationsJson.toJSONString();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String getAvailableEvaluationEntities() throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			//look up the available evaluations
			PaginatedResults<Evaluation> availableEvaluations = synapseClient.getAvailableEvaluationsPaginated(EvaluationStatus.OPEN, EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
			//collect contentSource entity IDs
			Set<String> uniqueEntityIds = new HashSet<String>();
			for (Evaluation eval : availableEvaluations.getResults()) {
				uniqueEntityIds.add(eval.getContentSource());
			}
			//create reference for each contentSource entity ID
			List<Reference> references = new ArrayList<Reference>();
			for (String entityId : uniqueEntityIds) {
				Reference ref = new Reference();
				ref.setTargetId(entityId);
				references.add(ref);
			}
			//query for the associated entities
			BatchResults<EntityHeader> results = synapseClient.getEntityHeaderBatch(references);
			//and return them
			return EntityFactory.createJSONStringForEntity(results);
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public ArrayList<String> getAvailableEvaluationEntitiesList() throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
		try {
			//look up the available evaluations
			PaginatedResults<Evaluation> availableEvaluations = synapseClient.getAvailableEvaluationsPaginated(EvaluationStatus.OPEN, EVALUATION_PAGINATION_OFFSET, EVALUATION_PAGINATION_LIMIT);
			//collect contentSource entity IDs
			Set<String> uniqueEntityIds = new HashSet<String>();
			for (Evaluation eval : availableEvaluations.getResults()) {
				uniqueEntityIds.add(eval.getContentSource());
			}
			//create reference for each contentSource entity ID
			List<Reference> references = new ArrayList<Reference>();
			for (String entityId : uniqueEntityIds) {
				Reference ref = new Reference();
				ref.setTargetId(entityId);
				references.add(ref);
			}
			//query for the associated entities
			BatchResults<EntityHeader> results = synapseClient.getEntityHeaderBatch(references);
			//and return them
			ArrayList<String> evaluations = new ArrayList<String>();
			for(EntityHeader eh : results.getResults()) {
				evaluations.add(eh.writeToJSONObject(adapterFactory.createNew()).toJSONString());
			}
			return evaluations;
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	public String createSubmission(String submissionJson, String etag) throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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
	
	public String getAvailableEvaluationsSubmitterAliases() throws RestServiceException{
		Synapse synapseClient = createSynapseClient();
		try {
			//query for all available evaluations.
			PaginatedResults<Evaluation> availableEvaluations = synapseClient.getAvailableEvaluationsPaginated(EvaluationStatus.OPEN, 0, Integer.MAX_VALUE);
			//gather all submissions
			List<Submission> allSubmissions = new ArrayList<Submission>();
			for (Evaluation evaluation : availableEvaluations.getResults()) {
				//query for all submissions for each evaluation
				PaginatedResults<Submission> submissions = synapseClient.getMySubmissions(evaluation.getId(), 0, Integer.MAX_VALUE);
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
	public String getSynapseVersions() throws RestServiceException {
		Synapse synapseClient = createSynapseClient();
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

	
}
