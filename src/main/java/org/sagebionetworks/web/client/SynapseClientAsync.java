
package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;
	
public interface SynapseClientAsync {

	void getEntity(String entityId, AsyncCallback<EntityWrapper> callback);
	
	void getEntityForVersion(String entityId, Long versionNumber, AsyncCallback<EntityWrapper> callback);
	
	void getEntityBundle(String entityId, int partsMask, AsyncCallback<EntityBundleTransport> callback);
	
	void getEntityBundleForVersion(String entityId, Long versionNumber, int partsMask, AsyncCallback<EntityBundleTransport> callback);

	void getEntityVersions(String entityId, int offset, int limit, AsyncCallback<String> callback);

	void updateEntity(String entityJson, AsyncCallback<EntityWrapper> callback);

	void getEntityPath(String entityId, AsyncCallback<EntityWrapper> callback);

	void search(String searchQueryJson, AsyncCallback<EntityWrapper> callback);

	void junk(SerializableWhitelist l,
			AsyncCallback<SerializableWhitelist> callback);

	void getEntityReferencedBy(String entityId, AsyncCallback<String> callback);

	void logDebug(String message, AsyncCallback<Void> callback);

	void logError(String message, AsyncCallback<Void> callback);

	void logInfo(String message, AsyncCallback<Void> callback);

	void getRepositoryServiceUrl(AsyncCallback<String> callback);

	void createOrUpdateEntity(String entityJson, String annoJson,
			boolean isNew, AsyncCallback<String> callback);
	
	void getEntityTypeBatch(List<String> entityIds, AsyncCallback<String> callback);
	
	void getEntityHeaderBatch(String referenceList,
			AsyncCallback<String> callback);

	void deleteEntityById(String entityId, AsyncCallback<Void> callback);

	void deleteEntityVersionById(String entityId, Long versionNumber, AsyncCallback<Void> callback);

	void getUserProfile(AsyncCallback<String> callback);
	
	void getUserProfile(String userId, AsyncCallback<String> callback);
	
	void getUserGroupHeadersById(List<String> ids, AsyncCallback<EntityWrapper> headers);
	
	void updateUserProfile(String userProfileJson, AsyncCallback<Void> callback);
	
	void createUserProfileAttachmentPresignedUrl(String id, String tokenOrPreviewId, AsyncCallback<String> callback);
	
	public void getNodeAcl(String id, AsyncCallback<EntityWrapper> callback);
	
	public void createAcl(EntityWrapper acl, AsyncCallback<EntityWrapper> callback);
	
	public void updateAcl(EntityWrapper acl, AsyncCallback<EntityWrapper> callback);
	
	public void updateAcl(EntityWrapper acl, boolean recursive, AsyncCallback<EntityWrapper> callback);
	
	public void deleteAcl(String ownerEntityId, AsyncCallback<EntityWrapper> callback);

	public void hasAccess(String ownerEntityId, String accessType, AsyncCallback<Boolean> callback);
	
	public void hasAccess(String ownerId, String ownerType, String accessType,AsyncCallback<Boolean> callback);

	public void getAllUsers(AsyncCallback<EntityWrapper> callback);
	
	public void getAllGroups(AsyncCallback<EntityWrapper> callback);
	
	public void createAccessRequirement(EntityWrapper arEW, AsyncCallback<EntityWrapper> callback);

	public void getUnmetAccessRequirements(String entityId, AsyncCallback<AccessRequirementsTransport> callback);

	public void createAccessApproval(EntityWrapper aaEW, AsyncCallback<EntityWrapper> callback);

	public void updateExternalLocationable(String entityId, String externalUrl, AsyncCallback<EntityWrapper> callback);
	
	public void updateExternalFile(String entityId, String externalUrl, AsyncCallback<EntityWrapper> callback) throws RestServiceException;
	
	public void createExternalFile(String parentEntityId, String externalUrl, AsyncCallback<EntityWrapper> callback) throws RestServiceException;

	public void markdown2Html(String markdown, Boolean isPreview, AsyncCallback<String> callback);
	
	public void getStorageUsage(String entityId, AsyncCallback<Long> callback);

	void getActivityForEntityVersion(String entityId, Long versionNumber, AsyncCallback<String> callback);

	void getActivityForEntity(String entityId, AsyncCallback<String> callback);

	void getActivity(String activityId, AsyncCallback<String> callback);
	
	void promoteEntityVersion(String entityId, Long versionNumber, AsyncCallback<String> callback);
	
	void removeAttachmentFromEntity(String entityId, String attachmentName, AsyncCallback<EntityWrapper> callback) throws RestServiceException;
	
	public void getJSONEntity(String repoUri, AsyncCallback<String> callback);
	
	//wiki crud
	public void createWikiPage(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
	public void getWikiPage(WikiPageKey key, AsyncCallback<String> callback);
	public void updateWikiPage(String ownerId, String ownerType, String wikiPageJson, AsyncCallback<String> callback);
	public void deleteWikiPage(WikiPageKey key, AsyncCallback<Void> callback);
	
	public void getWikiHeaderTree(String ownerId, String ownerType, AsyncCallback<String> callback);
	public void getWikiAttachmentHandles(WikiPageKey key, AsyncCallback<String> callback);
	public void getFileEndpoint(AsyncCallback<String> callback);

	void getEntitiesGeneratedBy(String activityId, Integer limit, Integer offset, AsyncCallback<String> callback);

	void addFavorite(String entityId, AsyncCallback<String> callback);

	void removeFavorite(String entityId, AsyncCallback<Void> callback);

	void getFavorites(Integer limit, Integer offset,
			AsyncCallback<String> callback);
	
	void getUserEvaluationState(String evaluationId, AsyncCallback<UserEvaluationState> callback) throws RestServiceException;

	/**
	 * Returns json string representation of created Participant
	 * @param evaluationId
	 * @return
	 * @throws RestServiceException
	 */
	void createParticipant(String evaluationId, AsyncCallback<String> callback) throws RestServiceException;
	
	void getDescendants(String nodeId, int pageSize, String lastDescIdExcl, AsyncCallback<String> callback);
	void getChunkedFileToken(String fileName,  String contentType, long chunkNumber, AsyncCallback<String> callback) throws RestServiceException;
	void getChunkedPresignedUrl(String requestJson, AsyncCallback<String> callback) throws RestServiceException;
	void completeChunkedFileUpload(String entityId, String requestJson, String parentEntityId, boolean isRestricted, AsyncCallback<String> callback) throws RestServiceException;
	void getEntityDoi(String entityId, Long versionNumber, AsyncCallback<String> callback);
	void createDoi(String entityId, Long versionNumber, AsyncCallback<Void> callback);

	void getFileEntityTemporaryUrlForVersion(String entityId, Long versionNumber, AsyncCallback<String> callback);
}
