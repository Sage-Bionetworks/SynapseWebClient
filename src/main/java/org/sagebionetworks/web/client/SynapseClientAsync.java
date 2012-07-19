package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.SerializableWhitelist;

import com.google.gwt.user.client.rpc.AsyncCallback;
	
public interface SynapseClientAsync {

	void getEntity(String entityId, AsyncCallback<EntityWrapper> callback);
	
	void getEntityBundle(String entityId, int partsMask, AsyncCallback<EntityBundleTransport> callback);

	void getEntityTypeRegistryJSON(AsyncCallback<String> callback);

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
	
	void getEntityTypeBatch(List<String> entityIds,
			AsyncCallback<String> callback);

	void deleteEntity(String entityId, AsyncCallback<Void> callback);

	void getUserProfile(AsyncCallback<String> callback);
	
	void getUserProfile(String userId, AsyncCallback<String> callback);
	
	void updateUserProfile(String userProfileJson, AsyncCallback<Void> callback);
	
	void createUserProfileAttachmentPresignedUrl(String id, String tokenOrPreviewId, AsyncCallback<String> callback);
	
	public void getNodeAcl(String id, AsyncCallback<EntityWrapper> callback);
	
	public void createAcl(EntityWrapper acl, AsyncCallback<EntityWrapper> callback);
	
	public void updateAcl(EntityWrapper acl, AsyncCallback<EntityWrapper> callback);
	
	public void deleteAcl(String ownerEntityId, AsyncCallback<EntityWrapper> callback);

	public void hasAccess(String ownerEntityId, String accessType, AsyncCallback<Boolean> callback);

	public void getAllUsers(AsyncCallback<EntityWrapper> callback);
	
	public void getAllGroups(AsyncCallback<EntityWrapper> callback);
	
	public void createAccessRequirement(EntityWrapper arEW, AsyncCallback<EntityWrapper> callback);

	public void getUnmetAccessRequirements(String entityId, AsyncCallback<AccessRequirementsTransport> callback);

	public void createAccessApproval(EntityWrapper aaEW, AsyncCallback<EntityWrapper> callback);

}
