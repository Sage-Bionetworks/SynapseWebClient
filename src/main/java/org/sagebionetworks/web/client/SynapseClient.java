package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("synapse")	
public interface SynapseClient extends RemoteService {
	
	
	

	public EntityWrapper getEntity(String entityId);
	
	public void deleteEntity(String entityId) throws RestServiceException;

	//public EntityWrapper createEntity(EntityType type, JSONObjectAdaptor properties);
	
	public String getEntityTypeRegistryJSON();
	
	public EntityWrapper getEntityPath(String entityId);
	
	public EntityWrapper search(String searchQueryJson); 
	
	public String getEntityTypeBatch(List<String> entityIds) throws RestServiceException;
	
	public SerializableWhitelist junk(SerializableWhitelist l);
	
	
	/**
	 * Get a bundle of information about an entity in a single call
	 * @param entityId
	 * @return
	 * @throws RestServiceException 
	 * @throws SynapseException 
	 */
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask) throws RestServiceException;

	public String getEntityReferencedBy(String entityId) throws RestServiceException;
	
	/**
	 * Log a debug message in the server-side log.
	 * @param message
	 */
	public void logDebug(String message);
	
	/**
	 * Log an error message in the server-side log.
	 * @param message
	 */
	public void logError(String message);
	

	/**
	 * Log an info message in the server-side log.
	 * @param message
	 */
	public void logInfo(String message);
	
	/**
	 * Get the repository service URL
	 * @return
	 */
	public String getRepositoryServiceUrl();
	

	/**
	 * Create or update an entity
	 * @param entityJson
	 * @param annoJson
	 * @param isNew
	 * @return
	 * @throws RestServiceException 
	 */
	public String createOrUpdateEntity(String entityJson, String annoJson, boolean isNew) throws RestServiceException;

	/**
	 * Returns the user's profile object in json string
	 * @return
	 * @throws RestServiceException
	 */
	public String getUserProfile() throws RestServiceException;;
	
	/**
	 * Updates the user's profile json object 
	 * @param userProfileJson json object of the user's profile
	 * @throws RestServiceException
	 */
	public void updateUserProfile(String userProfileJson) throws RestServiceException;
	
}
