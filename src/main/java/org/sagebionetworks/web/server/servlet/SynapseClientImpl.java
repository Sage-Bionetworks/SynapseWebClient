package org.sagebionetworks.web.server.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClient;
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

@SuppressWarnings("serial")
public class SynapseClientImpl extends RemoteServiceServlet implements
		SynapseClient, TokenProvider {

	static private Log log = LogFactory.getLog(SynapseClientImpl.class);

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SynapseClientImpl.class
			.getName());
	private TokenProvider tokenProvider = this;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	AutoGenFactory entityFactory = new AutoGenFactory();

	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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

	/**
	 * Get an Entity by its id
	 */
	@Override
	public EntityWrapper getEntity(String entityId) {
		Synapse synapseClient = createSynapseClient();
		try {
			Entity entity = synapseClient.getEntityById(entityId);
			JSONObjectAdapter entityJson = entity
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(entityJson.toJSONString(), entity
					.getClass().getName(), null);
		} catch (SynapseException e) {
			// Since we are not throwing errors, log them
			log.error(e);
			return new EntityWrapper(null, null,
					ExceptionUtil.convertSynapseException(e));
		} catch (JSONObjectAdapterException e) {
			// Since we are not throwing errors, log them
			log.error(e);
			return new EntityWrapper(null, null, new UnknownErrorException(
					e.getMessage()));
		}
	}

	@Override
	public String getEntityTypeRegistryJSON() {
		return SynapseClientImpl.getEntityTypeRegistryJson();
	}

	@Override
	public EntityWrapper getEntityPath(String entityId) {
		Synapse synapseClient = createSynapseClient();
		try {
			EntityPath entityPath = synapseClient.getEntityPath(entityId);
			JSONObjectAdapter entityPathJson = entityPath
					.writeToJSONObject(adapterFactory.createNew());
			return new EntityWrapper(entityPathJson.toJSONString(), entityPath
					.getClass().getName(), null);
		} catch (SynapseException e) {
			return new EntityWrapper(null, null,
					ExceptionUtil.convertSynapseException(e));
		} catch (JSONObjectAdapterException e) {
			// Since we are not throwing errors, log them
			log.error(e);
			return new EntityWrapper(null, null, new UnknownErrorException(
					e.getMessage()));
		}
	}

	@Override
	public EntityWrapper search(String searchQueryJson) {
		Synapse synapseClient = createSynapseClient();
		try {
			JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
			SearchResults searchResults = synapseClient.search(new SearchQuery(
					adapter.createNew(searchQueryJson)));
			searchResults.writeToJSONObject(adapter);
			return new EntityWrapper(adapter.toJSONString(),
					SearchResults.class.getName(), null);
		} catch (SynapseException e) {
			return new EntityWrapper(null, null,
					ExceptionUtil.convertSynapseException(e));
		} catch (JSONObjectAdapterException e) {
			// Since we are not throwing errors, log them
			log.error(e);
			return new EntityWrapper(null, null, new UnknownErrorException(
					e.getMessage()));
		} catch (UnsupportedEncodingException e) {
			// Since we are not throwing errors, log them
			log.error(e);
			return new EntityWrapper(null, null, new UnknownErrorException(
					e.getMessage()));
		}
	}

	/*
	 * Private Methods
	 */

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
		return synapseClient;
	}

	/**
	 * Read an input stream into a string.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static String readToString(InputStream in) throws IOException {
		try {
			BufferedInputStream bufferd = new BufferedInputStream(in);
			byte[] buffer = new byte[1024];
			StringBuilder builder = new StringBuilder();
			int index = -1;
			while ((index = bufferd.read(buffer, 0, buffer.length)) > 0) {
				builder.append(new String(buffer, 0, index, "UTF-8"));
			}
			return builder.toString();
		} finally {
			in.close();
		}
	}

	@Override
	public SerializableWhitelist junk(SerializableWhitelist l) {
		return null;
	}

	public static String getEntityTypeRegistryJson() {
		ClassLoader classLoader = EntityType.class.getClassLoader();
		InputStream in = classLoader
				.getResourceAsStream(EntityType.REGISTER_JSON_FILE_NAME);
		if (in == null)
			throw new IllegalStateException("Cannot find the "
					+ EntityType.REGISTER_JSON_FILE_NAME
					+ " file on the classpath");
		String jsonString = "";
		try {
			jsonString = readToString(in);
		} catch (IOException e) {
			log.error(e);
			// error reading file
		}
		return jsonString;
	}

	@Override
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask)
			throws RestServiceException {
		try {
			// Get all of the requested parts
			EntityBundleTransport transport = new EntityBundleTransport();
			Synapse synapseClient = createSynapseClient();
			// Add the entity?
			handleEntity(entityId, partsMask, transport, synapseClient);
			// Add the annotations?
			handleAnnotaions(entityId, partsMask, transport, synapseClient);
			// Add the permissions?
			handlePermissions(entityId, partsMask, transport, synapseClient);
			// Add the path?
			handleEntityPath(entityId, partsMask, transport, synapseClient);
			// Add Referenced By?
			handleEntityReferencedBy(entityId, partsMask, transport,
					synapseClient);
			// Add Referenced By?
			handleEntityChildCount(entityId, partsMask, transport,	synapseClient);
			return transport;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONObjectAdapterException e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}

	private void handleEntityChildCount(String entityId, int partsMask,
			EntityBundleTransport transport, Synapse synapseClient) throws SynapseException {
		if ((EntityBundleTransport.CHILD_COUNT & partsMask) > 0) {
			Long count = synapseClient.getChildCount(entityId);
			transport.setChildCount(count);
		}
	}

	/**
	 * Set the entity path if requested
	 * 
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleEntityPath(String entityId, int partsMask,
			EntityBundleTransport transport, Synapse synapseClient)
			throws SynapseException, JSONObjectAdapterException {
		if ((EntityBundleTransport.ENTITY_PATH & partsMask) > 0) {
			EntityPath path = synapseClient.getEntityPath(entityId);
			transport.setEntityPathJson(EntityFactory
					.createJSONStringForEntity(path));
		}
	}

	/**
	 * Add the permissions to the bundle if requested.
	 * 
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handlePermissions(String entityId, int partsMask,
			EntityBundleTransport transport, Synapse synapseClient)
			throws SynapseException, JSONObjectAdapterException {
		if ((EntityBundleTransport.PERMISSIONS & partsMask) > 0) {
			UserEntityPermissions permissions = synapseClient
					.getUsersEntityPermissions(entityId);
			transport.setPermissionsJson(EntityFactory
					.createJSONStringForEntity(permissions));
		}
	}

	/**
	 * Add the annotations to the bundle if requested.
	 * 
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleAnnotaions(String entityId, int partsMask,
			EntityBundleTransport transport, Synapse synapseClient)
			throws SynapseException, JSONObjectAdapterException {
		if ((EntityBundleTransport.ANNOTATIONS & partsMask) > 0) {
			Annotations annos = synapseClient.getAnnotations(entityId);
			transport.setAnnotaionsJson(EntityFactory
					.createJSONStringForEntity(annos));
		}
	}

	/**
	 * Add an entity to the bundle if requested
	 * 
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleEntity(String entityId, int partsMask,
			EntityBundleTransport transport, Synapse synapseClient)
			throws SynapseException, JSONObjectAdapterException {
		if ((EntityBundleTransport.ENTITY & partsMask) > 0) {
			Entity e = synapseClient.getEntityById(entityId);
			transport.setEntityJson(EntityFactory.createJSONStringForEntity(e));
		}
	}

	/**
	 * Set the entity path if requested
	 * 
	 * @param entityId
	 * @param partsMask
	 * @param transport
	 * @param synapseClient
	 * @throws SynapseException
	 * @throws JSONObjectAdapterException
	 */
	public void handleEntityReferencedBy(String entityId, int partsMask,
			EntityBundleTransport transport, Synapse synapseClient)
			throws SynapseException, JSONObjectAdapterException {
		if ((EntityBundleTransport.ENTITY_REFERENCEDBY & partsMask) > 0) {
			// TODO : support entity version
			PaginatedResults<EntityHeader> results = synapseClient
					.getEntityReferencedBy(entityId, null);
			transport.setEntityReferencedByJson(EntityFactory
					.createJSONStringForEntity(results));
		}
	}

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
	public void deleteEntity(String entityId) throws RestServiceException {
		try {
			Synapse synapseClient = createSynapseClient();
			synapseClient.deleteEntityById(entityId);			
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public String getUserProfile() throws RestServiceException {
		try {
			Synapse synapseClient = createSynapseClient();
			JSONObject userProfile = synapseClient.getSynapseEntity(urlProvider.getRepositoryServiceUrl(), "/userProfile");
			return userProfile.toString();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}

	@Override
	public void updateUserProfile(String userProfileJson)
			throws RestServiceException {
		try {
			Synapse synapseClient = createSynapseClient();
			synapseClient.putEntity("/userProfile", new JSONObject(userProfileJson));			
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} catch (JSONException e) {
			throw new UnknownErrorException(e.getMessage());
		} 
	}

}
