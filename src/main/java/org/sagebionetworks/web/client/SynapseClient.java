package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("synapse")	
public interface SynapseClient extends RemoteService {

	public EntityWrapper getEntity(String entityId) throws RestServiceException;
	
	public EntityWrapper getEntityForVersion(String entityId, Long versionNumber) throws RestServiceException;
		
	public String getEntityVersions(String entityId, int offset, int limit) throws RestServiceException;

	public void deleteEntityById(String entityId) throws RestServiceException;

	public void deleteEntityVersionById(String entityId, Long versionNumber) throws RestServiceException;
	
	public EntityWrapper getEntityPath(String entityId) throws RestServiceException;
	
	public EntityWrapper search(String searchQueryJson) throws RestServiceException; 
	
	public String getEntityTypeBatch(List<String> entityIds) throws RestServiceException;
	
	public String getEntityHeaderBatch(String referenceList) throws RestServiceException;
	
	public SerializableWhitelist junk(SerializableWhitelist l);
	
	/**
	 * Updates the entity in the repo  
	 * @param entityJson - the JSON string representing the entity
	 * @return the updated version of the entity
	 * @throws RestServiceException
	 */
	public EntityWrapper updateEntity(String entityJson) throws RestServiceException;
	
	/**
	 * Get a bundle of information about an entity in a single call
	 * @param entityId
	 * @return
	 * @throws RestServiceException 
	 * @throws SynapseException 
	 */
	public EntityBundleTransport getEntityBundle(String entityId, int partsMask) throws RestServiceException;

	/**
	 * Get a bundle of information about an entity in a single call
	 * @param entityId
	 * @return
	 * @throws RestServiceException 
	 * @throws SynapseException 
	 */
	public EntityBundleTransport getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask) throws RestServiceException;

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
	public String getUserProfile() throws RestServiceException;
	
	/**
	 * Returns the specified user's profile object in json string
	 * @return
	 * @throws RestServiceException
	 */
	public String getUserProfile(String userId) throws RestServiceException;
	
	/**
	 * Batch get headers for users/groups matching a list of Synapse IDs.
	 * 
	 * @param ids
	 * @return
	 * @throws RestServiceException
	 */
	public EntityWrapper getUserGroupHeadersById(List<String> ids) throws RestServiceException;

	/**
	 * Updates the user's profile json object 
	 * @param userProfileJson json object of the user's profile
	 * @throws RestServiceException
	 */
	public void updateUserProfile(String userProfileJson) throws RestServiceException;
	
	public EntityWrapper getNodeAcl(String id) throws RestServiceException;
	
	public EntityWrapper createAcl(EntityWrapper acl) throws RestServiceException;
	
	/**
	 * Update an ACL. Default to non-recursive application.
	 */
	public EntityWrapper updateAcl(EntityWrapper acl) throws RestServiceException;
	
	/**
	 * Update an entity's ACL. If 'recursive' is set to true, then any child 
	 * ACLs will be deleted, such that all child entities inherit this ACL. 
	 */
	public EntityWrapper updateAcl(EntityWrapper aclEW, boolean recursive) throws RestServiceException;

	public EntityWrapper deleteAcl(String ownerEntityId) throws RestServiceException;

	public boolean hasAccess(String ownerEntityId, String accessType) throws RestServiceException;
	
	public boolean hasAccess(String ownerId, String ownerType, String accessType) throws RestServiceException;

	public EntityWrapper getAllUsers() throws RestServiceException;
	
	public EntityWrapper getAllGroups() throws RestServiceException;

	public String createUserProfileAttachmentPresignedUrl(String id,
			String tokenOrPreviewId) throws RestServiceException;

	EntityWrapper createAccessRequirement(EntityWrapper arEW)
			throws RestServiceException;

	AccessRequirementsTransport getUnmetAccessRequirements(String entityId)
			throws RestServiceException;

	EntityWrapper createAccessApproval(EntityWrapper aaEW)
			throws RestServiceException;
	
	public EntityWrapper updateExternalLocationable(String entityId, String externalUrl) throws RestServiceException;
	
	public EntityWrapper updateExternalFile(String entityId, String externalUrl) throws RestServiceException;
	
	public EntityWrapper createExternalFile(String parentEntityId, String externalUrl) throws RestServiceException;
	
	/**
	 * convenience method for converting markdown to html
	 * @param markdown
	 * @return
	 */
	public String markdown2Html(String markdown, Boolean isPreview) throws RestServiceException;
	
	public Long getStorageUsage(String entityId) throws RestServiceException;

	public String getActivityForEntity(String entityId) throws RestServiceException;
	
	public String getActivityForEntityVersion(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getActivity(String activityId) throws RestServiceException;
	
	public String getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset) throws RestServiceException;
	
	String promoteEntityVersion(String entityId, Long versionNumber) throws RestServiceException;
	public EntityWrapper removeAttachmentFromEntity(String entityId, String attachmentName) throws RestServiceException;
	public String getJSONEntity(String repoUri) throws RestServiceException;
	
	//wiki crud
	public String createWikiPage(String ownerId, String ownerType, String wikiPageJson) throws RestServiceException;
	public String getWikiPage(WikiPageKey key)  throws RestServiceException;
	public String updateWikiPage(String ownerId, String ownerType, String wikiPageJson)  throws RestServiceException;
	public void deleteWikiPage(WikiPageKey key)  throws RestServiceException;
	
	public String getWikiHeaderTree(String ownerId, String ownerType) throws RestServiceException;
	
	public String getWikiAttachmentHandles(WikiPageKey key) throws RestServiceException;
	
	public String getFileEndpoint() throws RestServiceException;
	
	public String addFavorite(String entityId) throws RestServiceException;
	
	public void removeFavorite(String entityId) throws RestServiceException;
	
	public String getFavorites(Integer limit, Integer offset) throws RestServiceException;
	
	public UserEvaluationState getUserEvaluationState(String evaluationId) throws RestServiceException;
	
	public String createParticipant(String evaluationId) throws RestServiceException;
	
	public String getDescendants(String nodeId, int pageSize, String lastDescIdExcl) throws RestServiceException;
	
	public String getChunkedFileToken(String fileName, String contentType, long chunkNumber) throws RestServiceException;
	public String getChunkedPresignedUrl(String requestJson) throws RestServiceException;
	public String completeChunkedFileUpload(String entityId, String requestJson,String parentEntityId, boolean isRestricted) throws RestServiceException;
	
	public String getEntityDoi(String entityId, Long versionNumber) throws RestServiceException;
	public void createDoi(String entityId, Long versionNumber) throws RestServiceException;
	
	public String getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber) throws RestServiceException;
}
